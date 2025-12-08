package com.fd.depthchart.controller;

import com.fd.depthchart.model.DepthChartKey;
import com.fd.depthchart.model.Player;
import com.fd.depthchart.service.DepthChartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Depth Chart", description = "Manage team depth charts")
@Slf4j
public class DepthChartController {

    private final DepthChartService depthChartService;

    @Operation(
            summary = "Add a player to depth chart",
            description = "Adds a player at the given position. If position_depth is not provided, "
                    + "the player is added to the end of the depth chart."
    )
    @PostMapping("/{league}/teams/{team}/depth-chart/{position}")
    public ResponseEntity<Void> addPlayerToDepthChart(
            @Parameter(description = "League code (e.g. NFL)") @PathVariable String league,
            @Parameter(description = "Team code within the league (e.g. TB)") @PathVariable String team,
            @Parameter(description = "Position code (e.g. QB)") @PathVariable String position,
            @RequestBody Player player,
            @Parameter(description = "Optional depth index (0 = starter)") @RequestParam(value = "position_depth", required = false) Integer positionDepth) {

        String correlationId = UUID.randomUUID().toString();
        log.info("[{}] Received request for addPlayer: league={}, team={}, position={}",
                correlationId, league, team, position);
        DepthChartKey key = DepthChartKey.of(league, team, position);

        depthChartService.addPlayerToDepthChart(key, player, positionDepth);

        return ResponseEntity
                .created(URI.create(String.format(
                        "/api/v1/%s/teams/%s/depth-chart/%s",
                        key.league(), key.team(), key.position()
                )))
                .header("X-Correlation-Id", correlationId)
                .build();
    }

    @Operation(
            summary = "Remove a player from a depth chart position",
            description = "Returns the removed player in a list, or empty list if not present"
    )
    @DeleteMapping("/{league}/teams/{team}/depth-chart/{position}")
    public ResponseEntity<List<Player>> removePlayerFromDepthChart(
            @Parameter(description = "League code (e.g. NFL)") @PathVariable String league,
            @Parameter(description = "Team code within the league (e.g. TB)") @PathVariable String team,
            @Parameter(description = "Position code (e.g. QB)") @PathVariable String position,
            @RequestBody Player player) {

        String correlationId = UUID.randomUUID().toString();
        log.info("[{}] Received request for removePlayerFromDepthChart : league={}, team={}",
                correlationId, league, team);

        DepthChartKey key = DepthChartKey.of(league, team, position);

        List<Player> removed = depthChartService.removePlayerFromDepthChart(key, player);

        return ResponseEntity.ok()
                .header("X-Correlation-Id", correlationId)
                .body(removed);
    }

    @Operation(
            summary = "Get backups for a player",
            description = "Returns all players ranked below the given player at the specified position. "
                    + "If the player is not listed or has no backups, an empty list is returned."
    )
    @PostMapping("/{league}/teams/{team}/depth-chart/{position}/backups")
    public ResponseEntity<List<Player>> getBackups(
            @Parameter(description = "League code (e.g. NFL)") @PathVariable String league,
            @Parameter(description = "Team code within the league (e.g. TB)") @PathVariable String team,
            @Parameter(description = "Position code (e.g. QB)")  @PathVariable String position,
            @RequestBody Player player) {

        String correlationId = UUID.randomUUID().toString();

        log.info("[{}] Get backups: league={}, team={}, position={}",
                correlationId, league, team, position);

        DepthChartKey key = DepthChartKey.of(league, team, position);

        List<Player> backups = depthChartService.getBackups(key, player);


        return ResponseEntity.ok()
                .header("X-Correlation-Id", correlationId)
                .body(backups);
    }

    @Operation(
            summary = "Get full depth chart for a team",
            description = "Returns all positions and players for the given league and team."
    )
    @GetMapping("/{league}/teams/{team}/depth-chart")
    public ResponseEntity<Map<String, List<Player>>> getFullDepthChart(
            @Parameter(description = "League code (e.g. NFL)") @PathVariable String league,
            @Parameter(description = "Team code within the league (e.g. TB)") @PathVariable String team) {

        String correlationId = UUID.randomUUID().toString();
        log.info("[{}] Received request for getFullDepthChart: league={}, team={}",
                correlationId, league, team);
        Map<String, List<Player>> fullChart =
                depthChartService.getFullDepthChart(league, team);

        return ResponseEntity.ok()
                .header("X-Correlation-Id", correlationId)
                .body(fullChart);
    }
}
