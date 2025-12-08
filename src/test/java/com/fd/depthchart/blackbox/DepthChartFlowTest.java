package com.fd.depthchart.blackbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fd.depthchart.model.Player;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class DepthChartFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addPlayers_and_getBackups() throws Exception {
        Player starter = new Player(12, "Tom Brady");
        Player backup = new Player(6, "Blaine Gabbert");

        // Add starter
        mockMvc.perform(post("/api/v1/NFL/teams/TB/depth-chart/QB")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(starter)))
                .andExpect(status().isCreated());

        // Add backup
        mockMvc.perform(post("/api/v1/NFL/teams/TB/depth-chart/QB")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(backup)))
                .andExpect(status().isCreated());

        // Get backups for Tom Brady -> expect Blaine Gabbert
        mockMvc.perform(post("/api/v1/NFL/teams/TB/depth-chart/QB/backups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(starter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value(6))
                .andExpect(jsonPath("$[0].name").value("Blaine Gabbert"));
    }

    @Test
    void addPlayers_and_removePlayer() throws Exception {
        Player starter = new Player(12, "Tom Brady");
        Player backup = new Player(6, "Blaine Gabbert");

        // Add starter
        mockMvc.perform(post("/api/v1/NFL/teams/TB/depth-chart/QB")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(starter)))
                .andExpect(status().isCreated());

        // Add backup
        mockMvc.perform(post("/api/v1/NFL/teams/TB/depth-chart/QB")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(backup)))
                .andExpect(status().isCreated());

        // Remove Tom Brady -> response should contain removed player
        mockMvc.perform(delete("/api/v1/NFL/teams/TB/depth-chart/QB")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(starter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value(12))
                .andExpect(jsonPath("$[0].name").value("Tom Brady"));

        // Full depth chart should now only have Blaine at QB
        mockMvc.perform(get("/api/v1/NFL/teams/TB/depth-chart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.QB[0].number").value(6))
                .andExpect(jsonPath("$.QB[0].name").value("Blaine Gabbert"));
    }
}
