package com.goinghatway.app.utils;

public final class PriceCalculator {

    private PriceCalculator() {}

    public static double calculateParcelFee(double weightKg) {
        if (weightKg <= 1) return 30.0;
        if (weightKg <= 5) return 60.0;
        if (weightKg <= 15) return 120.0;
        return 200.0;
    }

    public static double calculateRideFare(int passengerCount) {
        if (passengerCount <= 1) return 80.0;
        if (passengerCount <= 3) return 150.0;
        return 220.0;
    }

    public static double calculateDriverEarning(double amount) {
        return amount * Constants.CARRIER_SHARE_PERCENT;
    }

    public static double calculatePlatformFee(double amount) {
        return amount * Constants.PLATFORM_FEE_PERCENT;
    }
}
