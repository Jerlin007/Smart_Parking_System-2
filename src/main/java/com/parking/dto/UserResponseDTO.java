package com.parking.dto;

import com.parking.enums.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private Long userId;
    private String username;
    private String email;
    private Role role;
    private String status;
    private LocalDateTime createdDate;
}