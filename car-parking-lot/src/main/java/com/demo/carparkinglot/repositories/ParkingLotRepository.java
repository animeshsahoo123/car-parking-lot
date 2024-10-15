package com.demo.carparkinglot.repositories;

import com.demo.carparkinglot.models.ParkingLot;

public interface ParkingLotRepository {

    void saveAndFlush(ParkingLot parkingLot);
    ParkingLot getParkingLot();

}
