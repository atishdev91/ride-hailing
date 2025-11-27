package com.as.tripservice.models;

public enum TripStatus {

    REQUESTED,
    ASSIGNED,
    ACCEPTED,
    ARRIVED,
    STARTED,
    COMPLETED,
    CANCELLED;

    public boolean canTransitionTo(TripStatus newStatus) {
        return switch (this) {
            case REQUESTED -> newStatus == ASSIGNED;
            case ASSIGNED -> newStatus == ACCEPTED;
            case ACCEPTED -> newStatus == ARRIVED;
            case ARRIVED -> newStatus == STARTED;
            case STARTED -> newStatus == COMPLETED || newStatus == CANCELLED;
            default -> false;
        };
    }
}
