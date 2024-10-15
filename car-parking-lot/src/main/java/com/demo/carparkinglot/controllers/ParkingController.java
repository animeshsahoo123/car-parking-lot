package com.demo.carparkinglot.controllers;

import com.demo.carparkinglot.enums.SlotAssignmentMessageEnum;
import com.demo.carparkinglot.enums.SlotStatusEnum;
import com.demo.carparkinglot.exchanges.response.SlotInfoResponseDto;
import com.demo.carparkinglot.exchanges.response.VehicleParkingResponseDto;
import com.demo.carparkinglot.exchanges.response.VehicleUnParkingResponseDto;
import com.demo.carparkinglot.models.ParkingSlot;
import com.demo.carparkinglot.services.ParkingLotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/parking-lot")
@RequiredArgsConstructor
public class ParkingController {
    private final ParkingLotService parkingLotService;

    @PostMapping("/park")
    public ResponseEntity<VehicleParkingResponseDto> parkVehicle(@RequestBody String licensePlateNo) {
        log.info("Received request to park vehicle having license plate: {}", licensePlateNo);
        VehicleParkingResponseDto responseDto = new VehicleParkingResponseDto();
        // Check if vehicle is already parked
        HttpStatus httpStatus;
        if (parkingLotService.isVehicleAlreadyParked(licensePlateNo)) {
            httpStatus = HttpStatus.BAD_REQUEST;
            responseDto.setMessage("Vehicle already parked having license plate: " + licensePlateNo);
        } else {
            httpStatus = parkingLotService.parkVehicle(licensePlateNo, responseDto);
        }
        responseDto.setVehicleParked(HttpStatus.OK.equals(httpStatus));
        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @PostMapping("/unpark")
    public ResponseEntity<VehicleUnParkingResponseDto> unparkVehicle(@RequestBody String licensePlateNo) {
        log.info("Received request to unpark vehicle for licensePlate: {}", licensePlateNo);
        VehicleUnParkingResponseDto responseDto = new VehicleUnParkingResponseDto();
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            Optional<ParkingSlot> parkingSlotOpt = parkingLotService.findVehicleParkingSlotByLicenseNo(licensePlateNo);
            if (parkingSlotOpt.isPresent()) {
                ParkingSlot parkingSlot = parkingSlotOpt.get();
                LocalDateTime parkedOn = parkingSlot.getParkedOn();
                // Unpark vehicle
                parkingLotService.unparkVehicle(parkingSlot, licensePlateNo);
                // Update response
                responseDto.setSlotId(parkingSlot.getId());
                responseDto.setParkingTime(parkedOn);
                responseDto.setUnParkingTime(LocalDateTime.now());
                responseDto.setLicensePlate(licensePlateNo);
                responseDto.setTotalDurationParked(Duration.between(
                        responseDto.getParkingTime(), responseDto.getUnParkingTime()));
                responseDto.setMessage("Vehicle unparked!");
                log.info("Vehicle unparked from slot: {}", parkingSlot.getId());
            } else {
                responseDto.setMessage("No parked vehicle found having license plate: " + licensePlateNo);
                httpStatus = HttpStatus.BAD_REQUEST;
            }
        } catch (Exception e) {
            log.error("Exception occurred while unParking vehicle, ex: {}", e.toString());
            responseDto.setMessage(SlotAssignmentMessageEnum.INTERNAL_SERVER_ERROR.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping("/slot/{slotId}")
    public ResponseEntity<SlotInfoResponseDto> getSlotStatus(@PathVariable Long slotId) {
        SlotInfoResponseDto responseDto = new SlotInfoResponseDto();
        Optional<ParkingSlot> parkingSlotOpt = parkingLotService.findSlotById(slotId);
        HttpStatus httpStatus = HttpStatus.OK;
        if (parkingSlotOpt.isPresent()) {
            ParkingSlot parkingSlot = parkingSlotOpt.get();
            log.info("SlotStatus: {}", parkingSlot);
            responseDto.setSlotId(parkingSlot.getId());
            if (SlotStatusEnum.OCCUPIED.equals(parkingSlot.getSlotStatusEnum())) {
                responseDto.setParkedOn(parkingSlot.getParkedOn());
                responseDto.setLicensePlate(parkingSlot.getVehicle().getLicensePlateNo());
                responseDto.setTotalDurationParked(Duration.between(parkingSlot.getParkedOn(), LocalDateTime.now()));
            }
            responseDto.setSlotStatus(parkingSlot.getSlotStatusEnum());
            responseDto.setMessage("Slot found having slotId: " + slotId);
        } else {
            responseDto.setMessage("Slot not found having slotId: " + slotId);
            httpStatus = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(responseDto, httpStatus);
    }

    @GetMapping("/slot/total-available")
    public ResponseEntity<Long> getTotalAvailableSlotsInParkingLot() {
        return new ResponseEntity<>(parkingLotService.getTotalAvailableSlotsInParkingLot(), HttpStatus.OK);
    }

    @GetMapping("/slot/total-capacity")
    public ResponseEntity<Long> getTotalSlotsInParkingLot() {
        return new ResponseEntity<>(parkingLotService.getTotalSlotsInParkingLot(), HttpStatus.OK);
    }
}
