package com.demo.carparkinglot.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SlotAssignmentMessageEnum {
    SLOT_ASSIGNED_SUCCESSFULLY("Slot assigned successfully!"),
    NO_SLOTS_AVAILABLE("No slots are available"),
    INTERNAL_SERVER_ERROR("Internal server error, contact support...");

    private final String message;
}
