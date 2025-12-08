package com.fd.depthchart.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Set;


@ConfigurationProperties(prefix = "league-catalog")
public class LeagueCatalogProperties {

    /**
     * Key: League name (e.g., NFL, NBA)
     * Value: league metadata (teams + positions)
     */
    private Map<String, League> leagues = Map.of();

    public Map<String, League> getLeagues() {
        return leagues;
    }

    public void setLeagues(Map<String, League> leagues) {
        this.leagues = leagues;
    }

    public static class League {
        private Set<String> teams = Set.of();;
        private Set<String> positions = Set.of();;

        public Set<String> getTeams() {
            return teams;
        }

        public void setTeams(Set<String> teams) {
            this.teams = teams;
        }

        public Set<String> getPositions() {
            return positions;
        }

        public void setPositions(Set<String> positions) {
            this.positions = positions;
        }
    }
}
