package com.parking.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueDataPoint {
    private String month;
    private double revenue;
    private double occupancy;
}
