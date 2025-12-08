package com.fd.depthchart.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fd.depthchart.controller.DepthChartController;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void addPlayerToDepthChart_returnsCreated() throws Exception {
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
    void addPlayerToDepthChart_invalidLeague_returnsBadRequest() throws Exception {
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
    void getFullDepthChart_returnsChart() throws Exception {
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
}
