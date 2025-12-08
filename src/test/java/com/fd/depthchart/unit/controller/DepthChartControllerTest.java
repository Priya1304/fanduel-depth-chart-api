package com.fd.depthchart.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fd.depthchart.controller.DepthChartController;
import com.fd.depthchart.model.DepthChartKey;
import com.fd.depthchart.model.Player;
import com.fd.depthchart.service.DepthChartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DepthChartController.class)
public class DepthChartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepthChartService depthChartService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("TC: Add player returns 201 and delegates to service")
    void addPlayerToDepthChart_shouldReturnsCreated() throws Exception {
        // given
        Player player = new Player(12, "Tom Brady");

        // when
        mockMvc.perform(post("/api/v1/{league}/teams/{team}/depth-chart/{position}",
                        "NFL", "TB", "QB")
                        .queryParam("position_depth", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(player)))
                // then
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "/api/v1/NFL/teams/TB/depth-chart/QB"));

        // also verify that service is called
        verify(depthChartService)
                .addPlayerToDepthChart(any(), eq(player), eq(0));
    }

    @Test
    @DisplayName("TC: Add player returns 400, when service throws IllegalArgumentException ")
    void addPlayerToDepthChart_invalidLeague_shouldReturnsBadRequest() throws Exception {
        // given
        Player player = new Player(12, "Tom Brady");

        // service throws validation error
        doThrow(new IllegalArgumentException("Unsupported league: xxx"))
                .when(depthChartService)
                .addPlayerToDepthChart(any(), eq(player), eq(0));

        // when and then
        mockMvc.perform(post("/api/v1/{league}/teams/{team}/depth-chart/{position}",
                        "xxx", "tb", "qb")
                        .queryParam("position_depth", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(player)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported league: xxx"));

        verify(depthChartService)
                .addPlayerToDepthChart(any(), eq(player), eq(0));
    }


    @Test
    @DisplayName("TC: Full depth chart returns 200 with positions and players")
    void getFullDepthChart_shouldReturnsChart() throws Exception {
        // given
        Map<String, List<Player>> chart = Map.of(
                "QB", List.of(new Player(12, "Tom Brady")),
                "WR", List.of(new Player(13, "Mike Evans"))
        );

        when(depthChartService.getFullDepthChart("nfl", "tb"))
                .thenReturn(chart);

        // when / then
        mockMvc.perform(get("/api/v1/{league}/teams/{team}/depth-chart",
                        "nfl", "tb"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // simple json checks
                .andExpect(jsonPath("$.QB[0].number").value(12))
                .andExpect(jsonPath("$.QB[0].name").value("Tom Brady"))
                .andExpect(jsonPath("$.WR[0].number").value(13))
                .andExpect(jsonPath("$.WR[0].name").value("Mike Evans"));

        verify(depthChartService).getFullDepthChart("nfl", "tb");
    }

    @Test
    @DisplayName("TC: Remove player and return the player")
    void removePlayer_shouldReturnRemovedPlayer() throws Exception {
        DepthChartKey key = DepthChartKey.of("NFL", "TB", "QB");
        Player player = new Player(12, "Tom Brady");

        when(depthChartService.removePlayerFromDepthChart(eq(key), any(Player.class)))
                .thenReturn(List.of(player));

        mockMvc.perform(delete("/api/v1/NFL/teams/TB/depth-chart/QB")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "number": 12,
                              "name": "Tom Brady"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value(12))
                .andExpect(jsonPath("$[0].name").value("Tom Brady"));
    }

    @Test
    @DisplayName("TC: Remove player, returns empty list when player is not found")
    void removePlayer_whenPlayerNotFound_shouldReturnEmptyList() throws Exception {
        DepthChartKey key = DepthChartKey.of("NFL", "TB", "QB");

        when(depthChartService.removePlayerFromDepthChart(eq(key), any(Player.class)))
                .thenReturn(List.of());

        mockMvc.perform(delete("/api/v1/NFL/teams/TB/depth-chart/QB")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "number": 99,
                              "name": "Unknown"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("TC: Get backup, returns backup players")
    void getBackups_shouldReturnBackupPlayers() throws Exception {
        DepthChartKey key = DepthChartKey.of("NFL", "TB", "QB");
        List<Player> backups = List.of(
                new Player(6, "Backup One"),
                new Player(7, "Backup Two")
        );

        when(depthChartService.getBackups(eq(key), any(Player.class)))
                .thenReturn(backups);

        mockMvc.perform(post("/api/v1/NFL/teams/TB/depth-chart/QB/backups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "number": 12,
                              "name": "Tom Brady"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value(6))
                .andExpect(jsonPath("$[0].name").value("Backup One"))
                .andExpect(jsonPath("$[1].number").value(7))
                .andExpect(jsonPath("$[1].name").value("Backup Two"));
    }

    @Test
    @DisplayName("TC: Get backup, returns empty list")
    void getBackups_whenNoBackups_shouldReturnEmptyList() throws Exception {
        DepthChartKey key = DepthChartKey.of("NFL", "TB", "QB");

        when(depthChartService.getBackups(eq(key), any(Player.class)))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/v1/NFL/teams/TB/depth-chart/QB/backups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "number": 12,
                              "name": "Tom Brady"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

}
