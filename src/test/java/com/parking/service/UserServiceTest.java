package com.parking.service;

import com.parking.entity.*;
import com.parking.enums.Role;
import com.parking.enums.TransactionStatus;
import com.parking.exception.ResourceNotFoundException;
import com.parking.repository.*;
import com.parking.service.impl.UserServiceImpl;
import com.parking.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private ParkingTransactionRepository transactionRepository;
    @Mock private BillingRepository billingRepository;
    @Mock private PaymentRepository paymentRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository, vehicleRepository, reservationRepository,
                transactionRepository, billingRepository, paymentRepository);
    }

    @Test
    void saveUser_ShouldReturnSavedUser() {
        User user = TestDataBuilder.createCustomerUser();
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.saveUser(user);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("customer");
        verify(userRepository).save(user);
    }

    @Test
    void getUserById_ShouldReturnUser() {
        User user = TestDataBuilder.createCustomerUser();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(2L);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(2L);
        assertThat(result.getUsername()).isEqualTo("customer");
    }

    @Test
    void getUserById_ShouldThrowWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id : 99");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        List<User> users = Arrays.asList(
                TestDataBuilder.createAdminUser(),
                TestDataBuilder.createCustomerUser());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_ShouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<User> result = userService.getAllUsers();

        assertThat(result).isEmpty();
    }

    @Test
    void updateUser_ShouldUpdateFields() {
        User existing = TestDataBuilder.createCustomerUser();
        User updated = User.builder()
                .username("newuser")
                .email("new@test.com")
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.updateUser(2L, updated);

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(result.getRole()).isEqualTo(Role.ROLE_CUSTOMER);
    }

    @Test
    void updateUser_ShouldThrowWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, new User()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteUser_ShouldDeleteUserWithNoVehicles() {
        User user = TestDataBuilder.createCustomerUser();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.deleteUser(2L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_ShouldDeleteUserWithVehiclesAndTransactions() {
        User user = TestDataBuilder.createCustomerUser();
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        ParkingSlot slot = TestDataBuilder.createAvailableCarSlot(lot);
        Vehicle vehicle = TestDataBuilder.createCarVehicle(user);
        ParkingTransaction tx = TestDataBuilder.createTransaction(1L, vehicle, slot, TransactionStatus.COMPLETED);
        Billing bill = TestDataBuilder.createBilling(1L, tx, "PENDING");
        tx.setBilling(bill);
        vehicle.setTransactions(Collections.singletonList(tx));
        user.setVehicles(Collections.singletonList(vehicle));

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(paymentRepository.findByBilling(bill)).thenReturn(Optional.empty());
        doNothing().when(billingRepository).delete(bill);
        doNothing().when(transactionRepository).deleteAll(anyList());
        doNothing().when(userRepository).delete(user);

        userService.deleteUser(2L);

        verify(paymentRepository).findByBilling(bill);
        verify(billingRepository).delete(bill);
        verify(transactionRepository).deleteAll(Collections.singletonList(tx));
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_ShouldThrowWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void activateUser_ShouldSetStatusActive() {
        User user = TestDataBuilder.createCustomerUser();
        user.setStatus("INACTIVE");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.activateUser(2L);

        assertThat(result.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void activateUser_ShouldThrowWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.activateUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deactivateUser_ShouldSetStatusInactive() {
        User user = TestDataBuilder.createCustomerUser();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.deactivateUser(2L);

        assertThat(result.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    void deactivateUser_ShouldThrowWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
