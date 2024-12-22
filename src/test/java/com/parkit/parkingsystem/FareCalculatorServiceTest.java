package com.parkit.parkingsystem;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar() {
        Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000)); // 1 heure
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket, false);

        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice(), 0.001); // Tolérance 0.001
    }

    @Test
    public void calculateFareBike() {
        Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000)); // 1 heure
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket, false);

        assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice(), 0.001); // Tolérance 0.001
    }

    @Test
    public void calculateFareUnknownType() {
        Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000)); // 1 heure
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket, false));
    }

    @Test
    public void calculateFareBikeWithFutureInTime() {
        Date inTime = new Date(System.currentTimeMillis() + (60 * 60 * 1000)); // 1 heure dans le futur
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket, false));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime() {
        Date inTime = new Date(System.currentTimeMillis() - (45 * 60 * 1000)); // 45 minutes
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket, false);

        assertEquals(0.75 * Fare.BIKE_RATE_PER_HOUR, ticket.getPrice(), 0.001); // Tolérance 0.001
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime() {
        Date inTime = new Date(System.currentTimeMillis() - (45 * 60 * 1000)); // 45 minutes
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket, false);

        assertEquals(0.75 * Fare.CAR_RATE_PER_HOUR, ticket.getPrice(), 0.001); // Tolérance 0.001
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime() {
        Date inTime = new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000)); // 24 heures
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket, false);

        assertEquals(24 * Fare.CAR_RATE_PER_HOUR, ticket.getPrice(), 0.001); // Tolérance 0.001
    }

    @Test
    public void calculateFareCarWithLessThan30minutesParkingTime() {
        Date inTime = new Date(System.currentTimeMillis() - (25 * 60 * 1000)); // 25 minutes
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
    
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
    
        fareCalculatorService.calculateFare(ticket, false);
    
        assertEquals(0.0, ticket.getPrice(), 0.001); // Tolérance 0.001
    }
    
    @Test
    public void calculateFareBikeWithLessThan30minutesParkingTime() {
        Date inTime = new Date(System.currentTimeMillis() - (25 * 60 * 1000)); // 25 minutes
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
    
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
    
        fareCalculatorService.calculateFare(ticket, false);
    
        assertEquals(0.0, ticket.getPrice(), 0.001); // Tolérance 0.001
    }

    @Test
    public void calculateFareCarWithDiscount() {
        // Given
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, true));
        // Convertir LocalDateTime en Date
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(60); // 60 minutes d'utilisation
        ticket.setInTime(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        ticket.setOutTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        
        FareCalculatorService fareCalculatorService = new FareCalculatorService();
        
        // When
        fareCalculatorService.calculateFare(ticket, true);
        
        // Then
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR * 0.95, 0.01); // Vérifie 95%
    }
    
    @Test
    public void calculateFareBikeWithDiscount() {
        // Given
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.BIKE, true));
        // Convertir LocalDateTime en Date
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(60); // 60 minutes d'utilisation
        ticket.setInTime(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        ticket.setOutTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        
        FareCalculatorService fareCalculatorService = new FareCalculatorService();
        
        // When
        fareCalculatorService.calculateFare(ticket, true);
        
        // Then
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR * 0.95, 0.01); // Vérifie 95%
    }

}
