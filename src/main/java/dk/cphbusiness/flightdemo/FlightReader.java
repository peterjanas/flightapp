package dk.cphbusiness.flightdemo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Purpose:
 *
 * @author: Thomas Hartmann
 */
public class FlightReader {
    private final String FLIGHT_URL = "https://api.aviationstack.com/v1/flights?access_key=%s&limit=%s&offset=%s";

    public static void main(String[] args) {
        FlightReader flightReader = new FlightReader();
        try {
            List<FlightDTO> flightList = getFlightsFromFile();
            List<FlightInfo> flightInfoList = getFlightInfoDetails(flightList);
            flightInfoList.forEach(f->{
                System.out.println("\n"+f);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public List<FlightDTO> reader(String urlString, int numberOfRequests, int limit) throws IOException {
        List<FlightDTO> flightDTOList = new ArrayList();
        List<FlightCollectionDTO> flightCollectionDTOList = new ArrayList<>();

        int offset = 0;
        FlightCollectionDTO flights = null;

        for (int i = 0; i < numberOfRequests; i++) {
            urlString = String.format(urlString, getPropertyValue("aviation.key"), limit, offset);
            URL url = new URL(urlString);
            flights = getObjectMapper()
                    .readValue(url, FlightCollectionDTO.class);
            flightCollectionDTOList.add(flights);
            offset += limit;
        }

        flightCollectionDTOList.forEach(flightCollectionDTO -> {
            for (FlightDTO flightDTO : flightCollectionDTO.getData()) {
                flightDTOList.add(flightDTO);
            }
        });
        return flightDTOList;
    }

    public void jsonToFile(List<FlightDTO> flightCollection, String fileName) throws IOException {
        getObjectMapper().writeValue(Paths.get(fileName).toFile(), flightCollection);
    }

    public List<FlightDTO> jsonFromFile(String fileName) throws IOException {
        List<FlightDTO> flights = getObjectMapper().readValue(Paths.get(fileName).toFile(), List.class);
        return flights;
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.writer(new DefaultPrettyPrinter());
        return objectMapper;
    }

    private static List<FlightInfo> getFlightInfoDetails(List<FlightDTO> flightList) {
        List<FlightInfo> flightInfoList = flightList.stream().map(flight -> {
            Duration duration = Duration.between(flight.getDeparture().getScheduled(), flight.getArrival().getScheduled());
            FlightInfo flightInfo = FlightInfo.builder()
                    .name(flight.getFlight().getNumber())
                    .iata(flight.getFlight().getIata())
                    .airline(flight.getAirline().getName())
                    .duration(duration)
                    .departure(flight.getDeparture().getScheduled().toLocalDateTime())
                    .arrival(flight.getArrival().getScheduled().toLocalDateTime())
                    .origin(flight.getDeparture().getAirport())
                    .destination(flight.getArrival().getAirport())
                    .build();

            return flightInfo;
        }).toList();
        return flightInfoList;
    }

    private static List<FlightDTO> getFlightsFromFile() throws IOException {
        FlightDTO[] flights = getObjectMapper().readValue(Paths.get("flights.json").toFile(), FlightDTO[].class);
        System.out.println("FlIGHT COLLECTION: " + flights);

        List<FlightDTO> flightList = Arrays.stream(flights).toList();
        return flightList;
    }

    private static void writeFlightsToFile(FlightReader flightReader) throws IOException {
        List<FlightDTO> result = flightReader.reader(flightReader.FLIGHT_URL, 50, 100);
        flightReader.jsonToFile(result, "flightsfile.json");
        System.out.println(result);
    }

    private String getPropertyValue(String key) throws IOException {
        Properties props = new Properties();
        props.load(FlightReader.class.getClassLoader().getResourceAsStream("config.properties"));
        return props.getProperty(key);
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @AllArgsConstructor
    @Builder
    @EqualsAndHashCode
    private static class FlightCollectionDTO {
        private FlightDTO[] data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @AllArgsConstructor
    @Builder
    @EqualsAndHashCode
    protected static class FlightDTO {
        private String flight_date;
        private String flight_status;
        private AirportTime departure;
        private AirportTime arrival;
        private AirplaneDTO flight;
        private AirlineDTO airline;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @AllArgsConstructor
    @Builder
    @EqualsAndHashCode
    private static class AirportTime {
        private String airport;
        private String timezone;
        private String iata;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        private OffsetDateTime scheduled;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @AllArgsConstructor
    @Builder
    @EqualsAndHashCode
    private static class AirlineDTO {
        private String name;
        private String iata;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @AllArgsConstructor
    @Builder
    @EqualsAndHashCode
    private static class AirplaneDTO {
        private String number;
        private String iata;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @AllArgsConstructor
    @Builder
    @EqualsAndHashCode
    private static class FlightInfo {
        private String name;
        private String iata;
        private String airline;
        private Duration duration;
        private LocalDateTime departure;
        private LocalDateTime arrival;
        private String origin;
        private String destination;
    }
}
