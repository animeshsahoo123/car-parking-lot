package com.demo.carparkinglot.models;

import com.demo.carparkinglot.enums.SlotStatusEnum;
import com.demo.carparkinglot.models.vehicles.Car;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.demo.carparkinglot.utils.ReflectionTestUtils.getFieldValue;
import static com.demo.carparkinglot.utils.ReflectionTestUtils.setFieldValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(SpringExtension.class)
class ParkingLotTest {

    private final String parkingLotName = "Demo Parking Lot";
    private final int noOfSlots = 5;
    private ParkingLot parkingLot;

    @BeforeEach
    void init() {
        parkingLot = new ParkingLot(parkingLotName, noOfSlots);
    }

    @Test
    void getAvailableParkingSlotWhenAvailable() {
        String license = "BK-78h";
        Optional<ParkingSlot> slot = parkingLot.getAvailableParkingSlot(license);
        assertTrue(slot.isPresent());
        ParkingSlot parkingSlot = slot.get();
        assertTrue(parkingSlot.isLocked());
        assertEquals(license, parkingSlot.getSlotLockVehicleLicenceNumber());
    }

    @Test
    void getAvailableParkingSlotWhenNoSlotsAvailable() {
        String license = "BK-78h";
        setFieldValue(parkingLot, "unoccupiedParkingSlots", new ConcurrentHashMap<>());
        Optional<ParkingSlot> slot = parkingLot.getAvailableParkingSlot(license);
        assertTrue(slot.isEmpty());
    }

    @Test
    void getAvailableParkingSlotWhenOneSlotsAvailable() {
        String license = "BK-78h";
        ParkingSlot parkingSlot = new ParkingSlot(1L);
        setFieldValue(parkingLot, "unoccupiedParkingSlots",
                Map.of(parkingSlot.getId(), parkingSlot));
        Optional<ParkingSlot> slot = parkingLot.getAvailableParkingSlot(license);
        assertFalse(slot.isEmpty());
        assertEquals(parkingSlot, slot.get());
    }


    @Test
    void releaseLockWhenLocked() {
        ParkingSlot parkingSlot = new ParkingSlot(1L);
        parkingSlot.setSlotStatusEnum(SlotStatusEnum.LOCKED);
        parkingLot.releaseLock(parkingSlot);
        assertEquals(SlotStatusEnum.AVAILABLE, parkingSlot.getSlotStatusEnum());
    }

    @Test
    void dontReleaseLockWhenNotLocked() {
        ParkingSlot parkingSlot = new ParkingSlot(1L);
        parkingSlot.setSlotStatusEnum(SlotStatusEnum.OCCUPIED);
        parkingLot.releaseLock(parkingSlot);
        assertEquals(SlotStatusEnum.OCCUPIED, parkingSlot.getSlotStatusEnum());
    }

    @Test
    void parkVehicleSuccessWhenLockedBySameUser() {
        final String license = "BK-1234";
        ParkingSlot parkingSlot = new ParkingSlot(1L);
        Map<Long, ParkingSlot> slotMap = new HashMap<>();
        slotMap.put(parkingSlot.getId(), parkingSlot);
        setFieldValue(parkingLot, "unoccupiedParkingSlots", slotMap);
        setFieldValue(parkingLot, "occupiedParkingSlots", new HashMap<>());
        parkingSlot.lockSlot(license);
        Car car = new Car(license);
        parkingSlot = parkingLot.parkVehicle(car, parkingSlot);
        assertEquals(SlotStatusEnum.OCCUPIED, parkingSlot.getSlotStatusEnum());
        assertEquals(car, parkingSlot.getVehicle());
        assertTrue(ChronoUnit.SECONDS.between(parkingSlot.getParkedOn(), LocalDateTime.now()) < 1);
        assertTrue(((Map<Long, ParkingSlot>) getFieldValue(parkingLot, "unoccupiedParkingSlots")).isEmpty());
        assertTrue(((Map<Long, ParkingSlot>) getFieldValue(parkingLot, "occupiedParkingSlots"))
                .containsKey(license));
    }

