package com.parking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parking.dto.UserDTO;
import com.parking.dto.UserResponseDTO;
import com.parking.entity.User;
import com.parking.enums.Role;
import com.parking.repository.UserRepository;
import com.parking.security.JwtService;
import com.parking.security.SecurityHelper;
import com.parking.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private JwtService jwtService;
    @MockBean private UserService userService;
    @MockBean private PasswordEncoder passwordEncoder;
    @MockBean private UserRepository userRepository;
    @MockBean private SecurityHelper securityHelper;

    private UserResponseDTO createDTO(Long id, String username, String email, Role role, String status) {
        return new UserResponseDTO(id, username, email, role, status, LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllUsers_ShouldReturnList() throws Exception {
        when(userRepository.findAll()).thenReturn(Arrays.asList(
                User.builder().userId(1L).username("admin").email("admin@test.com").role(Role.ROLE_ADMIN).status("ACTIVE").build(),
                User.builder().userId(2L).username("customer").email("customer@test.com").role(Role.ROLE_CUSTOMER).status("ACTIVE").build()
        ));
        when(securityHelper.isAdmin()).thenReturn(true);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[1].username").value("customer"));
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void getAllUsers_NonAdmin_ShouldFail() throws Exception {
        when(securityHelper.isAdmin()).thenReturn(false);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void getUser_OwnProfile_ShouldSucceed() throws Exception {
        when(securityHelper.isAdmin()).thenReturn(false);
        when(securityHelper.getCurrentUserId()).thenReturn(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(
                User.builder().userId(2L).username("customer").email("c@t.com").role(Role.ROLE_CUSTOMER).status("ACTIVE").build()
        ));

        mockMvc.perform(get("/api/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("customer"));
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void getUser_OtherProfile_ShouldFail() throws Exception {
        when(securityHelper.isAdmin()).thenReturn(false);
        when(securityHelper.getCurrentUserId()).thenReturn(2L);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void activateUser_ShouldSucceed() throws Exception {
        when(securityHelper.isAdmin()).thenReturn(true);
        when(userService.activateUser(1L)).thenReturn(
                User.builder().userId(1L).username("u").email("u@t.com").role(Role.ROLE_CUSTOMER).status("ACTIVE").build()
        );

        mockMvc.perform(put("/api/users/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void activateUser_NonAdmin_ShouldFail() throws Exception {
        when(securityHelper.isAdmin()).thenReturn(false);

        mockMvc.perform(put("/api/users/1/activate"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deactivateUser_ShouldSucceed() throws Exception {
        when(securityHelper.isAdmin()).thenReturn(true);
        when(userService.deactivateUser(1L)).thenReturn(
                User.builder().userId(1L).username("u").email("u@t.com").role(Role.ROLE_CUSTOMER).status("INACTIVE").build()
        );

        mockMvc.perform(put("/api/users/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteUser_ShouldSucceed() throws Exception {
        when(securityHelper.isAdmin()).thenReturn(true);
        when(securityHelper.getCurrentUserId()).thenReturn(1L);
        doNothing().when(userService).deleteUser(2L);

        mockMvc.perform(delete("/api/users/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteUser_CannotDeleteSelf() throws Exception {
        when(securityHelper.isAdmin()).thenReturn(true);
        when(securityHelper.getCurrentUserId()).thenReturn(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createUser_ShouldSucceed() throws Exception {
        UserDTO dto = new UserDTO(null, "newuser", "new@test.com", "password123", Role.ROLE_CUSTOMER);

        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setUserId(1L);
            return u;
        });

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateUser_ShouldSucceed() throws Exception {
        UserDTO dto = new UserDTO(null, "updated", "updated@test.com", null, Role.ROLE_ADMIN);
        when(userService.updateUser(anyLong(), any(User.class))).thenReturn(
                User.builder().userId(1L).username("updated").email("updated@test.com").role(Role.ROLE_ADMIN).status("ACTIVE").build()
        );

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updated"));
    }
}
