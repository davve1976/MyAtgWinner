package org.example.domain;

/**
 * Bana, för ev. banprofil/upplopp.
 */
public record Track(
        String name,
        int stretchLengthMeters,
        boolean isTightTrack
) {}
