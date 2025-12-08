package com.fd.depthchart.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * DTO for loading a depth chart from the static JSON for in-memory data.
 * */
@Data
public class DepthChartDTO {
    private String league;
    private String team;
    private String season;
    private Map<String, List<Player>> positions;
}
