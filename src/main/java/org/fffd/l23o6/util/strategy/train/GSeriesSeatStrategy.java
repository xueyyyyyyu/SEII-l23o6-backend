package org.fffd.l23o6.util.strategy.train;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Nullable;


public class GSeriesSeatStrategy extends TrainSeatStrategy {
    public static final GSeriesSeatStrategy INSTANCE = new GSeriesSeatStrategy();

    private final Map<Integer, String> BUSINESS_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> FIRST_CLASS_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> SECOND_CLASS_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> NO_SEAT_MAP = new HashMap<>();

    private final Map<GSeriesSeatType, Map<Integer, String>> TYPE_MAP = new HashMap<>() {{
        put(GSeriesSeatType.BUSINESS_SEAT, BUSINESS_SEAT_MAP);
        put(GSeriesSeatType.FIRST_CLASS_SEAT, FIRST_CLASS_SEAT_MAP);
        put(GSeriesSeatType.SECOND_CLASS_SEAT, SECOND_CLASS_SEAT_MAP);
        put(GSeriesSeatType.NO_SEAT, NO_SEAT_MAP);
    }};


    private GSeriesSeatStrategy() {

        int counter = 0;

        for (String s : Arrays.asList("1车1A","1车1C","1车1F")) {
            BUSINESS_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("2车1A","2车1C","2车1D","2车1F","2车2A","2车2C","2车2D","2车2F","3车1A","3车1C","3车1D","3车1F")) {
            FIRST_CLASS_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("4车1A","4车1B","4车1C","4车1D","4车2F","4车2A","4车2B","4车2C","4车2D","4车2F","4车3A","4车3B","4车3C","4车3D","4车3F")) {
            SECOND_CLASS_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("无座01","无座02","无座03","无座04","无座05","无座05","无座06","无座07","无座07","无座08",
                "无座09","无座10","无座11","无座12","无座13","无座14","无座15","无座16","无座17","无座18")) {
            NO_SEAT_MAP.put(counter++, s);
        }

    }

