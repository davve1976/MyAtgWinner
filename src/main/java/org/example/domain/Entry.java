package org.example.domain;

/**
 * En start i ett specifikt lopp: hästen X från spår Y.
 */
public record Entry(
        int startNumber,
        Horse horse
) {}
