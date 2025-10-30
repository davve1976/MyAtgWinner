package org.example.domain;

import java.util.List;

/**
 * Hela omgången, t.ex. "V86 2025-10-18".
 */
public record RaceCard(
        String gameType,
        String date,
        List<RaceToAnalyze> races
) {}
