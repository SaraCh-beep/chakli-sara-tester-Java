package com.parkit.parkingsystem.integration;

import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

        @Test
    public void testParkingACar() {

    // Arrange
    ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

    // Act
    parkingService. processIncomingVehicle();
    // Assert
    // Vérifier que le ticket est bien sauvegardé
    Ticket ticket = ticketDAO.getTicket("ABCDEF");
    assertNotNull(ticket);
    assertEquals("ABCDEF", ticket.getVehicleRegNumber());
    assertNotNull(ticket.getInTime());
    assertEquals(0, ticket.getPrice());
    // Vérifier que la place est bien marquée comme occupée
    ParkingSpot parkingSpot = ticket.getParkingSpot();
    assertFalse(parkingSpot.isAvailable());
    }

    @Test
    public void testParkingLotExit(){
 
    // Arrange
    testParkingACar(); // D'abord garer la voiture
    ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    // Act
    parkingService.processExitingVehicle();
    
    // Assert
    // Vérifier que le ticket est mis à jour
    Ticket ticket = ticketDAO.getTicket("ABCDEF");
    assertNotNull(ticket);
    assertNotNull(ticket.getOutTime());
    assertTrue(ticket.getPrice() > 0);

    // vérifier que la place est libérée
    ParkingSpot parkingSpot = ticket.getParkingSpot();
    assertTrue(parkingSpot.isAvailable());
    }

    @Test
    public void testParkingLotExitRecurringUser() {
        // Arrange
        // Premier passage
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        // Deuxième passage (utilisateur récurrent)
        parkingService.processIncomingVehicle();

        // Attendre une heure simulée
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        ticket.setInTime(new Date(System.currentTimeMillis() + (60 * 60 * 1000))); // Simuler une entrée dans une heure
        ticketDAO.updateTicket(ticket);

        // Act
        parkingService.processExitingVehicle();

        // Assert
        ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket);
        
        // Vérifier que la réduction est appliquée
        double expectedPrice = Fare.CAR_RATE_PER_HOUR * 0.95; // Prix avec réduction de 5%
        assertEquals(expectedPrice, ticket.getPrice(), 0.001); // Vérification avec tolérance

        // Vérifier que la place est libérée
        assertTrue(ticket.getParkingSpot().isAvailable());
    }
}
