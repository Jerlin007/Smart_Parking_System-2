package com.parking.dto;

import com.parking.enums.SlotStatus;
import com.parking.enums.SlotType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParkingSlotDTO {

    private Long slotId;

    private Long lotId;

    private String lotName;

    @NotBlank(message = "Slot number is required")
    private String slotNumber;

    @NotNull(message = "Slot type is required")
    private SlotType slotType;

    private SlotStatus status;

    @NotNull(message = "Floor number is required")
    @Min(value = 1, message = "Floor number must be greater than 0")
    private Integer floorNumber;
}