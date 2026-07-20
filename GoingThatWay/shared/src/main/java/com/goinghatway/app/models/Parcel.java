package com.goinghatway.app.models;

import com.google.gson.annotations.SerializedName;

public class Parcel {

    public static final String STATUS_PENDING   = "PENDING";
    public static final String STATUS_MATCHED   = "MATCHED";
    public static final String STATUS_COLLECTED = "COLLECTED";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    @SerializedName("id")
    private String id;

    @SerializedName("sender_id")
    private String senderId;

    @SerializedName("sender")
    private User sender;

    @SerializedName("carrier_id")
    private String carrierId;

    @SerializedName("carrier")
    private User carrier;

    @SerializedName("description")
    private String description;

    @SerializedName("weight_kg")
    private double weightKg;

    @SerializedName("size_label")
    private String sizeLabel; // SMALL, MEDIUM, LARGE

    @SerializedName("pickup_address")
    private String pickupAddress;

    @SerializedName("pickup_lat")
    private double pickupLat;

    @SerializedName("pickup_lng")
    private double pickupLng;

    @SerializedName("destination_address")
    private String destinationAddress;

    @SerializedName("destination_lat")
    private double destinationLat;

    @SerializedName("destination_lng")
    private double destinationLng;

    @SerializedName("fee")
    private double fee; // total fee paid by sender

    @SerializedName("carrier_earnings")
    private double carrierEarnings; // fee * 0.80

    @SerializedName("platform_fee")
    private double platformFee; // fee * 0.20

    @SerializedName("payment_reference")
    private String paymentReference;

    @SerializedName("payment_status")
    private String paymentStatus; // PENDING, CONFIRMED

    @SerializedName("status")
    private String status;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("special_instructions")
    private String specialInstructions;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("collected_at")
    private String collectedAt;

    @SerializedName("delivered_at")
    private String deliveredAt;

    public Parcel() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public String getCarrierId() { return carrierId; }
    public void setCarrierId(String carrierId) { this.carrierId = carrierId; }

    public User getCarrier() { return carrier; }
    public void setCarrier(User carrier) { this.carrier = carrier; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public String getSizeLabel() { return sizeLabel; }
    public void setSizeLabel(String sizeLabel) { this.sizeLabel = sizeLabel; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public double getPickupLat() { return pickupLat; }
    public void setPickupLat(double pickupLat) { this.pickupLat = pickupLat; }

    public double getPickupLng() { return pickupLng; }
    public void setPickupLng(double pickupLng) { this.pickupLng = pickupLng; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public double getDestinationLat() { return destinationLat; }
    public void setDestinationLat(double destinationLat) { this.destinationLat = destinationLat; }

    public double getDestinationLng() { return destinationLng; }
    public void setDestinationLng(double destinationLng) { this.destinationLng = destinationLng; }

    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }

    public double getCarrierEarnings() { return carrierEarnings; }
    public void setCarrierEarnings(double carrierEarnings) { this.carrierEarnings = carrierEarnings; }

    public double getPlatformFee() { return platformFee; }
    public void setPlatformFee(double platformFee) { this.platformFee = platformFee; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getCollectedAt() { return collectedAt; }
    public void setCollectedAt(String collectedAt) { this.collectedAt = collectedAt; }

    public String getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(String deliveredAt) { this.deliveredAt = deliveredAt; }
}
