package dk.cphbusiness.flightdemo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightReaderTest {
    FlightReader instance = new FlightReader();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

//    @Test
    @DisplayName("Test if reader method works")
    void reader() {
        try {
            List<FlightReader.FlightDTO> flightList = instance.reader("https://api.aviationstack.com/v1/flights?access_key=%s&limit=%s&offset=%s", 1, 10);
            assertEquals(10, flightList.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    @DisplayName("Test reading Json Array from file using Jackson")
    void jsonFromFile() {
        try {
            List<FlightReader.FlightDTO> flights = instance.jsonFromFile("flights.json");
            assertEquals(5000, flights.size());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}