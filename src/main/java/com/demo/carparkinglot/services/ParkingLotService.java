package com.demo.carparkinglot.services;

import com.demo.carparkinglot.configuration.ParkingLotConfig;
import com.demo.carparkinglot.models.ParkingLot;
import com.demo.carparkinglot.models.ParkingSlot;
import com.demo.carparkinglot.models.vehicles.Car;
import com.demo.carparkinglot.repositories.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingLotService {
    private final ParkingLotConfig parkingLotConfig;
    private final ParkingLotRepository parkingLotRepository;
    private final AtomicBoolean parkingLotInitialized = new AtomicBoolean(false);

    public void initParkingLot() {
        if (parkingLotInitialized.compareAndSet(false, true)) {
            ParkingLot parkingLot = new ParkingLot(parkingLotConfig.getName(), parkingLotConfig.getSlots());
            log.info("Initializing parking lot having name: {} initial slotSize: {}",
                    parkingLot.getName(), parkingLot.getTotalSlotsInParkingLot());
            parkingLotRepository.saveAndFlush(parkingLot);
            log.info("Parking lot initialized!");
        } else {
            throw new UnsupportedOperationException("Parking lot can be initialized only once...");
        }
    }

    public Optional<ParkingSlot> getAvailableParkingSlot(String licenseNo) {
        ParkingLot parkingLot = parkingLotRepository.getParkingLot();
        return parkingLot.getAvailableParkingSlot(licenseNo);
    }

    public void releaseLockOnParkingSlot(ParkingSlot parkingSlot) {
        ParkingLot parkingLot = parkingLotRepository.getParkingLot();
        parkingLot.releaseLock(parkingSlot);
    }

    public ParkingSlot parkVehicle(Car car, ParkingSlot parkingSlot) {
        ParkingLot parkingLot = parkingLotRepository.getParkingLot();
        return parkingLot.parkVehicle(car, parkingSlot);
    }

    public Optional<ParkingSlot> findVehicleParkingSlotByLicenseNo(String licensePlateNo) {
        ParkingLot parkingLot = parkingLotRepository.getParkingLot();
        return parkingLot.getParkingSlotByLicenseNo(licensePlateNo);
    }

    public void unparkVehicle(ParkingSlot parkingSlot, String licenseNo) {
        ParkingLot parkingLot = parkingLotRepository.getParkingLot();
        parkingLot.unparkVehicle(parkingSlot, licenseNo);
    }

    public Optional<ParkingSlot> findSlotById(Long slotId) {
        ParkingLot parkingLot = parkingLotRepository.getParkingLot();
        return parkingLot.getSlotById(slotId);
    }

    public long getTotalSlotsInParkingLot() {
        ParkingLot parkingLot = parkingLotRepository.getParkingLot();
        return parkingLot.getTotalSlotsInParkingLot();
    }

    public long getTotalAvailableSlotsInParkingLot() {
        ParkingLot parkingLot = parkingLotRepository.getParkingLot();
        return parkingLot.getTotalAvailableSlots();
    }

    public boolean isVehicleAlreadyParked(String licensePlateNo) {
        ParkingLot parkingLot = parkingLotRepository.getParkingLot();
        return parkingLot.isVehicleAlreadyParked(licensePlateNo);
    }
}
