package org.example.analysis;

import org.example.domain.Entry;
import org.example.domain.Driver;
import org.example.domain.Horse;
import org.example.domain.RaceResult;

/**
 * Räkna ut ett "styrkevärde" för ett ekipage.
 * Kan tweakas över tid.
 */
public class ScoreCalculator {

    public double scoreEntry(Entry e) {
        double driverScore = driverFactor(e.horse().driver());
        double formScore   = formFactor(e.horse());
        double postScore   = startPositionFactor(e.startNumber());

        // Viktning kan du ändra
        return driverScore * 0.5
             + formScore   * 0.4
             + postScore   * 0.1;
    }

    private double driverFactor(Driver d) {
        // Kusken: 1-5 -> 2.0-10.0
        return d.rating() * 2.0;
    }

    private double formFactor(Horse h) {
        // Enkel form: hur många topp-3 på senaste 5 starter
        int goodRuns = 0;
        int racesCounted = 0;

        for (RaceResult rr : h.lastRaces()) {
            racesCounted++;
            if (rr.finishPosition() <= 3) {
                goodRuns++;
            }
            if (racesCounted == 5) break;
        }

        return (goodRuns / 5.0) * 10.0; // 0..10
    }

    private double startPositionFactor(int startNumber) {
        // Placeholder: bättre spår = lite högre poäng.
        if (startNumber == 1) return 10.0;
        if (startNumber <= 4) return 8.0;
        if (startNumber <= 8) return 5.0;
        return 3.0;
    }
}
