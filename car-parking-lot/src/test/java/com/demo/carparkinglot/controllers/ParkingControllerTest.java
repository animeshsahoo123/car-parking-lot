package com.demo.carparkinglot.controllers;

import com.demo.carparkinglot.enums.SlotStatusEnum;
import com.demo.carparkinglot.exchanges.response.SlotInfoResponseDto;
import com.demo.carparkinglot.exchanges.response.VehicleParkingResponseDto;
import com.demo.carparkinglot.exchanges.response.VehicleUnParkingResponseDto;
import com.demo.carparkinglot.models.ParkingLot;
import com.demo.carparkinglot.models.ParkingSlot;
import com.demo.carparkinglot.models.vehicles.Car;
import com.demo.carparkinglot.services.ParkingLotService;
import com.demo.carparkinglot.services.VehicleService;
import com.demo.carparkinglot.utils.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
class ParkingControllerTest {

    @Mock
    private ParkingLotService parkingLotService;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private ParkingController parkingController;

    @BeforeEach
    void init() {
        parkingLotService.initParkingLot();
        ReflectionTestUtils.setFieldValue(parkingController, "parkingLotService", parkingLotService);
    }

    @Test
    void parkVehicleSuccessWhenNoVehicleIsParked() {
        String licenseNo = "BK-1234";
        when(parkingLotService.isVehicleAlreadyParked(licenseNo)).thenReturn(false);
        when(parkingLotService.parkVehicle(anyString(), any())).thenReturn(HttpStatus.OK);
        ResponseEntity<VehicleParkingResponseDto> resEntity = parkingController.parkVehicle(licenseNo);
        assertEquals(HttpStatus.OK, resEntity.getStatusCode());
        assertNotNull(resEntity.getBody());
        VehicleParkingResponseDto responseDto = resEntity.getBody();
        assertTrue(responseDto.isVehicleParked());
    }

    @Test
    void parkVehicleSuccessWhenVehicleIsAlreadyParked() {
        String licenseNo = "BK-1234";
        when(parkingLotService.isVehicleAlreadyParked(licenseNo)).thenReturn(Boolean.TRUE);
        ResponseEntity<VehicleParkingResponseDto> resEntity = parkingController.parkVehicle(licenseNo);
        assertEquals(HttpStatus.BAD_REQUEST, resEntity.getStatusCode());
        assertNotNull(resEntity.getBody());
        VehicleParkingResponseDto responseDto = resEntity.getBody();
        assertNull(responseDto.getLicensePlate());
        assertFalse(responseDto.isVehicleParked());
    }

    @Test
    void unparkVehicleSuccess() {
        String licenseNo = "BK-1234";
        ParkingSlot parkingSlot = new ParkingSlot(6L);
        parkingSlot.setParkedOn(LocalDateTime.now().minusHours(1));
        when(parkingLotService.getParkingLot()).thenReturn(new ParkingLot("", 5));
        ParkingLot parkingLot = parkingLotService.getParkingLot();
        ReflectionTestUtils.setFieldValue(parkingController, "parkingLotService", parkingLotService);
        Map<String, ParkingSlot> slotMap = new HashMap<>();
        slotMap.put(licenseNo, parkingSlot);
        ReflectionTestUtils.setFieldValue(parkingLot, "occupiedParkingSlots", slotMap);
        when(parkingLotService.findVehicleParkingSlotByLicenseNo(licenseNo)).thenReturn(Optional.of(parkingSlot));
        ResponseEntity<VehicleUnParkingResponseDto> resEntity = parkingController.unparkVehicle(licenseNo);
        assertEquals(HttpStatus.OK, resEntity.getStatusCode());
        assertNotNull(resEntity.getBody());
        VehicleUnParkingResponseDto responseDto = resEntity.getBody();
        assertEquals(licenseNo, responseDto.getLicensePlate());
    }

