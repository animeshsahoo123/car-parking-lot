package com.demo.carparkinglot.services;

import com.demo.carparkinglot.models.ParkingLot;
import com.demo.carparkinglot.models.ParkingSlot;
import com.demo.carparkinglot.repositories.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@RequiredArgsConstructor
class ParkingSlotServiceTest {
    @Mock
    private ParkingLotRepository parkingLotRepository;

    private ParkingLot parkingLot;

    @BeforeEach
    void init() {
        this.parkingLot = Mockito.mock();
        when(parkingLotRepository.getParkingLot()).thenReturn(parkingLot);
    }

    @Test
    void getAvailableParkingSlot() {
        String licenseNo = "BK-23";
        when(parkingLot.getAvailableParkingSlot(licenseNo)).thenReturn(Optional.of(new ParkingSlot(2L)));
        assertTrue(parkingLot.getAvailableParkingSlot(licenseNo).isPresent());
    }
}