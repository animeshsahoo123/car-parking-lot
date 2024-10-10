package com.demo.carparkinglot.models;

import com.demo.carparkinglot.enums.SlotStatusEnum;
import com.demo.carparkinglot.models.vehicles.Car;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Setter
@ToString
public class ParkingLot {
    private final String name;
    private final Map<Long, ParkingSlot> unoccupiedParkingSlots = new ConcurrentHashMap<>();
    private final Map<String, ParkingSlot> occupiedParkingSlots = new ConcurrentHashMap<>();
    private final long totalSlots;

    public ParkingLot(String name, long totalSlots) {
        this.name = name;
        for (long slotId = 1; slotId <= totalSlots; slotId++) {
            unoccupiedParkingSlots.put(slotId, new ParkingSlot(slotId));
        }
        this.totalSlots = unoccupiedParkingSlots.size();
        log.info("ParkingLot created having total slots: {}", totalSlots);
    }

    public synchronized Optional<ParkingSlot> getAvailableParkingSlot(String licenseNo) {
        Optional<ParkingSlot> parkingSlotOpt = unoccupiedParkingSlots.values()
                .stream()
                .filter(ParkingSlot::isAvailable).findFirst();
        parkingSlotOpt.ifPresent(s -> {
            s.lockSlot(licenseNo);
            log.info("Slot: {} locked by vehicle having license plate: {}", s.getId(), licenseNo);
        });
        return parkingSlotOpt;
    }

    public void releaseLock(ParkingSlot parkingSlot) {
        if (parkingSlot.getSlotStatusEnum() == SlotStatusEnum.LOCKED) {
            parkingSlot.setSlotStatusEnum(SlotStatusEnum.AVAILABLE);
            log.info("Released lock on parking slot: {}", parkingSlot.getId());
        }
    }

    public ParkingSlot parkVehicle(Car car, ParkingSlot parkingSlot) {
        if (parkingSlot.isLocked() && car.getLicensePlateNo().equals(parkingSlot.getSlotLockVehicleLicenceNumber())) {
            parkingSlot.setSlotStatusEnum(SlotStatusEnum.OCCUPIED);
            parkingSlot.setVehicle(car);
            parkingSlot.setVehicleParkingTimestamp();
            unoccupiedParkingSlots.remove(parkingSlot.getId());
            occupiedParkingSlots.put(car.getLicensePlateNo(), parkingSlot);
            return parkingSlot;
        } else {
            throw new UnsupportedOperationException("You can't parking vehicle in a slot not locked by you...");
        }
    }

    public Optional<ParkingSlot> getParkingSlotByLicenseNo(String licenseNo) {
        if (occupiedParkingSlots.containsKey(licenseNo)) {
            return Optional.of(occupiedParkingSlots.get(licenseNo));
        }
        return Optional.empty();
    }

    public void unparkVehicle(ParkingSlot parkingSlot, String licenseNo) {
        if (parkingSlot.isOccupied()) {
            parkingSlot.unlockSlot(licenseNo);
            occupiedParkingSlots.remove(licenseNo);
            unoccupiedParkingSlots.put(parkingSlot.getId(), parkingSlot);
        } else {
            throw new UnsupportedOperationException(
                    String.format("You can't unpark vehicle in a slot which is not occupied. Current slot status: %s slotId: %s licenseNo: %s",
                            parkingSlot.getSlotStatusEnum(), parkingSlot.getId(), licenseNo));
        }
    }

    public Optional<ParkingSlot> getSlotById(Long slotId) {
        ParkingSlot parkingSlot = unoccupiedParkingSlots.get(slotId);
        if (Objects.isNull(parkingSlot)) {
            return occupiedParkingSlots.values().stream().filter(s -> s.getId().equals(slotId)).findFirst();
        }
        return Optional.of(parkingSlot);
    }

    public long getTotalSlotsInParkingLot() {
        return totalSlots;
    }

    public long getTotalAvailableSlots() {
        return unoccupiedParkingSlots.values().stream().filter(ParkingSlot::isAvailable).count();
    }

    public boolean isVehicleAlreadyParked(String licensePlateNo) {
        return occupiedParkingSlots.containsKey(licensePlateNo);
    }
}
