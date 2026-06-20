package com.parking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Monthly revenue data point for trend charts")
public class RevenueDataPoint {

    @Schema(description = "Month label", example = "Jan")
    private String month;

    @Schema(description = "Total revenue for the month", example = "5000.0")
    private double revenue;

    @Schema(description = "Occupancy percentage for the month", example = "75.5")
    private double occupancy;
}
