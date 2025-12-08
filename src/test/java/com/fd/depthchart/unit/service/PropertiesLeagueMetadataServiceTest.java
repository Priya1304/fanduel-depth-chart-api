package com.fd.depthchart.unit.service;

import com.fd.depthchart.config.LeagueCatalogProperties;
import com.fd.depthchart.service.PropertiesLeagueMetadataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PropertiesLeagueMetadataServiceTest {
    private PropertiesLeagueMetadataService service;

    @BeforeEach
    void setUp() {
        LeagueCatalogProperties.League nfl = new LeagueCatalogProperties.League();
        nfl.setTeams(Set.of("TB", "NE"));
        nfl.setPositions(Set.of("QB", "WR"));

        LeagueCatalogProperties props = new LeagueCatalogProperties();
        props.setLeagues(Map.of("NFL", nfl));

        service = new PropertiesLeagueMetadataService(props);
    }

    @Test
    @DisplayName("validateLeague should pass for known league")
    void validateLeague_valid() {
        service.validateLeague("NFL");
    }

    @Test
    @DisplayName("validateLeague should throw for unknown league")
    void validateLeague_invalid() {
        assertThrows(IllegalArgumentException.class, () -> service.validateLeague("NBA"));
    }

    @Test
    void validateLeagueTeam_acceptsKnownTeam() {
        service.validateLeagueTeam("NFL", "tb");
    }

    @Test
    void validateLeagueTeam_rejectsUnknownTeam() {
        assertThrows(IllegalArgumentException.class, () -> service.validateLeagueTeam("NFL", "XYZ"));
    }

    @Test
    void validateLeagueTeamPosition_acceptsKnownPosition() {
        service.validateLeagueTeamPosition("NFL", "TB", "qb");
    }

    @Test
    void validateLeagueTeamPosition_rejectsUnknownPosition() {
        assertThrows(IllegalArgumentException.class, () -> service.validateLeagueTeamPosition("NFL", "TB", "RB"));
    }
}
