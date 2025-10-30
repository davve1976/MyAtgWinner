package org.example.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;

/**
 * Läser in hästdata från horses.json i resources.
 */
public class HorseLoader {

    public static List<Horse> loadHorses() {
        try (InputStream is = HorseLoader.class.getResourceAsStream("/horses.json")) {
            if (is == null) {
                throw new RuntimeException("horses.json saknas i resources!");
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(is, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Kunde inte läsa horses.json", e);
        }
    }
}
