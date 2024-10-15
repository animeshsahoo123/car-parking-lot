package com.demo.carparkinglot.services;

import com.demo.carparkinglot.configuration.ParkingLotConfig;
import com.demo.carparkinglot.enums.SlotStatusEnum;
import com.demo.carparkinglot.exchanges.response.VehicleParkingResponseDto;
import com.demo.carparkinglot.models.ParkingLot;
import com.demo.carparkinglot.models.ParkingSlot;
import com.demo.carparkinglot.models.vehicles.Car;
import com.demo.carparkinglot.repositories.ParkingLotRepository;
import com.demo.carparkinglot.utils.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.demo.carparkinglot.enums.SlotAssignmentMessageEnum.NO_SLOTS_AVAILABLE;
import static com.demo.carparkinglot.enums.SlotAssignmentMessageEnum.SLOT_ASSIGNED_SUCCESSFULLY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ParkingLotServiceTest {

    @Mock
    private ParkingLotConfig parkingLotConfig;

    @Mock
    private ParkingLotRepository parkingLotRepository;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private ParkingSlotService parkingSlotService;

    @InjectMocks
    private ParkingLotService parkingLotService;

    private ParkingLot parkingLot;

    @BeforeEach
    void setUp() {
        parkingLot = mock();
        when(parkingLotRepository.getParkingLot()).thenReturn(parkingLot);
    }

    @Test
    void getParkingLot() {
        assertEquals(parkingLot, parkingLotService.getParkingLot());
    }

    @Test
    void parkVehicleSuccess() {
        String license = "BK-123";
        ParkingLot parkingLot1 = new ParkingLot("", 2);
        when(parkingLotRepository.getParkingLot()).thenReturn(parkingLot1);
        ParkingSlot parkingSlot = new ParkingSlot(7L);
        parkingSlot.setSlotStatusEnum(SlotStatusEnum.LOCKED);
        parkingSlot.setSlotLockLicenseNumberRef(new AtomicReference<>(license));
        Map<Long, ParkingSlot> parkingSlots = new HashMap<>();
        parkingSlots.put(parkingSlot.getId(), parkingSlot);
        ReflectionTestUtils.setFieldValue(parkingLot1, "unoccupiedParkingSlots", parkingSlots);
        ReflectionTestUtils.setFieldValue(parkingLot1, "occupiedParkingSlots", new HashMap<>());
        when(parkingSlotService.getAvailableParkingSlot(license)).thenReturn(Optional.of(parkingSlot));
        when(vehicleService.createCarObjectForParking(license)).thenReturn(new Car(license));
        VehicleParkingResponseDto responseDto = new VehicleParkingResponseDto();
        HttpStatus httpStatus = parkingLotService.parkVehicle(license, responseDto);
        assertEquals(HttpStatus.OK, httpStatus);
        assertEquals(parkingSlot.getId(), responseDto.getSlotId());
        assertNotNull(responseDto.getVehicleParkingTime());
        assertEquals(license, responseDto.getLicensePlate());
        assertEquals(SLOT_ASSIGNED_SUCCESSFULLY.getMessage(), responseDto.getMessage());
    }

    @Test
    void parkVehicleFailureNoAvailableSlots() {
        String license = "BK-123";
        when(parkingSlotService.getAvailableParkingSlot(license)).thenReturn(Optional.empty());
        VehicleParkingResponseDto responseDto = new VehicleParkingResponseDto();
        HttpStatus httpStatus = parkingLotService.parkVehicle(license, responseDto);
        assertEquals(HttpStatus.OK, httpStatus);
        assertNull(responseDto.getSlotId());
        assertNull(responseDto.getVehicleParkingTime());
        assertNull(responseDto.getLicensePlate());
        assertEquals(NO_SLOTS_AVAILABLE.getMessage(), responseDto.getMessage());
    }

    @Test
    void findVehicleParkingSlotByLicenseNo() {
        String licenseNo = "BK-123";
        when(parkingLot.getParkingSlotByLicenseNo(licenseNo)).thenReturn(Optional.of(mock(ParkingSlot.class)));
        assertTrue(parkingLotService.findVehicleParkingSlotByLicenseNo(licenseNo).isPresent());
    }

    @Test
    void findSlotByIdSuccess() {
        ParkingSlot parkingSlot = new ParkingSlot(3L);
        when(parkingLot.getSlotById(3L)).thenReturn(Optional.of(parkingSlot));
        Optional<ParkingSlot> slotById = parkingLotService.findSlotById(3L);
        assertFalse(slotById.isEmpty());
        assertEquals(parkingSlot, slotById.get());
    }

    @Test
    void findSlotByIdFailure() {
        long slotId = 5L;
        when(parkingLot.getSlotById(slotId)).thenReturn(Optional.empty());
        Optional<ParkingSlot> slotById = parkingLotService.findSlotById(slotId);
        assertTrue(slotById.isEmpty());
    }

    @Test
    void getTotalSlotsInParkingLot() {
        when(parkingLot.getTotalSlotsInParkingLot()).thenReturn(3L);
        assertEquals(3L, parkingLotService.getTotalSlotsInParkingLot());
    }

    @Test
    void getTotalAvailableSlotsInParkingLot() {
        when(parkingLot.getTotalAvailableSlots()).thenReturn(5L);
        assertEquals(5L, parkingLotService.getTotalAvailableSlotsInParkingLot());
    }

    @Test
    void isVehicleAlreadyParkedTrue() {
        String license = "BK-123";
        when(parkingLot.isVehicleAlreadyParked(license)).thenReturn(true);
        assertTrue(parkingLotService.isVehicleAlreadyParked(license));
    }

    @Test
    void isVehicleAlreadyParkedFalse() {
        String license = "BK-123";
        when(parkingLot.isVehicleAlreadyParked(license)).thenReturn(false);
        assertFalse(parkingLotService.isVehicleAlreadyParked(license));
    }
}