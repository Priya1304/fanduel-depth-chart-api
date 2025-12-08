package com.fd.depthchart.service;

import com.fd.depthchart.model.DepthChartKey;
import com.fd.depthchart.model.Player;

import java.util.List;
import java.util.Map;

public interface DepthChartService {

    void addPlayerToDepthChart (DepthChartKey key, Player player, Integer position_depth);

    List<Player> removePlayerFromDepthChart(DepthChartKey key, Player player);

    List<Player> getBackups(DepthChartKey key, Player player);

    Map<String, List<Player>> getFullDepthChart(String league, String team);
}
