package org.example.domain;

/**
 * Ett historiskt lopp för en häst.
 */
public record RaceResult(
        String track,        // t.ex. "Solvalla"
        int distanceMeters,  // 2140, 1640...
        int startPosition,   // spår
        int finishPosition,  // placering i mål
        long timeInMs,       // tid normaliserad om du vill jämföra form
        boolean gallop       // om hästen galopperade
) {}
