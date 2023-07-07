package org.fffd.l23o6.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.fffd.l23o6.dao.RouteDao;
import org.fffd.l23o6.dao.TrainDao;
import org.fffd.l23o6.mapper.TrainMapper;
import org.fffd.l23o6.pojo.entity.RouteEntity;
import org.fffd.l23o6.pojo.entity.TrainEntity;
import org.fffd.l23o6.pojo.enum_.TrainType;
import org.fffd.l23o6.pojo.vo.train.TicketInfo;
import org.fffd.l23o6.service.TrainService;
import org.fffd.l23o6.pojo.vo.train.AdminTrainVO;
import org.fffd.l23o6.pojo.vo.train.TrainVO;
import org.fffd.l23o6.pojo.vo.train.TrainDetailVO;
import org.fffd.l23o6.util.strategy.train.GSeriesSeatStrategy;
import org.fffd.l23o6.util.strategy.train.KSeriesSeatStrategy;
import org.fffd.l23o6.util.strategy.train.TrainSeatStrategy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import io.github.lyc8503.spring.starter.incantation.exception.CommonErrorType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrainServiceImpl implements TrainService {
    private final TrainDao trainDao;
    private final RouteDao routeDao;

    @Override
    public TrainDetailVO getTrain(Long trainId) {
        TrainEntity train = trainDao.findById(trainId).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        return TrainDetailVO.builder().id(trainId).date(train.getDate()).name(train.getName())
                .stationIds(route.getStationIds()).arrivalTimes(train.getArrivalTimes())
                .departureTimes(train.getDepartureTimes()).extraInfos(train.getExtraInfos()).build();
    }

    @Override
    public List<TrainVO> listTrains(Long startStationId, Long endStationId, String date) {
        // TODO
        // First, get all routes contains [startCity, endCity]
        List<RouteEntity> routes = routeDao.findAll(); // 获取所有路线信息

        List<RouteEntity> filteredRoutes = routes.stream()
                .filter(route -> {
                    List<Long> stationIds = route.getStationIds();
                    return stationIds.contains(startStationId) && stationIds.contains(endStationId)
                            && stationIds.indexOf(startStationId) < stationIds.indexOf(endStationId);
                }).toList();

        // Then, Get all trains on that day with the wanted routes
        List<TrainEntity> trainsOnDate = trainDao.findAll().stream()
                .filter(train -> train.getDate().equals(date)).toList();

        List<TrainEntity> matchingTrains = new ArrayList<>();

        for (RouteEntity route : filteredRoutes) {
            for (TrainEntity train : trainsOnDate) {
                if (train.getRouteId().equals(route.getId())) {
                    matchingTrains.add(train);
                }
            }
        }


        List<TrainVO> trainVOs = matchingTrains.stream()
                .map(train -> {

                    TrainVO trainVO = TrainMapper.INSTANCE.toTrainVO(train);

                    trainVO.setStartStationId(startStationId);
                    trainVO.setEndStationId(endStationId);
                    TrainDetailVO trainDetailVO = getTrain(trainVO.getId());
                    List<Long> stationIds = trainDetailVO.getStationIds();
                    int startStationIndex = stationIds.indexOf(startStationId);
                    int endStationIndex = stationIds.indexOf(endStationId);
                    int stationNum = endStationIndex - startStationIndex;

                    trainVO.setDepartureTime(getDepartureTimeByStationId(trainDetailVO, startStationId));
                    trainVO.setArrivalTime(getArrivalTimeByStationId(trainDetailVO, endStationId));


                    List<TicketInfo> ticketInfoList = new ArrayList<>();

                    // TODO price
                    if(train.getTrainType().getText().equals("高铁")){
                        Map<GSeriesSeatStrategy.GSeriesSeatType, Integer> leftSeatCount
                                = GSeriesSeatStrategy.INSTANCE.getLeftSeatCount(startStationIndex,
                                endStationIndex, train.getSeats());
                        TicketInfo ticketInfo1 = new TicketInfo("商务座",
                                leftSeatCount.get(GSeriesSeatStrategy.GSeriesSeatType.BUSINESS_SEAT),80 * stationNum);
                        TicketInfo ticketInfo2 = new TicketInfo("一等座",
                                leftSeatCount.get(GSeriesSeatStrategy.GSeriesSeatType.FIRST_CLASS_SEAT),60 * stationNum);
                        TicketInfo ticketInfo3 = new TicketInfo("二等座",
                                leftSeatCount.get(GSeriesSeatStrategy.GSeriesSeatType.SECOND_CLASS_SEAT),40 * stationNum);
                        TicketInfo ticketInfo4 = new TicketInfo("无座",
                                leftSeatCount.get(GSeriesSeatStrategy.GSeriesSeatType.NO_SEAT),20 * stationNum);
                        ticketInfoList.add(ticketInfo1);
                        ticketInfoList.add(ticketInfo2);
                        ticketInfoList.add(ticketInfo3);
                        ticketInfoList.add(ticketInfo4);
                    }else {
                        Map<KSeriesSeatStrategy.KSeriesSeatType, Integer> leftSeatCount
                                = KSeriesSeatStrategy.INSTANCE.getLeftSeatCount(startStationIndex,
                                 endStationIndex, train.getSeats());
                        TicketInfo ticketInfo1 = new TicketInfo("软卧",
                                leftSeatCount.get(KSeriesSeatStrategy.KSeriesSeatType.SOFT_SLEEPER_SEAT),50 * stationNum);
                        TicketInfo ticketInfo2 = new TicketInfo("硬卧",
                                leftSeatCount.get(KSeriesSeatStrategy.KSeriesSeatType.HARD_SLEEPER_SEAT),40 * stationNum);
                        TicketInfo ticketInfo3 = new TicketInfo("软座",
                                leftSeatCount.get(KSeriesSeatStrategy.KSeriesSeatType.SOFT_SEAT),30 * stationNum);
                        TicketInfo ticketInfo4 = new TicketInfo("硬座",
                                leftSeatCount.get(KSeriesSeatStrategy.KSeriesSeatType.HARD_SEAT),20 * stationNum);
                        TicketInfo ticketInfo5 = new TicketInfo("无座",
                                leftSeatCount.get(KSeriesSeatStrategy.KSeriesSeatType.NO_SEAT),10 * stationNum);
                        ticketInfoList.add(ticketInfo1);
                        ticketInfoList.add(ticketInfo2);
                        ticketInfoList.add(ticketInfo3);
                        ticketInfoList.add(ticketInfo4);
                        ticketInfoList.add(ticketInfo5);
                    }

                    trainVO.setTicketInfo(ticketInfoList);

                    return trainVO;
                })
                .collect(Collectors.toList());

        return trainVOs;
    }


    public Date getDepartureTimeByStationId(TrainDetailVO trainDetailVO, Long stationId) {
        List<Long> stationIds = trainDetailVO.getStationIds();
        List<Date> departureTimes = trainDetailVO.getDepartureTimes();

        if (stationIds != null && departureTimes != null && stationIds.size() == departureTimes.size()) {
            int index = stationIds.indexOf(stationId);
            if (index != -1) {
                return departureTimes.get(index);
            }
        }

        return null;
    }

    public Date getArrivalTimeByStationId(TrainDetailVO trainDetailVO, Long stationId) {
        List<Long> stationIds = trainDetailVO.getStationIds();
        List<Date> arrivalTimes = trainDetailVO.getArrivalTimes();

        if (stationIds != null && arrivalTimes != null && stationIds.size() == arrivalTimes.size()) {
            int index = stationIds.indexOf(stationId);
            if (index != -1) {
                return arrivalTimes.get(index);
            }
        }

        return null;
    }



    @Override
    public List<AdminTrainVO> listTrainsAdmin() {
        return trainDao.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(TrainMapper.INSTANCE::toAdminTrainVO).collect(Collectors.toList());
    }

    @Override
    public void addTrain(String name, Long routeId, TrainType type, String date, List<Date> arrivalTimes,
            List<Date> departureTimes) {
        TrainEntity entity = TrainEntity.builder().name(name).routeId(routeId).trainType(type)
                .date(date).arrivalTimes(arrivalTimes).departureTimes(departureTimes).build();
        RouteEntity route = routeDao.findById(routeId).get();
        if (route.getStationIds().size() != entity.getArrivalTimes().size()
                || route.getStationIds().size() != entity.getDepartureTimes().size()) {
            throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "时间信息不完整");
        }
        entity.setExtraInfos(new ArrayList<String>(Collections.nCopies(route.getStationIds().size(), "预计正点")));
        switch (entity.getTrainType()) {
            case HIGH_SPEED -> entity.setSeats(GSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
            case NORMAL_SPEED -> entity.setSeats(KSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
        }
        trainDao.save(entity);
    }

    @Override
    public void changeTrain(Long id, String name, Long routeId, TrainType type, String date,
                            List<Date> arrivalTimes, List<Date> departureTimes) {
        // TODO: edit train info, please refer to `addTrain` above
        TrainEntity train = trainDao.findById(id).orElse(null);
        if (train == null) {
            throw new BizException(CommonErrorType.NOT_FOUND, "指定的火车信息不存在");
        }

        train.setName(name);
        train.setRouteId(routeId);
        train.setTrainType(type);
        train.setDate(date);
        train.setArrivalTimes(arrivalTimes);
        train.setDepartureTimes(departureTimes);

        RouteEntity route = routeDao.findById(routeId).orElse(null);
        if (route == null) {
            throw new BizException(CommonErrorType.NOT_FOUND, "指定的路线信息不存在");
        }

        if (route.getStationIds().size() != arrivalTimes.size()
                || route.getStationIds().size() != departureTimes.size()) {
            throw new BizException(CommonErrorType.ILLEGAL_ARGUMENTS, "时间信息不完整");
        }

        train.setExtraInfos(new ArrayList<>(Collections.nCopies(route.getStationIds().size(), "预计正点")));

        switch (type) {
            case HIGH_SPEED -> train.setSeats(GSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));
            case NORMAL_SPEED -> train.setSeats(KSeriesSeatStrategy.INSTANCE.initSeatMap(route.getStationIds().size()));

            // Add more cases for other train types if necessary
        }

        trainDao.save(train);
    }


    @Override
    public void deleteTrain(Long id) {
        trainDao.deleteById(id);
    }
}