    public enum GSeriesSeatType implements SeatType {
        BUSINESS_SEAT("商务座"), FIRST_CLASS_SEAT("一等座"), SECOND_CLASS_SEAT("二等座"), NO_SEAT("无座");
        private String text;
        GSeriesSeatType(String text){
            this.text=text;
        }
        public String getText() {
            return this.text;
        }
        public static GSeriesSeatType fromString(String text) {
            for (GSeriesSeatType b : GSeriesSeatType.values()) {
                if (b.text.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }


    public @Nullable String allocSeat(int startStationIndex, int endStationIndex, GSeriesSeatType type, boolean[][] seatMap) {
        Map<Integer, String> currentSeatMap = TYPE_MAP.get(type);
        int offset = getOffset(type);

        for (int j = 0; j < currentSeatMap.size(); j++) {
            boolean available = true;
            for (int i = startStationIndex; i < endStationIndex; i++) {
                if (seatMap[i][j + offset]) {
                    available = false;
                    break;
                }
            }
            if (available) {
                // 座位可用，更新seatMap
                for (int i = startStationIndex; i < endStationIndex; i++) {
                    seatMap[i][j + offset] = true;
                }
                return currentSeatMap.get(j + offset);
            }
        }

        return null;
    }

    public int getPriceByType(GSeriesSeatType type, int startStationIndex, int endStationIndex){
        int res = 0;
        int stationNum = endStationIndex - startStationIndex;
        switch (type){
            case BUSINESS_SEAT -> res = 80 * stationNum;
            case FIRST_CLASS_SEAT -> res = 60 * stationNum;
            case SECOND_CLASS_SEAT -> res = 40 * stationNum;
            case NO_SEAT -> res = 20 * stationNum;
        }
        return res;
    }

    public GSeriesSeatType getTypeBySeatName(String name){
        GSeriesSeatType seatType;
        if(name.startsWith("1车")){
            seatType = GSeriesSeatType.BUSINESS_SEAT;
        }else if(name.startsWith("2车") || name.startsWith("3车")){
            seatType = GSeriesSeatType.FIRST_CLASS_SEAT;
        }else if(name.startsWith("4车")){
            seatType = GSeriesSeatType.SECOND_CLASS_SEAT;
        }else{
            seatType = GSeriesSeatType.NO_SEAT;
        }
        return seatType;
    }

    public @Nullable void returnSeat(String name, int startStationIndex, int endStationIndex, boolean[][] seatMap){
        GSeriesSeatType seatType = getTypeBySeatName(name);
        Map<Integer, String> seatTypeMap = TYPE_MAP.get(seatType);
        int offset = getOffset(seatType);
        for(int j = 0; j < seatTypeMap.size(); j++){
            if(seatTypeMap.get(j + offset).equals(name)){
                for(int i = startStationIndex; i < endStationIndex; i++){
                    seatMap[i][j + offset] = false;
                }
                break;
            }
        }
    }

    private int getOffset(GSeriesSeatType type) {
        int offset = 0;

        switch (type) {
            case BUSINESS_SEAT:
                break;
            case FIRST_CLASS_SEAT:
                offset += BUSINESS_SEAT_MAP.size();
                break;
            case SECOND_CLASS_SEAT:
                offset += BUSINESS_SEAT_MAP.size() + FIRST_CLASS_SEAT_MAP.size();
                break;
            case NO_SEAT:
                offset += BUSINESS_SEAT_MAP.size() + FIRST_CLASS_SEAT_MAP.size() + SECOND_CLASS_SEAT_MAP.size();
                break;
        }

        return offset;
    }


    public Map<GSeriesSeatType, Integer> getLeftSeatCount(int startStationIndex, int endStationIndex, boolean[][] seatMap) {
        Map<GSeriesSeatType, Integer> leftSeatCount = new HashMap<>();

        for (GSeriesSeatType seatType : TYPE_MAP.keySet()) {
            Map<Integer, String> seatTypeMap = TYPE_MAP.get(seatType);
            int offset = getOffset(seatType);

            int count = 0;
            for (int j = 0; j < seatTypeMap.size(); j++) {
                boolean available = true;
                for (int i = startStationIndex; i < endStationIndex; i++) {
                    if (seatMap[i][j + offset]) {
                        available = false;
                        break;
                    }
                }
                if (available) {
                    count++;
                }
            }

            leftSeatCount.put(seatType, count);
        }

        return leftSeatCount;
    }


/*        public Map<GSeriesSeatType, Integer> getLeftSeatCount(int startStationIndex, int endStationIndex, boolean[][] seatMap) {
            Map<GSeriesSeatType, Integer> leftSeatCount = new HashMap<>();
            int count1 = 0, count2 = 0, count3 = 0;

            int offset = 0;
            for(int j = 0; j < BUSINESS_SEAT_MAP.size(); j++) {
                for (int i = startStationIndex; i < endStationIndex; i++) {
                    if (seatMap[i][j + offset])
                        break;
                    if (i == endStationIndex - 1)
                        count1++;
                }
            }

            offset += BUSINESS_SEAT_MAP.size();
            for(int j = 0; j < FIRST_CLASS_SEAT_MAP.size(); j++){
                for (int i = startStationIndex; i < endStationIndex; i++) {
                    if (seatMap[i][j + offset])
                        break;
                    if (i == endStationIndex - 1)
                        count2++;
                }
            }

            offset += FIRST_CLASS_SEAT_MAP.size();
            for(int j = 0; j < SECOND_CLASS_SEAT_MAP.size(); j++){
                for (int i = startStationIndex; i < endStationIndex; i++) {
                    if (seatMap[i][j + offset])
                        break;
                    if (i == endStationIndex - 1)
                        count3++;
                }
            }

            leftSeatCount.put(GSeriesSeatType.BUSINESS_SEAT,count1);
            leftSeatCount.put(GSeriesSeatType.FIRST_CLASS_SEAT, count2);
            leftSeatCount.put(GSeriesSeatType.SECOND_CLASS_SEAT, count3);
            return leftSeatCount;
        }*/




    public boolean[][] initSeatMap(int stationCount) {
        return new boolean[stationCount - 1][BUSINESS_SEAT_MAP.size() + FIRST_CLASS_SEAT_MAP.size()
                + SECOND_CLASS_SEAT_MAP.size() + NO_SEAT_MAP.size()];
    }
}
