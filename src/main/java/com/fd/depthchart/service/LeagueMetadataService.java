package com.fd.depthchart.service;

public interface LeagueMetadataService {
    void validateLeague(String league);

    void validateLeagueTeam(String league, String team);

    void validateLeagueTeamPosition(String league, String team, String position);

}