    @Test
    void parkVehicleFailsWhenLockedByDifferentUser() {
        final String license = "BK-1234";
        ParkingSlot parkingSlot = new ParkingSlot(1L);
        Map<Long, ParkingSlot> slotMap = new HashMap<>();
        slotMap.put(parkingSlot.getId(), parkingSlot);
        setFieldValue(parkingLot, "unoccupiedParkingSlots", slotMap);
        setFieldValue(parkingLot, "occupiedParkingSlots", new HashMap<>());
        parkingSlot.lockSlot("SOMEOTHERLICENSE");
        Car car = new Car(license);
        assertThrows(UnsupportedOperationException.class, () -> parkingLot.parkVehicle(car, parkingSlot));
    }

    @Test
    void getParkingSlotByParkedLicenseNoSuccess() {
        String licenseNo = "BK-12K";
        ParkingSlot parkingSlot = new ParkingSlot(1L);
        Map<String, ParkingSlot> slotMap = new HashMap<>();
        slotMap.put(licenseNo, parkingSlot);
        setFieldValue(parkingLot, "occupiedParkingSlots", slotMap);
        Optional<ParkingSlot> slotOpt = parkingLot.getParkingSlotByLicenseNo(licenseNo);
        assertTrue(slotOpt.isPresent());
        assertEquals(parkingSlot, slotOpt.get());
    }

    @Test
    void getParkingSlotByUnknownLicenseNoFails() {
        ParkingSlot parkingSlot = new ParkingSlot(1L);
        Map<String, ParkingSlot> slotMap = new HashMap<>();
        slotMap.put("license", parkingSlot);
        setFieldValue(parkingLot, "occupiedParkingSlots", slotMap);
        Optional<ParkingSlot> slotOpt = parkingLot.getParkingSlotByLicenseNo("unknownLicense");
        assertTrue(slotOpt.isEmpty());
    }

