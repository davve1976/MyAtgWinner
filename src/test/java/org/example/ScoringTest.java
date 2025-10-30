package org.example;

import org.example.analysis.ScoreCalculator;
import org.example.domain.*;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mycket enkel sanity check:
 * kusk 5 + bra form ska slå kusk 2 + dålig form.
 */
public class ScoringTest {

    @Test
    void strongerDriverAndFormShouldScoreHigher() {
        Driver pro = new Driver("Top Driver", 5);
        Driver meh = new Driver("Random", 2);

        Horse goodHorse = new Horse(
                "Good Horse",
                "Trainer X",
                pro,
                List.of(
                        new RaceResult("Solvalla",2140,4,1,75000,false),
                        new RaceResult("Romme",2140,5,2,75500,false)
                )
        );

        Horse badHorse = new Horse(
                "Bad Horse",
                "Trainer Y",
                meh,
                List.of(
                        new RaceResult("Bollnäs",2140,11,7,78000,false),
                        new RaceResult("Eskilstuna",2140,9,8,80000,true)
                )
        );

        Entry e1 = new Entry(4, goodHorse);
        Entry e2 = new Entry(11, badHorse);

        ScoreCalculator calc = new ScoreCalculator();
        double s1 = calc.scoreEntry(e1);
        double s2 = calc.scoreEntry(e2);

        assertTrue(s1 > s2, "Expected goodHorse to score higher than badHorse");
    }
}
