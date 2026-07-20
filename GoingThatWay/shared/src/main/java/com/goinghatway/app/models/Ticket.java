package com.goinghatway.app.models;

import com.google.gson.annotations.SerializedName;

public class Ticket {

    public static final String STATUS_ACTIVE  = "ACTIVE";
    public static final String STATUS_USED    = "USED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    @SerializedName("id")
    private String id;

    @SerializedName("owner_id")
    private String ownerId;

    @SerializedName("owner")
    private User owner;

    @SerializedName("ticket_code")
    private String ticketCode;

    @SerializedName("price_paid")
    private double pricePaid;

    @SerializedName("status")
    private String status;

    @SerializedName("used_for_trip_id")
    private String usedForTripId;

    @SerializedName("payment_reference")
    private String paymentReference;

    @SerializedName("purchased_at")
    private String purchasedAt;

    @SerializedName("expires_at")
    private String expiresAt;

    @SerializedName("used_at")
    private String usedAt;

    public Ticket() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

    public double getPricePaid() { return pricePaid; }
    public void setPricePaid(double pricePaid) { this.pricePaid = pricePaid; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUsedForTripId() { return usedForTripId; }
    public void setUsedForTripId(String usedForTripId) { this.usedForTripId = usedForTripId; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public String getPurchasedAt() { return purchasedAt; }
    public void setPurchasedAt(String purchasedAt) { this.purchasedAt = purchasedAt; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }

    public String getUsedAt() { return usedAt; }
    public void setUsedAt(String usedAt) { this.usedAt = usedAt; }
}
