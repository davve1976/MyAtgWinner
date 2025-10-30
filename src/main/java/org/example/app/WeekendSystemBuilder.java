package org.example.app;

import org.example.analysis.RaceAnalyzer;
import org.example.analysis.RaceAnalyzer.EntryScore;
import org.example.domain.*;

import java.util.List;
import java.util.Map;

public class WeekendSystemBuilder {

    public static void main(String[] args) {
        // === 1. Läs in kuskbetyg ===
        Map<String, Driver> driverMap = DriverLoader.loadDrivers();
        System.out.println("Läste in " + driverMap.size() + " kuskar från drivers.json");

        // === 2. Hämta kuskar med lookup ===
        Driver kilstrom = DriverLoader.find(driverMap, "Örjan Kihlström");
        Driver goop     = DriverLoader.find(driverMap, "Björn Goop");
        Driver random   = DriverLoader.find(driverMap, "Olle Okänd");

        // Resten av koden som tidigare...
        // 2. Bygg hästar
        Horse horse1 = new Horse(
                "Mighty Turbo",
                "Tränare A",
                kilstrom,
                List.of(
                        new RaceResult("Solvalla", 2140, 4, 1, 75000, false),
                        new RaceResult("Romme",    2140, 5, 2, 75500, false),
                        new RaceResult("Åby",      1640, 2, 5, 73000, true)
                )
        );

        Horse horse2 = new Horse(
                "Dark Rocket",
                "Tränare B",
                goop,
                List.of(
                        new RaceResult("Solvalla", 2140, 8, 3, 76000, false),
                        new RaceResult("Färjestad",2140, 3, 6, 77000, false)
                )
        );

        Horse horse3 = new Horse(
                "Budget Häst",
                "Tränare C",
                random,
                List.of(
                        new RaceResult("Bollnäs",    2140, 11, 7, 78000, false),
                        new RaceResult("Eskilstuna", 2140, 9, 8, 80000, true)
                )
        );

        // 3. Bana och lopp
        Track solvalla = new Track("Solvalla", 196, false);

        RaceToAnalyze v86_1 = new RaceToAnalyze(
                1,
                solvalla,
                2140,
                true,
                List.of(
                        new Entry(4, horse1),
                        new Entry(8, horse2),
                        new Entry(11, horse3)
                )
        );

        RaceCard card = new RaceCard(
                "V86",
                "2025-10-18",
                List.of(v86_1) // här kan du stoppa in fler RaceToAnalyze för alla lopp
        );

        // 4. Analysera första loppet
        RaceAnalyzer analyzer = new RaceAnalyzer();
        List<EntryScore> ranking = analyzer.rankRace(card.races().get(0));

        // 5. Skriv ut ranking
        System.out.println("=== Ranking för " + card.gameType() + " " + card.date() +
                ", lopp " + v86_1.raceNumber() + " på " + v86_1.track().name() + " ===");

        for (EntryScore es : ranking) {
            Entry e = es.entry();
            Horse h = e.horse();
            Driver d = h.driver();

            System.out.printf(
                    "Spår %2d | %-14s | Kusk %-18s (%d/5) | Score %.2f%n",
                    e.startNumber(),
                    h.name(),
                    d.name(),
                    d.rating(),
                    es.score()
            );
        }

        System.out.println("\nTips:");
        System.out.println("- Spik?: ta #1 i rank om den har tydligt högre score än #2");
        System.out.println("- Gardera?: om topp 3 ligger nära varandra i score\n");
    }
}
