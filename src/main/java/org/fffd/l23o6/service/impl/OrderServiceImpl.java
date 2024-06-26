package org.fffd.l23o6.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.fffd.l23o6.dao.OrderDao;
import org.fffd.l23o6.dao.RouteDao;
import org.fffd.l23o6.dao.TrainDao;
import org.fffd.l23o6.dao.UserDao;
import org.fffd.l23o6.pojo.entity.UserEntity;
import org.fffd.l23o6.pojo.enum_.OrderStatus;
import org.fffd.l23o6.exception.BizError;
import org.fffd.l23o6.pojo.entity.OrderEntity;
import org.fffd.l23o6.pojo.entity.RouteEntity;
import org.fffd.l23o6.pojo.entity.TrainEntity;
import org.fffd.l23o6.service.OrderService;
import org.fffd.l23o6.pojo.vo.order.OrderVO;
import org.fffd.l23o6.util.strategy.train.GSeriesSeatStrategy;
import org.fffd.l23o6.util.strategy.train.KSeriesSeatStrategy;
import org.springframework.stereotype.Service;

import io.github.lyc8503.spring.starter.incantation.exception.BizException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderDao orderDao;
    private final UserDao userDao;
    private final TrainDao trainDao;
    private final RouteDao routeDao;

    public Long createOrder(String username, Long trainId, Long fromStationId, Long toStationId, String seatType,
            Long seatNumber) {
        Long userId = userDao.findByUsername(username).getId();
        TrainEntity train = trainDao.findById(trainId).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        int startStationIndex = route.getStationIds().indexOf(fromStationId);
        int endStationIndex = route.getStationIds().indexOf(toStationId);
        String seat = switch (train.getTrainType()) {
            case HIGH_SPEED -> GSeriesSeatStrategy.INSTANCE.allocSeat(startStationIndex, endStationIndex,
                    GSeriesSeatStrategy.GSeriesSeatType.fromString(seatType), train.getSeats());
            case NORMAL_SPEED -> KSeriesSeatStrategy.INSTANCE.allocSeat(startStationIndex, endStationIndex,
                    KSeriesSeatStrategy.KSeriesSeatType.fromString(seatType), train.getSeats());
        };
        if (seat == null) {
            throw new BizException(BizError.OUT_OF_SEAT);
        }
        OrderEntity order = OrderEntity.builder().trainId(trainId).userId(userId).seat(seat)
                .status(OrderStatus.PENDING_PAYMENT).arrivalStationId(toStationId).departureStationId(fromStationId)
                .build();
        train.setUpdatedAt(null);// force it to update
        trainDao.save(train);
        orderDao.save(order);
        return order.getId();
    }

    public List<OrderVO> listOrders(String username) {
        Long userId = userDao.findByUsername(username).getId();
        List<OrderEntity> orders = orderDao.findByUserId(userId);
        orders.sort((o1,o2)-> o2.getId().compareTo(o1.getId()));
        return orders.stream().map(order -> {
            TrainEntity train = trainDao.findById(order.getTrainId()).get();
            RouteEntity route = routeDao.findById(train.getRouteId()).get();
            int startIndex = route.getStationIds().indexOf(order.getDepartureStationId());
            int endIndex = route.getStationIds().indexOf(order.getArrivalStationId());
            return OrderVO.builder().id(order.getId()).trainId(order.getTrainId())
                    .seat(order.getSeat()).status(order.getStatus().getText())
                    .createdAt(order.getCreatedAt())
                    .startStationId(order.getDepartureStationId())
                    .endStationId(order.getArrivalStationId())
                    .departureTime(train.getDepartureTimes().get(startIndex))
                    .arrivalTime(train.getArrivalTimes().get(endIndex))
                    .build();
        }).collect(Collectors.toList());
    }

    public OrderVO getOrder(Long id) {
        OrderEntity order = orderDao.findById(id).get();
        TrainEntity train = trainDao.findById(order.getTrainId()).get();
        RouteEntity route = routeDao.findById(train.getRouteId()).get();
        int startIndex = route.getStationIds().indexOf(order.getDepartureStationId());
        int endIndex = route.getStationIds().indexOf(order.getArrivalStationId());
        return OrderVO.builder().id(order.getId()).trainId(order.getTrainId())
                .seat(order.getSeat()).status(order.getStatus().getText())
                .createdAt(order.getCreatedAt())
                .startStationId(order.getDepartureStationId())
                .endStationId(order.getArrivalStationId())
                .departureTime(train.getDepartureTimes().get(startIndex))
                .arrivalTime(train.getArrivalTimes().get(endIndex))
                .build();
    }

    public int getPriceById(Long id){
        OrderEntity order = orderDao.findById(id).get();
        TrainEntity train = trainDao.getReferenceById(order.getTrainId());
        RouteEntity route = routeDao.getReferenceById(train.getRouteId());

        List<Long> stationIds = route.getStationIds();
        int startStationIndex = stationIds.indexOf(order.getDepartureStationId());
        int endStationIndex = stationIds.indexOf(order.getArrivalStationId());

        String seatName = order.getSeat();

        int price = switch (train.getTrainType()) {
            case HIGH_SPEED ->
                GSeriesSeatStrategy.INSTANCE.getPriceByType(GSeriesSeatStrategy.INSTANCE.getTypeBySeatName(seatName),
                        startStationIndex, endStationIndex);
            case NORMAL_SPEED ->
                KSeriesSeatStrategy.INSTANCE.getPriceByType(KSeriesSeatStrategy.INSTANCE.getTypeByName(seatName),
                        startStationIndex, endStationIndex);
        };
        return price;
    }

    public void cancelOrder(Long id) {
        OrderEntity order = orderDao.findById(id).get();

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BizException(BizError.ILLEAGAL_ORDER_STATUS);
        }

        // TODO 返还座位
        TrainEntity train = trainDao.getReferenceById(order.getTrainId());
        RouteEntity route = routeDao.getReferenceById(train.getRouteId());

        List<Long> stationIds = route.getStationIds();
        int startStationIndex = stationIds.indexOf(order.getArrivalStationId());
        int endStationIndex = stationIds.indexOf(order.getDepartureStationId());

        String seatName = order.getSeat();

        switch (train.getTrainType()) {
            case HIGH_SPEED -> GSeriesSeatStrategy.INSTANCE.returnSeat(seatName, startStationIndex, endStationIndex, train.getSeats());
            case NORMAL_SPEED -> KSeriesSeatStrategy.INSTANCE.returnSeat(seatName, startStationIndex, endStationIndex, train.getSeats());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderDao.save(order);
    }

    public void payOrder(Long id) {
        OrderEntity order = orderDao.findById(id).get();
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BizException(BizError.ILLEAGAL_ORDER_STATUS);
        }

        // TODO: use payment strategy to pay!
        UserEntity user = userDao.getReferenceById(order.getUserId());
        int credit = user.getCredit();
        int price_order = getPriceById(id);
        int price_pay = price_order;
        int credit_left;
        if (credit >= 50000) {
            credit_left = credit - 50000;
            price_pay = price_pay - 118;
        } else if (credit >= 10000) {
            credit_left = credit - 10000;
            price_pay = price_pay - 18;
        } else if (credit >= 3000) {
            credit_left = credit - 3000;
            price_pay = price_pay - 4;
        } else if (credit >= 1000) {
            credit_left = credit - 1000;
            price_pay = price_pay - 1;
        } else {
            credit_left = credit;
        }
        user.setMoney(user.getMoney() - price_pay);
        user.setCredit(price_order * 100 + credit_left);
        order.setStatus(OrderStatus.COMPLETED);
        orderDao.save(order);
    }


}
