package com.demo.carparkinglot.services;

import com.demo.carparkinglot.models.ParkingLot;
import com.demo.carparkinglot.models.ParkingSlot;
import com.demo.carparkinglot.repositories.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParkingSlotService {
    private final ParkingLotRepository parkingLotRepository;

    public Optional<ParkingSlot> getAvailableParkingSlot(String licenseNo) {
        ParkingLot parkingLot = parkingLotRepository.getParkingLot();
        return parkingLot.getAvailableParkingSlot(licenseNo);
    }

}
