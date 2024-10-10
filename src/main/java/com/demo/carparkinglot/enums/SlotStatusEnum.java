package com.demo.carparkinglot.enums;

public enum SlotStatusEnum {
    AVAILABLE, // When no vehicle is parked
    OCCUPIED,  // When vehicle is parked
    LOCKED  // When a vehicle is trying to park a vehicle, handling concurrent access case (first come first serve)
}
