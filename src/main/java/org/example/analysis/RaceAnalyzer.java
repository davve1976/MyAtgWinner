package org.example.analysis;

import org.example.domain.Entry;
import org.example.domain.RaceToAnalyze;

import java.util.Comparator;
import java.util.List;

/**
 * Rankar ett lopp baserat på ScoreCalculator.
 */
public class RaceAnalyzer {

    private final ScoreCalculator calc = new ScoreCalculator();

    public List<EntryScore> rankRace(RaceToAnalyze race) {
        return race.starters().stream()
                .map(e -> new EntryScore(e, calc.scoreEntry(e)))
                .sorted(Comparator.comparingDouble(EntryScore::score).reversed())
                .toList();
    }

    /**
     * Ekipage + score.
     */
    public record EntryScore(Entry entry, double score) {}
}
