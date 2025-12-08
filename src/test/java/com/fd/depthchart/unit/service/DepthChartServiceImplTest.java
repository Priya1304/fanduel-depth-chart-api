package com.fd.depthchart.unit.service;

import com.fd.depthchart.model.DepthChartKey;
import com.fd.depthchart.model.Player;
import com.fd.depthchart.service.DepthChartServiceImpl;
import com.fd.depthchart.service.LeagueMetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DepthChartServiceImplTest {

    @Mock
    private LeagueMetadataService leagueMetadataService;

    private DepthChartServiceImpl depthChartService;

    @BeforeEach
    void setUp() {
        depthChartService = new DepthChartServiceImpl(leagueMetadataService);
    }

    @DisplayName("addPlayerToDepthChart adds player to the end of the depth chart, when positionDepth is not given")
    @Test
    void addPlayer_appendsWhenPositionDepthIsNull() {
        DepthChartKey key = DepthChartKey.of("nfl", "tb", "qb");
        Player player = new Player(12, "Tom Brady");

        depthChartService.addPlayerToDepthChart(key, player, null);

        Map<String, List<Player>> chart = depthChartService.getFullDepthChart("nfl", "tb");
        List<Player> expected = List.of(player);

        assertIterableEquals(expected, chart.get("QB"));

        // league/team validation should be invoked at least once
        verify(leagueMetadataService, times(1))
                .validateLeagueTeamPosition("NFL", "TB", "QB");
    }

    @Test
    @DisplayName("addPlayerToDepthChart inserts at given depth and shifts others down")
    void addPlayer_insertsAtSpecificDepth() {
        DepthChartKey key = DepthChartKey.of("nfl", "tb", "wr");

        Player player1 = new Player(11, "Player One");
        Player player2 = new Player(13, "Player Two");
        Player player3 = new Player(10, "Player Three");

        // add two players
        depthChartService.addPlayerToDepthChart(key, player1, null); // index 0
        depthChartService.addPlayerToDepthChart(key, player2, null); // index 1

        // insert the player3 at depth 0, pushing others down
        depthChartService.addPlayerToDepthChart(key, player3, 0);

        Map<String, List<Player>> chart = depthChartService.getFullDepthChart("nfl", "tb");
        List<Player> wrPlayers = chart.get("WR");

        assertEquals(3, wrPlayers.size());
        assertIterableEquals(List.of(player3, player1, player2), wrPlayers);
    }





}