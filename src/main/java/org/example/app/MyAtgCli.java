package org.example.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.analysis.RaceAnalyzer;
import org.example.analysis.RaceAnalyzer.EntryScore;
import org.example.atg.AtgParser;
import org.example.domain.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * CLI för MyAtgWinner
 *
 * Kommandon:
 *
 *   --list-drivers
 *       Visa alla kuskar och deras rating (drivers.json)
 *
 *   --convert <SPELFORM> <RÅFIL.json>
 *       Konvertera ATG:s rådata (V4/V5/V64/V65/V75/V86 ...) till vårt RaceCard-format.
 *       Ex:
 *         --convert V86 v86-2025-10-29.json
 *         --convert V64 v64-2025-11-02.json
 *         --convert V75 v75-2025-11-02.json
 *
 *       Skriver ut converted-<SPELFORM>-<DATUM>.json
 *
 *   --analyze <RaceCard.json>
 *       Kör vår ranking på en RaceCard-fil som har fält:
 *         gameType, date, races[] { raceNumber, track, starters[] { horse{driver{rating}}}}
 */
public class MyAtgCli {

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        try {
            switch (args[0]) {

                case "--list-drivers" -> listDrivers();

                case "--convert" -> {
                    if (args.length < 3) {
                        System.err.println("Använd: --convert <SPELFORM> <FIL.json>");
                        System.err.println("Ex: --convert V86 v86-2025-10-29.json");
                        return;
                    }
                    String gameType = args[1];    // V86, V75, V64, V65, V5, V4, osv
                    String fileName = args[2];    // t.ex. v86-2025-10-29.json
                    AtgParser.convert(gameType, fileName);
                }

                case "--analyze" -> {
                    if (args.length < 2) {
                        System.err.println("Ange fil, ex: --analyze converted-V86-2025-10-29.json");
                        return;
                    }
                    analyzeFile(args[1]);
                }

                default -> printHelp();
            }
        } catch (Exception e) {
            System.err.println("Fel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printHelp() {
        System.out.println("""
                Användning:

                  Lista kuskbetyg (din drivers.json):
                    java -cp target/my-atg-winner-1.0-SNAPSHOT.jar org.example.app.MyAtgCli --list-drivers

                  Konvertera ATG:s rådata (sparad från DevTools/Network) till internt RaceCard-format:
                    java -cp target/my-atg-winner-1.0-SNAPSHOT.jar org.example.app.MyAtgCli --convert V86 v86-2025-10-29.json
                    java -cp target/my-atg-winner-1.0-SNAPSHOT.jar org.example.app.MyAtgCli --convert V64 v64-2025-11-02.json
                    java -cp target/my-atg-winner-1.0-SNAPSHOT.jar org.example.app.MyAtgCli --convert V65 v65-2025-10-31.json
                    java -cp target/my-atg-winner-1.0-SNAPSHOT.jar org.example.app.MyAtgCli --convert V4  v4-2025-11-03.json
                    java -cp target/my-atg-winner-1.0-SNAPSHOT.jar org.example.app.MyAtgCli --convert V5  v5-2025-11-03.json
                    java -cp target/my-atg-winner-1.0-SNAPSHOT.jar org.example.app.MyAtgCli --convert V75 v75-2025-11-02.json

                  (Kommandot skriver ut converted-<SPELFORM>-<DATUM>.json)

                  Analysera en konverterad omgång:
                    java -cp target/my-atg-winner-1.0-SNAPSHOT.jar org.example.app.MyAtgCli --analyze converted-V86-2025-10-29.json


                Flöde per omgång:
                  1. På atg.se → DevTools/Network → välj spelet (V64, V75, V86...) → Copy response → spara som t.ex. v64-2025-11-02.json
                  2. --convert V64 v64-2025-11-02.json
                  3. --analyze converted-V64-2025-11-02.json
                """);
    }

    /* === --list-drivers === */
    private static void listDrivers() {
        Map<String, Driver> drivers = DriverLoader.loadDrivers();
        System.out.println("=== Kuskar i drivers.json ===");
        drivers.values().forEach(d ->
                System.out.printf("%-20s rating %d/5%n", d.name(), d.rating()));
    }

    /* === --analyze FILE.json === */
    private static void analyzeFile(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            System.err.println("Filen finns inte: " + filename);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        RaceCard card = mapper.readValue(file, new TypeReference<>() {});
        RaceAnalyzer analyzer = new RaceAnalyzer();

        System.out.println("=== Analys för " + card.gameType() + " " + card.date() + " ===");

        for (RaceToAnalyze race : card.races()) {
            List<EntryScore> ranking = analyzer.rankRace(race);

            System.out.println("\nLopp " + race.raceNumber() + " (" + race.track().name() + "):");

            ranking.forEach(es -> {
                Entry e = es.entry();
                Horse h = e.horse();
                Driver d = h.driver();

                System.out.printf(
                        "Spår %2d | %-18s | %-18s (%d/5) | Score %.2f%n",
                        e.startNumber(),
                        h.name(),
                        d.name(),
                        d.rating(),
                        es.score()
                );
            });
        }

        System.out.println("\nTips:");
        System.out.println("- Spik?: topphästen om den har klart högre score än tvåan.");
        System.out.println("- Gardera?: om topp 3 ligger nära varandra i score.");
    }
}
