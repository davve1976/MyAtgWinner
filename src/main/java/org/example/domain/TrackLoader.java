package org.example.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;

/**
 * Läser in ban-data från tracks.json i resources.
 */
public class TrackLoader {

    public static List<Track> loadTracks() {
        try (InputStream is = TrackLoader.class.getResourceAsStream("/tracks.json")) {
            if (is == null) {
                throw new RuntimeException("tracks.json saknas i resources!");
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(is, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Kunde inte läsa tracks.json", e);
        }
    }
}
