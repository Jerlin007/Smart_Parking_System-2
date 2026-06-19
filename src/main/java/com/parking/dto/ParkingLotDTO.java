package com.parking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParkingLotDTO {

    private Long lotId;

    @NotBlank
    private String lotName;

    @NotBlank
    private String location;

    private Integer totalSlots;

    private Integer carSlots;

    private Integer bikeSlots;

    private Integer evSlots;

    private long availableSlots;

    private long occupiedSlots;

    private long reservedSlots;
}