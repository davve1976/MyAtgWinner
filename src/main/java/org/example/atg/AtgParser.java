package org.example.atg;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.domain.Driver;
import org.example.domain.DriverLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Konverterar ATG:s rådata (oavsett speltyp: V4, V5, V64, V65, V75, V86, osv)
 * till vårt interna RaceCard-format som analysmotorn redan förstår.
 *
 * Viktigt:
 *  - input är rå JSON du sparat från DevTools/Network → "Copy response"
 *  - output blir converted-<speltyp>-<datum>.json
 *
 * Antaganden om ATG JSON (baserat på det du visade):
 *
 * root:
 *   id: "V86_2025-10-29_40_1"
 *   races: [ { ... } ]
 *
 * race:
 *   number           -> loppnummer
 *   distance         -> distans meter
 *   startMethod      -> "auto" / "volte"
 *   track.name       -> bana
 *   starts[]         -> deltagare
 *
 * starts[] element:
 *   number           -> startspår
 *   horse.name       -> hästens namn
 *   horse.trainer    -> tränare
 *   driver.firstName / driver.lastName  -> kusk
 *   (ibland driver.name istället)
 *
 * Om vissa fält saknas (t.ex. starts[] heter participants[]),
 * försöker vi fallbacka.
 */
public class AtgParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * @param gameType   t.ex. "V64", "V75", "V86", "V4", "V5", "V65", "V85"
     * @param inputFile  filnamn på rå ATG JSON
     */
    public static void convert(String gameType, String inputFile) {
        try {
            // 1. Läs rå json
            File inFile = new File(inputFile);
            JsonNode root = MAPPER.readTree(inFile);

            // 2. Försök hitta datum.
            //    Vi har sett id i stil med: "V86_2025-10-29_40_1"
            //    Vi försöker parsa ut YYYY-MM-DD ur root.id. Annars fall-back: första race.date.
            String date = extractDate(root);

            // 3. Ladda kusk ratings
            Map<String, Driver> knownDrivers = DriverLoader.loadDrivers();

            // 4. Bygg RaceCard-output
            ObjectNode raceCardOut = MAPPER.createObjectNode();
            raceCardOut.put("gameType", gameType);
            raceCardOut.put("date", date);

            var racesArrayOut = MAPPER.createArrayNode();

            for (JsonNode raceNode : root.path("races")) {
                // race info
                int raceNumber = raceNode.path("number").asInt();
                int distance = raceNode.path("distance").asInt();
                String startMethod = raceNode.path("startMethod").asText();
                boolean autoStart = isAutoStart(startMethod);

                String trackName = raceNode.path("track").path("name").asText("?");

                ObjectNode trackOut = MAPPER.createObjectNode();
                trackOut.put("name", trackName);
                // placeholder tills vi kopplar tracks.json:
                trackOut.put("stretchLengthMeters", 0);
                trackOut.put("isTightTrack", false);

                ObjectNode raceOut = MAPPER.createObjectNode();
                raceOut.put("raceNumber", raceNumber);
                raceOut.set("track", trackOut);
                raceOut.put("distanceMeters", distance);
                raceOut.put("autoStart", autoStart);

                // starters
                var startersArrayOut = MAPPER.createArrayNode();

                JsonNode startsNode = findStartsArray(raceNode);
                if (startsNode != null && startsNode.isArray()) {
                    for (JsonNode startNode : startsNode) {
                        int startNumber = startNode.path("number").asInt();

                        // horse
                        JsonNode horseNode = startNode.path("horse");
                        String horseName = horseNode.path("name").asText("?");
                        String trainer = horseNode.path("trainer").asText("?");

                        // driver
                        JsonNode driverNode = startNode.path("driver");
                        String driverFullName = extractDriverName(driverNode);
                        int rating = lookupDriverRating(driverFullName, knownDrivers);

                        ObjectNode driverOut = MAPPER.createObjectNode();
                        driverOut.put("name", driverFullName);
                        driverOut.put("rating", rating);

                        // lastRaces -> tom tills vi gör historik
                        var lastRacesArray = MAPPER.createArrayNode();

                        ObjectNode horseOut = MAPPER.createObjectNode();
                        horseOut.put("name", horseName);
                        horseOut.put("trainer", trainer);
                        horseOut.set("driver", driverOut);
                        horseOut.set("lastRaces", lastRacesArray);

                        ObjectNode starterOut = MAPPER.createObjectNode();
                        starterOut.put("startNumber", startNumber);
                        starterOut.set("horse", horseOut);

                        startersArrayOut.add(starterOut);
                    }
                } else {
                    System.err.println("⚠ Hittade inga starts/participants för lopp " + raceNumber);
                }

                raceOut.set("starters", startersArrayOut);

                racesArrayOut.add(raceOut);
            }

            raceCardOut.set("races", racesArrayOut);

            // 5. Skriv fil
            String safeDate = (date == null || date.isBlank()) ? "UNKNOWNDATE" : date;
            String outName = "converted-" + gameType + "-" + safeDate + ".json";
            Path outPath = Path.of(outName);

            String pretty = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(raceCardOut);
            Files.writeString(outPath, pretty);

            System.out.println("✅ Sparade konverterad fil: " + outName);
            System.out.println("Analysera nu med:");
            System.out.println("  java -cp target/my-atg-winner-1.0-SNAPSHOT.jar org.example.app.MyAtgCli --analyze " + outName);

        } catch (IOException e) {
            throw new RuntimeException("Kunde inte läsa/parsa " + inputFile, e);
        }
    }

    // Försök hitta datum för omgången
    private static String extractDate(JsonNode root) {
        // 1. Försök root.id -> "V86_2025-10-29_40_1"
        String id = root.path("id").asText("");
        if (!id.isEmpty()) {
            // leta efter första token som ser ut som YYYY-MM-DD
            String[] parts = id.split("_");
            for (String p : parts) {
                if (p.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    return p;
                }
            }
        }

        // 2. fallback: plocka från första race.date
        if (root.has("races") && root.get("races").isArray() && root.get("races").size() > 0) {
            JsonNode firstRace = root.get("races").get(0);
            String d = firstRace.path("date").asText("");
            if (!d.isEmpty()) {
                return d;
            }
        }

        return "UNKNOWN";
    }

    // ATG kan kalla det "starts", "start", "participants".
    private static JsonNode findStartsArray(JsonNode raceNode) {
        if (raceNode.has("starts") && raceNode.get("starts").isArray()) {
            return raceNode.get("starts");
        }
        if (raceNode.has("start") && raceNode.get("start").isArray()) {
            return raceNode.get("start");
        }
        if (raceNode.has("participants") && raceNode.get("participants").isArray()) {
            return raceNode.get("participants");
        }
        return null;
    }

    // Bygg kusknamn snyggt oavsett fältupplägg
    private static String extractDriverName(JsonNode driverNode) {
        if (driverNode == null || driverNode.isMissingNode()) {
            return "Okänd kusk";
        }
        String full = driverNode.path("name").asText("");
        if (!full.isBlank()) {
            return full;
        }
        String first = driverNode.path("firstName").asText("");
        String last = driverNode.path("lastName").asText("");
        String merged = (first + " " + last).trim();
        return merged.isBlank() ? "Okänd kusk" : merged;
    }

    // slå i drivers.json
    private static int lookupDriverRating(String driverFullName, Map<String, Driver> knownDrivers) {
        Driver d = knownDrivers.get(driverFullName);
        if (d != null) {
            return d.rating();
        }
        // fallback default
        return 1;
    }

    private static boolean isAutoStart(String startMethod) {
        if (startMethod == null) return false;
        String s = startMethod.toLowerCase();
        return s.contains("auto"); // "auto", "autostart"
    }
}
