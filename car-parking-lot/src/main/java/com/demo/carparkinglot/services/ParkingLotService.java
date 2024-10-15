package com.demo.carparkinglot.services;

import com.demo.carparkinglot.configuration.ParkingLotConfig;
import com.demo.carparkinglot.enums.SlotAssignmentMessageEnum;
import com.demo.carparkinglot.exchanges.response.VehicleParkingResponseDto;
import com.demo.carparkinglot.models.ParkingLot;
import com.demo.carparkinglot.models.ParkingSlot;
import com.demo.carparkinglot.models.vehicles.Car;
import com.demo.carparkinglot.repositories.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingLotService {
    private final ParkingLotConfig parkingLotConfig;
    private final ParkingLotRepository parkingLotRepository;
    private final VehicleService vehicleService;
    private final ParkingSlotService parkingSlotService;
    private final AtomicBoolean parkingLotInitialized = new AtomicBoolean(false);

    public ParkingLot getParkingLot() {
        return parkingLotRepository.getParkingLot();
    }

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

    public HttpStatus parkVehicle(String licensePlateNo, VehicleParkingResponseDto responseDto) {
        HttpStatus httpStatus = HttpStatus.OK;
        // Try to get lock on parking slot
        // Since api is concurrent, multiple users should not be able to lock on same slot
        Optional<ParkingSlot> availableParkingSlotOpt = parkingSlotService.getAvailableParkingSlot(licensePlateNo);
        if (availableParkingSlotOpt.isPresent()) {
            ParkingSlot parkingSlot = availableParkingSlotOpt.get();
            try {
                // Default vehicle to car for now, in future if multiple vehicle types are supported,
                // use factory to prepare appropriate object
                Car car = vehicleService.createCarObjectForParking(licensePlateNo);
                // Park vehicle
                parkingSlot = parkVehicle(car, parkingSlot);
                // Populate response object with details
                responseDto.setSlotId(parkingSlot.getId());
                responseDto.setVehicleParkingTime(parkingSlot.getParkedOn());
                responseDto.setLicensePlate(car.getLicensePlateNo());
                responseDto.setMessage(SlotAssignmentMessageEnum.SLOT_ASSIGNED_SUCCESSFULLY.getMessage());
                log.info("Vehicle parked successfully in slot: {} licenseNo: {}", parkingSlot.getId(), licensePlateNo);
            } catch (Exception e) {
                // If exception occurs, we need to unlock the locked slot
                releaseLockOnParkingSlot(parkingSlot);
                log.error("Exception occurred while parking vehicle, ex: {}", e.toString());
                responseDto.setMessage(SlotAssignmentMessageEnum.INTERNAL_SERVER_ERROR.getMessage());
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        } else {
            responseDto.setMessage(SlotAssignmentMessageEnum.NO_SLOTS_AVAILABLE.getMessage());
        }
        return httpStatus;
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
