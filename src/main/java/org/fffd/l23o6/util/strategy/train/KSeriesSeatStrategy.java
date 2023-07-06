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

    private final Map<KSeriesSeatType, Map<Integer, String>> TYPE_MAP = new HashMap<>() {{
        put(KSeriesSeatType.SOFT_SLEEPER_SEAT, SOFT_SLEEPER_SEAT_MAP);
        put(KSeriesSeatType.HARD_SLEEPER_SEAT, HARD_SLEEPER_SEAT_MAP);
        put(KSeriesSeatType.SOFT_SEAT, SOFT_SEAT_MAP);
        put(KSeriesSeatType.HARD_SEAT, HARD_SEAT_MAP);
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
        /*if (type.equals(KSeriesSeatType.SOFT_SLEEPER_SEAT)){

        } else if (type.equals(KSeriesSeatType.HARD_SLEEPER_SEAT)){

        } else if (type.equals(KSeriesSeatType.SOFT_SEAT)){

        } else if (type.equals(KSeriesSeatType.HARD_SEAT)){

        }*/
        return "null";
    }


    public Map<KSeriesSeatType, Integer> getLeftSeatCount(int startStationIndex, int endStationIndex, boolean[][] seatMap) {
        Map<KSeriesSeatStrategy.KSeriesSeatType, Integer> leftSeatCount = new HashMap<>();
        int count1 = 0, count2 = 0, count3 = 0, count4 = 0;

        int offset = 0;
        for(int j = 0; j < SOFT_SLEEPER_SEAT_MAP.size(); j++) {
            for (int i = startStationIndex; i < endStationIndex; i++) {
                if (seatMap[i][j + offset])
                    break;
                if (i == endStationIndex - 1)
                    count1++;
            }
        }

        offset += SOFT_SLEEPER_SEAT_MAP.size();
        for(int j = 0; j < HARD_SLEEPER_SEAT_MAP.size(); j++){
            for (int i = startStationIndex; i < endStationIndex; i++) {
                if (seatMap[i][j + offset])
                    break;
                if (i == endStationIndex - 1)
                    count2++;
            }
        }

        offset += HARD_SLEEPER_SEAT_MAP.size();
        for(int j = 0; j < SOFT_SEAT_MAP.size(); j++){
            for (int i = startStationIndex; i < endStationIndex; i++) {
                if (seatMap[i][j + offset])
                    break;
                if (i == endStationIndex - 1)
                    count3++;
            }
        }

        offset += SOFT_SEAT_MAP.size();
        for(int j = 0; j < HARD_SEAT_MAP.size(); j++){
            for (int i = startStationIndex; i < endStationIndex; i++) {
                if (seatMap[i][j + offset])
                    break;
                if (i == endStationIndex - 1)
                    count4++;
            }
        }

        leftSeatCount.put(KSeriesSeatType.SOFT_SLEEPER_SEAT,count1);
        leftSeatCount.put(KSeriesSeatType.HARD_SLEEPER_SEAT, count2);
        leftSeatCount.put(KSeriesSeatType.SOFT_SEAT, count3);
        leftSeatCount.put(KSeriesSeatType.HARD_SEAT, count4);
        return leftSeatCount;
    }

    public boolean[][] initSeatMap(int stationCount) {
        return new boolean[stationCount - 1][SOFT_SLEEPER_SEAT_MAP.size() + HARD_SLEEPER_SEAT_MAP.size() + SOFT_SEAT_MAP.size() + HARD_SEAT_MAP.size()];
    }
}
