package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }

    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();

        // Durée du stationnement en millisecondes
        long duration = outTime - inTime;

        // Gratuité pour stationnement < 30 minutes
        if (duration < (30 * 60 * 1000)) {
            ticket.setPrice(0.0);
            return;
        }

        // Durée en heures
        double durationInHours = duration / (1000.0 * 60 * 60);

        // Déclaration et initialisation de ratePerHour
        double ratePerHour;
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR:
                ratePerHour = Fare.CAR_RATE_PER_HOUR;
                break;
            case BIKE:
                ratePerHour = Fare.BIKE_RATE_PER_HOUR;
                break;
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }

        // Calcul du prix total
        double price = durationInHours * ratePerHour;

        // Application de la réduction de 5% si applicable
        if (discount) {
            price *= 0.95;
        }

        // Mise à jour du prix dans le ticket
        ticket.setPrice(price);
    }
}