package com.fd.depthchart.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fd.depthchart.model.DepthChartDTO;
import com.fd.depthchart.model.DepthChartKey;
import com.fd.depthchart.model.Player;
import com.fd.depthchart.service.DepthChartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepthChartDataLoader implements CommandLineRunner {

    private final DepthChartService depthChartService;
    private final ObjectMapper objectMapper;

    // e.g. src/main/resources/data/nfl/tb_2022.json
    @Value("classpath:data/*/*.json")
    private Resource[] depthChartResources;

    @Override
    public void run(String... args) {
        if (depthChartResources == null || depthChartResources.length == 0) {
            log.info("No depth chart JSON files found under classpath:data/*/*.json");
            return;
        }

        for (Resource resource : depthChartResources) {
            try {
                loadFile(resource);
            } catch (Exception ex) {
                log.error("Failed to load depth chart from {}", resource.getFilename(), ex);
            }
        }
    }

    private void loadFile(Resource resource) throws IOException {
        log.info("Loading depth chart from {}", resource.getFilename());

        DepthChartDTO dto = objectMapper.readValue(
                resource.getInputStream(),
                DepthChartDTO.class
        );

        String league = dto.getLeague();
        String team = dto.getTeam();
        Map<String, List<Player>> positions = dto.getPositions();

        if (league == null || team == null || positions == null || positions.isEmpty()) {
            log.warn("Skipping {} – missing league/team/positions", resource.getFilename());
            return;
        }

        int totalPlayers = 0;

        for (Map.Entry<String, List<Player>> entry : positions.entrySet()) {
            String position = entry.getKey();
            List<Player> players = entry.getValue();
            if (players == null || players.isEmpty()) {
                continue;
            }

            DepthChartKey key = DepthChartKey.of(league, team, position);

            for (int depthIndex = 0; depthIndex < players.size(); depthIndex++) {
                Player player = players.get(depthIndex);
                try {
                    depthChartService.addPlayerToDepthChart(key, player, depthIndex);
                    totalPlayers++;
                } catch (IllegalArgumentException ex) {
                    log.warn(
                            "Skipping player {} at {} depth {} in {} due to validation error: {}",
                            player,
                            position,
                            depthIndex,
                            resource.getFilename(),
                            ex.getMessage()
                    );
                }
            }
        }

        log.info("Loaded depth chart for {}-{} from {}: {} positions, {} players",
                league, team, resource.getFilename(), positions.size(), totalPlayers);
    }
}