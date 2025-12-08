package com.fd.depthchart.model;

public record DepthChartKey(String league, String team, String position) {

    public static DepthChartKey of(String league, String team, String position) {
        return new DepthChartKey(
                normalizeRequired("league", league),
                normalizeRequired("team", team),
                normalizeRequired("position", position)
        );
    }

    private static String normalizeRequired(String field, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim().toUpperCase();
    }
}