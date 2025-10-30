package org.example.domain;

import java.util.List;

/**
 * Häst med kusk och senaste form.
 */
public record Horse(
        String name,
        String trainer,
        Driver driver,
        List<RaceResult> lastRaces
) {}
