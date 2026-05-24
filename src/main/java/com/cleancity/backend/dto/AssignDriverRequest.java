package com.cleancity.backend.dto;

import java.util.UUID;

public class AssignDriverRequest {
    private UUID driverId;

    public UUID getDriverId() {
        return driverId;
    }

    public void setDriverId(UUID driverId) {
        this.driverId = driverId;
    }
}
