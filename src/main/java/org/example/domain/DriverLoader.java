package org.example.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Läser in kuskbetyg från drivers.json i resources.
 */
public class DriverLoader {

    public static Map<String, Driver> loadDrivers() {
        Map<String, Driver> driverMap = new HashMap<>();
        try (InputStream is = DriverLoader.class.getResourceAsStream("/drivers.json")) {
            if (is == null) {
                throw new RuntimeException("drivers.json saknas i resources!");
            }
            ObjectMapper mapper = new ObjectMapper();
            List<Driver> drivers = mapper.readValue(is, new TypeReference<>() {});
            for (Driver d : drivers) {
                driverMap.put(d.name().toLowerCase(), d);
            }
        } catch (Exception e) {
            throw new RuntimeException("Fel vid läsning av drivers.json: " + e.getMessage(), e);
        }
        return driverMap;
    }

    public static Driver find(Map<String, Driver> map, String name) {
        return map.getOrDefault(name.toLowerCase(), new Driver(name, 3));
    }
}
