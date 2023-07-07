package org.fffd.l23o6.util.map;

import org.springframework.data.util.Pair;

import java.util.HashMap;
import java.util.Map;

// 默认两个相邻站点只可能有一个距离，在创建 route 的时候更新 map
// map 保存相邻站点之间的距离
public class stationMap {
    private Map<Pair<Long, Long>, Integer> map = new HashMap<>();

    // TODO distance 需要与前端交互
    public void setDistance(Long stationIdA, Long stationIdB, int distance){
        Pair<Long, Long> pair = Pair.of(stationIdA, stationIdB);
        map.put(pair, distance);
    }

    public int getDistance(Long stationIdA, Long stationIdB){
        Pair<Long, Long> pair = Pair.of(stationIdA, stationIdB);
        if(map.get(pair) == null)
            pair = Pair.of(stationIdB, stationIdA);
        return map.get(pair);
    }
}
