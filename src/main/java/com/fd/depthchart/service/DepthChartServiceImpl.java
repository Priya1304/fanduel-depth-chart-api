package com.fd.depthchart.service;

import com.fd.depthchart.model.DepthChartKey;
import com.fd.depthchart.model.Player;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepthChartServiceImpl implements DepthChartService {

    private final LeagueMetadataService leagueMetadataService;

    // League+Team+Position -> ordered depth chart players
    private final Map<DepthChartKey, List<Player>> depthChart = new LinkedHashMap<>();

    public DepthChartServiceImpl(LeagueMetadataService leagueMetadataService) {
        this.leagueMetadataService = leagueMetadataService;
    }

    @Override
    public synchronized void addPlayerToDepthChart(DepthChartKey key,
                                                   Player player,
                                                   Integer positionDepth) {
        validatePlayer(player);
        validatePositionDepth(positionDepth);

        // Domain validation delegated to metadata service
        leagueMetadataService.validateLeagueTeamPosition(
                key.league(), key.team(), key.position()
        );

        // Get or create position depth list
        List<Player> depth = depthChart.computeIfAbsent(key, k -> new ArrayList<>());

        // Append case
        if (positionDepth == null || positionDepth >= depth.size()) {
            if (!depth.contains(player)) {
                depth.add(player);
            }
            return;
        }

        // If player already exists, remove to avoid duplicates
        int existingIndex = depth.indexOf(player);
        if (existingIndex != -1) {
            if (existingIndex == positionDepth) {
                return; // already at correct spot
            }
            depth.remove(existingIndex);
            if (existingIndex < positionDepth) {
                positionDepth--;
            }
        }

        // Insert at specific depth (shift others down)
        depth.add(positionDepth, player);
    }

    @Override
    public synchronized Map<String, List<Player>> getFullDepthChart(String league, String team) {
        String lg = normalizeRequired("league", league);
        String tm = normalizeRequired("team", team);

        leagueMetadataService.validateLeagueTeam(lg, tm);

        Map<String, List<Player>> result = new LinkedHashMap<>();

        for (Map.Entry<DepthChartKey, List<Player>> entry : depthChart.entrySet()) {
            DepthChartKey key = entry.getKey();
            if (key.league().equals(lg) && key.team().equals(tm)) {
                result.put(key.position(), new ArrayList<>(entry.getValue()));
            }
        }
        return result;
    }


    private void validatePlayer(Player player) {
        requireNonNull(player, "player");
        requireNonBlank(player.name(), "player.name");
    }

    private void validatePositionDepth(Integer depthIndex) {
        if (depthIndex != null && depthIndex < 0) {
            throw new IllegalArgumentException("position_depth must be >= 0 if specified");
        }
    }

    private void requireNonNull(Object value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    private void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    private static String normalizeRequired(String field, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim().toUpperCase();
    }
}
