package org.example.domain;

import java.util.List;

/**
 * Ett lopp i omgången, t.ex. V86-1.
 */
public record RaceToAnalyze(
        int raceNumber,
        Track track,
        int distanceMeters,
        boolean autoStart,
        java.util.List<Entry> starters
) {}
