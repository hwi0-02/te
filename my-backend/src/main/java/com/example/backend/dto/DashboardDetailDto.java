package com.example.backend.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class DashboardDetailDto {
    List<DailyPoint> dailyRevenue;
    List<DailyPoint> dailySignups;
    List<MonthlyPoint> monthlySignups;
    List<HotelRevenuePoint> topHotels;

    @Value
    @Builder
    public static class DailyPoint {
        String date;
        Long value;
    }

    @Value
    @Builder
    public static class MonthlyPoint {
        Integer month;
        Integer count;
    }

    @Value
    @Builder
    public static class HotelRevenuePoint {
        Long hotelId;
        String hotelName;
        Long revenue;
    }
}