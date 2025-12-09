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

    private final Map<DepthChartKey, List<Player>> depthChart = new LinkedHashMap<>();

    public DepthChartServiceImpl(LeagueMetadataService leagueMetadataService) {
        this.leagueMetadataService = leagueMetadataService;
    }

    /**
     * Adds player to the depth chart.
     * If the positionDepth is missing the player is added to the end of the depth chart.
     */
    @Override
    public synchronized void addPlayerToDepthChart(DepthChartKey key,
                                                   Player player,
                                                   Integer positionDepth) {
        validatePositionDepth(positionDepth);

        // Domain validation delegated to metadata service
        leagueMetadataService.validateLeagueTeamPosition(
                key.league(), key.team(), key.position()
        );

        validatePlayer(player);

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

    /**
     *  Removes a player from the depth chart for a given position and returns that player
     *  An empty list should be returned if the player is not listed in the depth chart at that position
     */
    @Override
    public synchronized List<Player> removePlayerFromDepthChart(DepthChartKey key, Player player) {
        PlayerIndex pi = lookupPlayer(key, player);
        if (pi == null) {
            return List.of();
        }

        Player removed = pi.depth().remove(pi.index());
        if (pi.depth().isEmpty()) {
            depthChart.remove(key);
        }

        return List.of(removed);
    }

    /**
     * Returns all backup players for the specified player at a position.
     */
    @Override
    public synchronized List<Player> getBackups(DepthChartKey key, Player player) {
        PlayerIndex pi = lookupPlayer(key, player);
        if (pi == null) {
            return List.of();
        }

        if (pi.index() + 1 >= pi.depth().size()) {
            return List.of();
        }

        return List.copyOf(pi.depth().subList(pi.index() + 1, pi.depth().size()));
    }

    /**
     * Returns the full depth chart for a league/team across all positions.
     */
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


    /**
     * Look up depth list for given key and find player's index.
     * Returns null if position not present or player not found.
     */
    private PlayerIndex lookupPlayer(DepthChartKey key, Player player) {
        requireNonNull(key, "key");
        requireNonNull(player, "player");

        leagueMetadataService.validateLeagueTeamPosition(
                key.league(), key.team(), key.position());

        List<Player> depth = depthChart.get(key);
        if (depth == null || depth.isEmpty()) {
            return null;
        }

        int index = depth.indexOf(player);
        if (index == -1) {
            return null;
        }

        return new PlayerIndex(depth, index);
    }

    private record PlayerIndex(List<Player> depth, int index) {}

    // Validations

    private void validatePlayer(Player player) {
        requireNonNull(player, "player");

        // Validate number
        if (player.number() <= 0) {
            throw new IllegalArgumentException("player.number is required");
        }
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
