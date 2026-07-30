package com.tripgoapi.domain.model;

public enum TourStatus {
    ACTIVE,
    INACTIVE,
    /**
     * Soft-deleted. Tours are referenced by bookings and reviews, so "Xóa tour" in the admin
     * portal moves the row here instead of removing it: the booking history stays intact while
     * the tour disappears from both the public API and the admin list.
     */
    DELETED;

    /**
     * Statuses an admin is allowed to pick on the tour form. DELETED is deliberately excluded —
     * it is only ever reached through the delete action, never by editing a dropdown.
     */
    public static TourStatus[] editableValues() {
        return new TourStatus[]{ACTIVE, INACTIVE};
    }
}
