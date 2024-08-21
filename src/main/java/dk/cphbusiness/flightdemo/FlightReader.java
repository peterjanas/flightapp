package dk.cphbusiness.flightdemo;

import dk.cphbusiness.utils.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Purpose:
 *
 * @author: Thomas Hartmann
 */
public class FlightReader {
    static List<DTOs.FlightInfo> flightInfoList;


    public static void main(String[] args) {
        FlightReader flightReader = new FlightReader();

        try {
            List<DTOs.FlightDTO> flightList = flightReader.getFlightsFromFile("flights.json");
            flightInfoList = flightReader.getFlightInfoDetails(flightList);
            flightInfoList.forEach(f -> {
                System.out.println("\n" + f);
            });

            flightReader.airLineAvg("Lufthansa");
            flightReader.airLineSum("Lufthansa");

            List<DTOs.FlightInfo> flightInfoList = flightReader.getFlightInfoDetails(flightList);

            Duration lufthansaTotalDuration = flightReader.getTotalFlightTime(flightList);
            String formattedDuration = flightReader.formatDuration(lufthansaTotalDuration);
            System.out.println("Total flight time for Lufthansa: " + formattedDuration);

           /* flightInfoList.forEach(f->{
                System.out.println("\n"+f);
            });*/
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


//    public List<FlightDTO> jsonFromFile(String fileName) throws IOException {
//        List<FlightDTO> flights = getObjectMapper().readValue(Paths.get(fileName).toFile(), List.class);
//        return flights;
//    }

    //Vi instantiere en double. Herefter tager vi fat i alle flights, og filtrerer specifikt efter Lufthansa.
    //Når flights er filtreret (.equals "Lufthansa") finder vi average for alle de flights.
    //Vi tager herefter fat i MapToDouble hvor vi hiver duration af flights ud med en fucked format 'P1H50M', og laver
    // det om til minutes. (Fra AirportTime fra DTO).
    public double airLineAvg (String airLine){
        double averageForLufthansa = flightInfoList
                .stream().filter(flightInfo -> airLine.equals(flightInfo.getAirline()))
                .mapToDouble(flightInfo -> flightInfo.getDuration().toMinutes())
                .average()
                .orElse(0.0);

        System.out.println("\n \n \nAvg tid for " + airLine + " flytider er " + averageForLufthansa + " min.");

        return averageForLufthansa;
    }

    public double airLineSum (String airLine){
        double totalForLufthansa = flightInfoList
                .stream().filter(flightInfo -> airLine.equals(flightInfo.getAirline()))
                .mapToDouble(flightInfo -> flightInfo.getDuration().toMinutes())
                .sum();

        System.out.println("\n \n \nTotal tid for " + airLine + " flytider er " + totalForLufthansa + " min.");

        return totalForLufthansa;
    }


    public List<DTOs.FlightInfo> getFlightInfoDetails(List<DTOs.FlightDTO> flightList) {
        List<DTOs.FlightInfo> flightInfoList = flightList.stream().map(flight -> {
            Duration duration = Duration.between(flight.getDeparture().getScheduled(), flight.getArrival().getScheduled());
            DTOs.FlightInfo flightInfo = DTOs.FlightInfo.builder()
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

    public String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public Duration getTotalFlightTime(List<DTOs.FlightDTO> flightList)
    {
        Duration totalDuration = flightList.stream()
                .filter(flight -> "Lufthansa".equals(flight.getAirline().getName()))
                .map(flight -> Duration.between(flight.getDeparture().getScheduled(), flight.getArrival().getScheduled()))
                .reduce(Duration.ZERO, Duration::plus); // Sum up all durations



        return totalDuration;
    }


    public List<DTOs.FlightDTO> getFlightsFromFile(String filename) throws IOException {
        DTOs.FlightDTO[] flights = new Utils().getObjectMapper().readValue(Paths.get(filename).toFile(), DTOs.FlightDTO[].class);

        List<DTOs.FlightDTO> flightList = Arrays.stream(flights).toList();
        return flightList;
    }
}