    @Test
    void unparkVehicleBadRequest() {
        String licenseNo = "BK-1234";
        when(parkingLotService.findVehicleParkingSlotByLicenseNo(licenseNo)).thenReturn(Optional.empty());
        ResponseEntity<VehicleUnParkingResponseDto> resEntity = parkingController.unparkVehicle(licenseNo);
        assertEquals(HttpStatus.BAD_REQUEST, resEntity.getStatusCode());
        assertNotNull(resEntity.getBody());
        VehicleUnParkingResponseDto responseDto = resEntity.getBody();
        assertNull(responseDto.getLicensePlate());
    }

    @Test
    void getValidSlotStatusAvailable() {
        long slotId = 1L;
        when(parkingLotService.findSlotById(slotId)).thenReturn(Optional.of(new ParkingSlot(slotId)));
        ResponseEntity<SlotInfoResponseDto> slotStatusRes = parkingController.getSlotStatus(slotId);
        assertEquals(HttpStatus.OK, slotStatusRes.getStatusCode());
        assertNotNull(slotStatusRes.getBody());
        SlotInfoResponseDto responseDto = slotStatusRes.getBody();
        assertEquals(slotId, responseDto.getSlotId());
        assertEquals(SlotStatusEnum.AVAILABLE, responseDto.getSlotStatus());
        assertNull(responseDto.getLicensePlate());
        assertNull(responseDto.getParkedOn());
        assertNull(responseDto.getTotalDurationParked());
    }

    @Test
    void getValidSlotStatusOccupied() {
        long slotId = 1L;
        ParkingSlot slot = mock(ParkingSlot.class);
        when(slot.getId()).thenReturn(slotId);
        when(slot.getParkedOn()).thenReturn(LocalDateTime.now().minusHours(1));
        when(slot.getVehicle()).thenReturn(new Car("LICENSE"));
        when(slot.getSlotStatusEnum()).thenReturn(SlotStatusEnum.OCCUPIED);
        when(parkingLotService.findSlotById(slotId)).thenReturn(Optional.of(slot));
        ResponseEntity<SlotInfoResponseDto> slotStatusRes = parkingController.getSlotStatus(slotId);
        assertEquals(HttpStatus.OK, slotStatusRes.getStatusCode());
        assertNotNull(slotStatusRes.getBody());
        SlotInfoResponseDto responseDto = slotStatusRes.getBody();
        assertEquals(slotId, responseDto.getSlotId());
        assertEquals(SlotStatusEnum.OCCUPIED, responseDto.getSlotStatus());
        assertNotNull(responseDto.getLicensePlate());
        assertNotNull(responseDto.getParkedOn());
        assertNotNull(responseDto.getTotalDurationParked());
    }

    @Test
    void getInvalidValidSlotStatus() {
        long slotId = 1L;
        when(parkingLotService.findSlotById(slotId)).thenReturn(Optional.empty());
        ResponseEntity<SlotInfoResponseDto> slotStatusRes = parkingController.getSlotStatus(slotId);
        assertEquals(HttpStatus.BAD_REQUEST, slotStatusRes.getStatusCode());
        assertNotNull(slotStatusRes.getBody());
        SlotInfoResponseDto responseDto = slotStatusRes.getBody();
        assertNull(responseDto.getSlotId());
        assertNull(responseDto.getSlotStatus());
        assertNull(responseDto.getLicensePlate());
        assertNull(responseDto.getParkedOn());
        assertNull(responseDto.getTotalDurationParked());
    }

    @Test
    void getTotalAvailableSlotsInParkingLot() {
        when(parkingLotService.getTotalAvailableSlotsInParkingLot()).thenReturn(5L);
        ResponseEntity<Long> slotStatusRes = parkingController.getTotalAvailableSlotsInParkingLot();
        assertEquals(HttpStatus.OK, slotStatusRes.getStatusCode());
        assertNotNull(slotStatusRes.getBody());
        assertEquals(5L, slotStatusRes.getBody());
    }

    @Test
    void getTotalSlotsInParkingLot() {
        when(parkingLotService.getTotalSlotsInParkingLot()).thenReturn(5L);
        ResponseEntity<Long> slotStatusRes = parkingController.getTotalSlotsInParkingLot();
        assertEquals(HttpStatus.OK, slotStatusRes.getStatusCode());
        assertNotNull(slotStatusRes.getBody());
        assertEquals(5L, slotStatusRes.getBody());
    }
}