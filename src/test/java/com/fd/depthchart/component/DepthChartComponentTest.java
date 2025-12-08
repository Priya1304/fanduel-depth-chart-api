package com.fd.depthchart.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fd.depthchart.model.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DepthChartComponentTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    @DisplayName("Component: add player and fetch full depth chart")
    void addPlayer_and_getFullChart() throws Exception {

        Player player = new Player(12, "Tom Brady");

        // Add player
        mockMvc.perform(post("/api/v1/NFL/teams/TB/depth-chart/QB")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(player)))
                .andExpect(status().isCreated());

        // Verify depth chart contains the player at QB
        mockMvc.perform(get("/api/v1/NFL/teams/TB/depth-chart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.QB[0].number").value(12))
                .andExpect(jsonPath("$.QB[0].name").value("Tom Brady"));
    }
}