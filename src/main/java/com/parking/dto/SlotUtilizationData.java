package com.parking.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotUtilizationData {
    private String name;
    private long value;
    private String color;
}
