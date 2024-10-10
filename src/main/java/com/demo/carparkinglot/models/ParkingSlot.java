package com.demo.carparkinglot.models;

import com.demo.carparkinglot.enums.SlotStatusEnum;
import com.demo.carparkinglot.models.vehicles.Vehicle;
import lombok.*;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static com.demo.carparkinglot.enums.SlotStatusEnum.LOCKED;
import static com.demo.carparkinglot.enums.SlotStatusEnum.OCCUPIED;

@ToString
@Getter
@Setter
@RequiredArgsConstructor
public class ParkingSlot {
    private final Long id;
    private Vehicle vehicle;
    private SlotStatusEnum slotStatusEnum = SlotStatusEnum.AVAILABLE; // Initially all slots are available
    @Getter(AccessLevel.NONE)
    private AtomicReference<String> slotLockLicenseNumberRef = new AtomicReference<>();
    private LocalDateTime parkedOn;

    public void lockSlot(String licenseNo) {
        if (isAvailable() && slotLockLicenseNumberRef.compareAndSet(null, licenseNo)) {
            this.slotStatusEnum = LOCKED;
        } else {
            throw new UnsupportedOperationException(
                    String.format("Cannot lock slot: %s having status: %s, slotLockedBy: %s",
                            id, slotStatusEnum, slotLockLicenseNumberRef.get()));
        }
    }

    public void unlockSlot(String licenseNo) {
        if (isOccupied() && licenseNo.equals(slotLockLicenseNumberRef.get())) {
            this.slotStatusEnum = SlotStatusEnum.AVAILABLE;
            this.vehicle = null;
            this.parkedOn = null;
            slotLockLicenseNumberRef.set(null);
        } else {
            throw new UnsupportedOperationException(
                    String.format("Can not unlock slot: %s slotStatus: %s slotLockedBy: %s",
                            id, slotStatusEnum, slotLockLicenseNumberRef.get()));
        }
    }

    public boolean isAvailable() {
        return slotStatusEnum == SlotStatusEnum.AVAILABLE;
    }

    public boolean isLocked() {
        return slotStatusEnum == LOCKED;
    }

    public boolean isOccupied() {
        return slotStatusEnum == OCCUPIED;
    }

    public String getSlotLockVehicleLicenceNumber() {
        return slotLockLicenseNumberRef.get();
    }

    public void setVehicleParkingTimestamp() {
        this.parkedOn = LocalDateTime.now();
    }
}
