package com.demo.carparkinglot.exchanges.response;

import com.demo.carparkinglot.enums.SlotStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class SlotInfoResponseDto {
    private Long slotId;
    private SlotStatusEnum slotStatus;
    private String licensePlate;
    private LocalDateTime parkedOn;
    private Duration totalDurationParked;
    private String message;
}
