package com.fd.depthchart.service;

import com.fd.depthchart.config.LeagueCatalogProperties;
import org.springframework.stereotype.Service;

/**
 * Validates league / team / position metadata using configuration-based catalog.
 */
@Service
public class PropertiesLeagueMetadataService implements LeagueMetadataService{

    private static final String FIELD_LEAGUE = "league";
    private static final String FIELD_TEAM = "team";
    private static final String FIELD_POSITION = "position";

    private final LeagueCatalogProperties props;

    public PropertiesLeagueMetadataService(LeagueCatalogProperties props) {
        this.props = props;
    }

    @Override
    public void validateLeague(String league) {
        getLeagueDataOrThrow(league);
    }

    @Override
    public void validateLeagueTeam(String league, String team) {
        var leagueData = getLeagueDataOrThrow(league);
        String normalizedTeam = normalize(team, FIELD_TEAM);

        if (!leagueData.getTeams().contains(normalizedTeam)) {
            throw new IllegalArgumentException("Invalid team: " + normalizedTeam);
        }
    }

    @Override
    public void validateLeagueTeamPosition(String league, String team, String position) {
        var leagueData = getLeagueDataOrThrow(league);

        String normalizedTeam = normalize(team, FIELD_TEAM);
        if (!leagueData.getTeams().contains(normalizedTeam)) {
            throw new IllegalArgumentException("Invalid team: " + normalizedTeam);
        }

        String normalizedPosition = normalize(position, FIELD_POSITION);
        if (!leagueData.getPositions().contains(normalizedPosition)) {
            throw new IllegalArgumentException("Invalid position: " + normalizedPosition);
        }
    }

    private LeagueCatalogProperties.League getLeagueDataOrThrow(String league) {
        String normalizedLeague = normalize(league, FIELD_LEAGUE);

        var leagueData = props.getLeagues().get(normalizedLeague);
        if (leagueData == null) {
            throw new IllegalArgumentException("Unsupported league: " + normalizedLeague);
        }
        return leagueData;
    }

    private LeagueCatalogProperties.League getLeagueDataOrNull(String league) {
        String normalizedLeague = normalize(league, FIELD_LEAGUE);
        return props.getLeagues().get(normalizedLeague);
    }

    private String normalize(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim().toUpperCase();
    }

}
