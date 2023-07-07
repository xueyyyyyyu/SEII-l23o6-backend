package org.fffd.l23o6.util.strategy.train;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Nullable;


public class KSeriesSeatStrategy extends TrainSeatStrategy {
    public static final KSeriesSeatStrategy INSTANCE = new KSeriesSeatStrategy();
     
    private final Map<Integer, String> SOFT_SLEEPER_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> HARD_SLEEPER_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> SOFT_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> HARD_SEAT_MAP = new HashMap<>();
    private final Map<Integer, String> NO_SEAT_MAP = new HashMap<>();

    private final Map<KSeriesSeatType, Map<Integer, String>> TYPE_MAP = new HashMap<>() {{
        put(KSeriesSeatType.SOFT_SLEEPER_SEAT, SOFT_SLEEPER_SEAT_MAP);
        put(KSeriesSeatType.HARD_SLEEPER_SEAT, HARD_SLEEPER_SEAT_MAP);
        put(KSeriesSeatType.SOFT_SEAT, SOFT_SEAT_MAP);
        put(KSeriesSeatType.HARD_SEAT, HARD_SEAT_MAP);
        put(KSeriesSeatType.NO_SEAT, NO_SEAT_MAP);
    }};


    private KSeriesSeatStrategy() {

        int counter = 0;

        for (String s : Arrays.asList("软卧1号上铺", "软卧2号下铺", "软卧3号上铺", "软卧4号上铺", "软卧5号上铺", "软卧6号下铺", "软卧7号上铺", "软卧8号上铺")) {
            SOFT_SLEEPER_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("硬卧1号上铺", "硬卧2号中铺", "硬卧3号下铺", "硬卧4号上铺", "硬卧5号中铺", "硬卧6号下铺", "硬卧7号上铺", "硬卧8号中铺", "硬卧9号下铺", "硬卧10号上铺", "硬卧11号中铺", "硬卧12号下铺")) {
            HARD_SLEEPER_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("1车1座", "1车2座", "1车3座", "1车4座", "1车5座", "1车6座", "1车7座", "1车8座", "2车1座", "2车2座", "2车3座", "2车4座", "2车5座", "2车6座", "2车7座", "2车8座")) {
            SOFT_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("3车1座", "3车2座", "3车3座", "3车4座", "3车5座", "3车6座", "3车7座", "3车8座", "3车9座", "3车10座", "4车1座", "4车2座", "4车3座", "4车4座", "4车5座", "4车6座", "4车7座", "4车8座", "4车9座", "4车10座")) {
            HARD_SEAT_MAP.put(counter++, s);
        }

        for (String s : Arrays.asList("无座01","无座02","无座03","无座04","无座05","无座05","无座06","无座07","无座07","无座08",
                "无座09","无座10","无座11","无座12","无座13","无座14","无座15","无座16","无座17","无座18")) {
            NO_SEAT_MAP.put(counter++, s);
        }

    }

    public enum KSeriesSeatType implements SeatType {
        SOFT_SLEEPER_SEAT("软卧"), HARD_SLEEPER_SEAT("硬卧"), SOFT_SEAT("软座"), HARD_SEAT("硬座"), NO_SEAT("无座");
        private String text;
        KSeriesSeatType(String text){
            this.text=text;
        }
        public String getText() {
            return this.text;
        }
        public static KSeriesSeatType fromString(String text) {
            for (KSeriesSeatType b : KSeriesSeatType.values()) {
                if (b.text.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    public @Nullable String allocSeat(int startStationIndex, int endStationIndex, KSeriesSeatType type, boolean[][] seatMap) {
        Map<Integer, String> seatTypeMap = TYPE_MAP.get(type);
        int offset = getOffset(type);

        for (int j = 0; j < seatTypeMap.size(); j++) {
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
                return seatTypeMap.get(j + offset);
            }
        }

        return null;
    }

    public KSeriesSeatType getTypeByName(String name){
        KSeriesSeatType seatType;
        if(name.startsWith("软卧")){
            seatType = KSeriesSeatType.SOFT_SLEEPER_SEAT;
        }else if(name.startsWith("硬卧")){
            seatType = KSeriesSeatType.HARD_SLEEPER_SEAT;
        }else if(name.startsWith("1车") || name.startsWith("2车")){
            seatType = KSeriesSeatType.SOFT_SEAT;
        }else if(name.startsWith("3车") || name.startsWith("4车")){
            seatType = KSeriesSeatType.HARD_SEAT;
        }else {
            seatType = KSeriesSeatType.NO_SEAT;
        }
        return seatType;
    }

    public int getPriceByType(KSeriesSeatType type, int startStationIndex, int endStationIndex){
        int res = 0;
        int stationNum = endStationIndex - startStationIndex;
        switch (type){
            case SOFT_SLEEPER_SEAT -> res = 50 * stationNum;
            case HARD_SLEEPER_SEAT -> res = 40 * stationNum;
            case SOFT_SEAT -> res = 30 * stationNum;
            case HARD_SEAT -> res = 20 * stationNum;
            case NO_SEAT -> res = 10 * stationNum;
        }
        return res;
    }

    public @Nullable void returnSeat(String name, int startStationIndex, int endStationIndex, boolean[][] seatMap){
        KSeriesSeatType seatType = getTypeByName(name);
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


    private int getOffset(KSeriesSeatType type) {
        int offset = 0;

        switch (type) {
            case SOFT_SLEEPER_SEAT:
                break;
            case HARD_SLEEPER_SEAT:
                offset += SOFT_SLEEPER_SEAT_MAP.size();
                break;
            case SOFT_SEAT:
                offset += SOFT_SLEEPER_SEAT_MAP.size() + HARD_SLEEPER_SEAT_MAP.size();
                break;
            case HARD_SEAT:
                offset += SOFT_SLEEPER_SEAT_MAP.size() + HARD_SLEEPER_SEAT_MAP.size() + SOFT_SEAT_MAP.size();
                break;
            case NO_SEAT:
                offset += SOFT_SLEEPER_SEAT_MAP.size() + HARD_SLEEPER_SEAT_MAP.size() + SOFT_SEAT_MAP.size() + HARD_SEAT_MAP.size();
                break;
        }

        return offset;
    }



    public Map<KSeriesSeatType, Integer> getLeftSeatCount(int startStationIndex, int endStationIndex, boolean[][] seatMap) {
        Map<KSeriesSeatType, Integer> leftSeatCount = new HashMap<>();

        for (KSeriesSeatType seatType : TYPE_MAP.keySet()) {
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


    public boolean[][] initSeatMap(int stationCount) {
        return new boolean[stationCount - 1][SOFT_SLEEPER_SEAT_MAP.size() + HARD_SLEEPER_SEAT_MAP.size()
                + SOFT_SEAT_MAP.size() + HARD_SEAT_MAP.size() + NO_SEAT_MAP.size()];
    }
}