    @Test
    void unparkVehicleSuccessBySameLicenseUser() {
        String licenseNo = "LICENSE";
        ParkingSlot parkingSlot = new ParkingSlot(1L);
        Map<String, ParkingSlot> slotMap = new HashMap<>();
        slotMap.put(licenseNo, parkingSlot);
        setFieldValue(parkingLot, "occupiedParkingSlots", slotMap);
        HashMap<Long, ParkingSlot> unOccupiedSlots = new HashMap<>();
        setFieldValue(parkingLot, "unoccupiedParkingSlots", unOccupiedSlots);
        setFieldValue(parkingSlot, "slotLockLicenseNumberRef", new AtomicReference<>(licenseNo));
        parkingSlot.setSlotStatusEnum(SlotStatusEnum.OCCUPIED);
        parkingLot.unparkVehicle(parkingSlot, licenseNo);
        assertEquals(SlotStatusEnum.AVAILABLE, parkingSlot.getSlotStatusEnum());
        assertFalse(unOccupiedSlots.containsKey(licenseNo));
        assertTrue(unOccupiedSlots.containsKey(parkingSlot.getId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"AVAILABLE", "LOCKED"})
    void unparkVehicleFailsIfNotOccupied(String slotStatus) {
        String licenseNo = "LICENSE";
        ParkingSlot parkingSlot = new ParkingSlot(1L);
        Map<String, ParkingSlot> slotMap = new HashMap<>();
        slotMap.put(licenseNo, parkingSlot);
        setFieldValue(parkingLot, "occupiedParkingSlots", slotMap);
        HashMap<Long, ParkingSlot> unOccupiedSlots = new HashMap<>();
        setFieldValue(parkingLot, "unoccupiedParkingSlots", unOccupiedSlots);
        setFieldValue(parkingSlot, "slotLockLicenseNumberRef", new AtomicReference<>(licenseNo));
        parkingSlot.setSlotStatusEnum(SlotStatusEnum.valueOf(slotStatus));
        assertThrows(UnsupportedOperationException.class,
                () -> parkingLot.unparkVehicle(parkingSlot, licenseNo));
    }

    @Test
    void unparkVehicleFailsByDifferentLicenseUser() {
        String licenseNo = "LICENSE";
        ParkingSlot parkingSlot = new ParkingSlot(1L);
        Map<String, ParkingSlot> slotMap = new HashMap<>();
        slotMap.put(licenseNo, parkingSlot);
        setFieldValue(parkingLot, "occupiedParkingSlots", slotMap);
        HashMap<Long, ParkingSlot> unOccupiedSlots = new HashMap<>();
        setFieldValue(parkingLot, "unoccupiedParkingSlots", unOccupiedSlots);
        setFieldValue(parkingSlot, "slotLockLicenseNumberRef", new AtomicReference<>(licenseNo));
        parkingSlot.setSlotStatusEnum(SlotStatusEnum.OCCUPIED);
        assertThrows(UnsupportedOperationException.class,
                () -> parkingLot.unparkVehicle(parkingSlot, "SOMEOTHERLICENSE"));
    }

    @Test
    void getSlotByIdWhenUnOccupied() {
        Map<Long, ParkingSlot> slotMap = new HashMap<>();
        slotMap.put(1L, new ParkingSlot(1L));
        slotMap.put(2L, new ParkingSlot(2L));
        setFieldValue(parkingLot, "unoccupiedParkingSlots", slotMap);
        setFieldValue(parkingLot, "occupiedParkingSlots", new HashMap<>());
        long slotIdToSearch = 1L;
        Optional<ParkingSlot> slotById = parkingLot.getSlotById(slotIdToSearch);
        assertTrue(slotById.isPresent());
        assertEquals(slotIdToSearch, slotById.get().getId());
    }

    @Test
    void getSlotByIdWhenOccupied() {
        Map<Long, ParkingSlot> slotMap = new HashMap<>();
        slotMap.put(1L, new ParkingSlot(1L));
        slotMap.put(2L, new ParkingSlot(2L));
        setFieldValue(parkingLot, "unoccupiedParkingSlots", slotMap);
        setFieldValue(parkingLot, "occupiedParkingSlots", Map.of("ANY_LICENSE", new ParkingSlot(3L)));
        long slotIdToSearch = 3L;
        Optional<ParkingSlot> slotById = parkingLot.getSlotById(slotIdToSearch);
        assertTrue(slotById.isPresent());
        assertEquals(slotIdToSearch, slotById.get().getId());
    }

    @Test
    void getTotalSlotsInParkingLot() {
        assertEquals(noOfSlots, parkingLot.getTotalSlots());
    }

    @Test
    void getTotalAvailableSlotsInitially() {
        assertEquals(noOfSlots, parkingLot.getTotalAvailableSlots());
    }

    @Test
    void getTotalAvailableSlotsWhenParked() {
        Map<Long, ParkingSlot> slotMap = new HashMap<>();
        ParkingSlot unAvailableSlot = new ParkingSlot(1L);
        unAvailableSlot.setSlotStatusEnum(SlotStatusEnum.OCCUPIED);
        slotMap.put(1L, unAvailableSlot);
        slotMap.put(2L, new ParkingSlot(2L));
        setFieldValue(parkingLot, "unoccupiedParkingSlots", slotMap);
        assertEquals(1, parkingLot.getTotalAvailableSlots());
    }

    @Test
    void isVehicleAlreadyParkedTrueWhenVehicleIsParked() {
        String license = "LICENSE";
        ParkingSlot parkingSlot = new ParkingSlot(1L);
        Map<String, ParkingSlot> slotMap = new HashMap<>();
        slotMap.put(license, parkingSlot);
        setFieldValue(parkingLot, "occupiedParkingSlots", slotMap);
        assertTrue(parkingLot.isVehicleAlreadyParked(license));
    }

    @Test
    void isVehicleAlreadyParkedFalseWhenVehicleIsNotParked() {
        String license = "LICENSE";
        ParkingSlot parkingSlot = new ParkingSlot(1L);
        Map<String, ParkingSlot> slotMap = new HashMap<>();
        slotMap.put(license, parkingSlot);
        setFieldValue(parkingLot, "occupiedParkingSlots", slotMap);
        assertTrue(parkingLot.isVehicleAlreadyParked(license));
    }
}