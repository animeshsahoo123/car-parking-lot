package com.demo.carparkinglot.models;

import com.demo.carparkinglot.enums.SlotStatusEnum;
import com.demo.carparkinglot.utils.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.demo.carparkinglot.utils.ReflectionTestUtils.getFieldValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(SpringExtension.class)
class ParkingSlotTest {

    private ParkingSlot parkingSlot;

    @BeforeEach
    void init() {
        parkingSlot = new ParkingSlot(1L);
    }

    @Test
    void lockAvailableSlotPasses() {
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotStatusEnum", SlotStatusEnum.AVAILABLE);
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotLockLicenseNumberRef", new AtomicReference<>(null));
        String licenseNumber = "1234";
        parkingSlot.lockSlot(licenseNumber);
        assertTrue(parkingSlot.isLocked());
        assertEquals(licenseNumber,
                ((AtomicReference<String>) getFieldValue(parkingSlot, "slotLockLicenseNumberRef")).get());
    }

    @Test
    void lockLockedSlotFails() {
        String licenseNumber = "1234";
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotStatusEnum", SlotStatusEnum.LOCKED);
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotLockLicenseNumberRef", new AtomicReference<>(licenseNumber));
        assertThrows(UnsupportedOperationException.class, () -> parkingSlot.lockSlot(licenseNumber));
    }

    @Test
    void lockOccupiedSlotFails() {
        String licenseNumber = "1234";
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotStatusEnum", SlotStatusEnum.OCCUPIED);
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotLockLicenseNumberRef", new AtomicReference<>(licenseNumber));
        assertThrows(UnsupportedOperationException.class, () -> parkingSlot.lockSlot(licenseNumber));
    }

    @Test
    void unlockSlotPassesForUserForWhichLockIsObtained() {
        String licenseNumber = "1234";
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotStatusEnum", SlotStatusEnum.OCCUPIED);
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotLockLicenseNumberRef", new AtomicReference<>(licenseNumber));
        parkingSlot.unlockSlot(licenseNumber);
        assertEquals(SlotStatusEnum.AVAILABLE, parkingSlot.getSlotStatusEnum());
        assertNull(parkingSlot.getVehicle());
        assertNull(parkingSlot.getParkedOn());
        assertNull(((AtomicReference<String>) getFieldValue(parkingSlot, "slotLockLicenseNumberRef")).get());
    }

    @Test
    void unlockSlotByAnotherUserFails() {
        String licenseNumber = "1234";
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotStatusEnum", SlotStatusEnum.OCCUPIED);
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotLockLicenseNumberRef",
                new AtomicReference<>("OTHERLICENSE"));
        assertThrows(UnsupportedOperationException.class, () -> parkingSlot.unlockSlot(licenseNumber));
    }

    @Test
    void unlockUnoccupiedSlotFails() {
        String licenseNumber = "1234";
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotStatusEnum", SlotStatusEnum.LOCKED);
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotLockLicenseNumberRef", new AtomicReference<>(licenseNumber));
        assertThrows(UnsupportedOperationException.class, () -> parkingSlot.unlockSlot(licenseNumber));
    }


    @Test
    void isAvailableTrue() {
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotStatusEnum", SlotStatusEnum.AVAILABLE);
        assertTrue(parkingSlot.isAvailable());
    }

    @ParameterizedTest
    @ValueSource(strings = {"OCCUPIED", "LOCKED"})
    void isAvailableFalse(String status) {
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotStatusEnum", SlotStatusEnum.valueOf(status));
        assertFalse(parkingSlot.isAvailable());
    }

    @Test
    void isLockedTrue() {
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotStatusEnum", SlotStatusEnum.LOCKED);
        assertTrue(parkingSlot.isLocked());
    }

    @ParameterizedTest
    @ValueSource(strings = {"AVAILABLE", "OCCUPIED"})
    void isLockedFalse(String status) {
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotStatusEnum", SlotStatusEnum.valueOf(status));
        assertFalse(parkingSlot.isLocked());
    }

    @Test
    void isOccupiedTrue() {
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotStatusEnum", SlotStatusEnum.OCCUPIED);
        assertTrue(parkingSlot.isOccupied());
    }

    @ParameterizedTest
    @ValueSource(strings = {"AVAILABLE", "LOCKED"})
    void isOccupiedFalse(String status) {
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotStatusEnum", SlotStatusEnum.valueOf(status));
        assertFalse(parkingSlot.isOccupied());
    }

    @Test
    void getSlotLockVehicleLicenceNumber() {
        String licenseNumber = "L1234";
        ReflectionTestUtils.setFieldValue(parkingSlot, "slotLockLicenseNumberRef", new AtomicReference<>(licenseNumber));
        assertEquals(licenseNumber, parkingSlot.getSlotLockVehicleLicenceNumber());
    }

    @Test
    void setVehicleParkingTimestamp() {
        parkingSlot.setVehicleParkingTimestamp();
        assertNotNull(parkingSlot.getParkedOn());
        assertTrue(ChronoUnit.SECONDS.between(parkingSlot.getParkedOn(), LocalDateTime.now()) < 1);
    }
}