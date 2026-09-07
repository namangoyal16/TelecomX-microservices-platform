package com.telecomx.customer.service;

import com.telecomx.customer.config.JwtService;
import com.telecomx.customer.domain.Customer;
import com.telecomx.customer.dto.RegisterRequest;
import com.telecomx.customer.exception.ConflictException;
import com.telecomx.customer.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock CustomerRepository customerRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    @InjectMocks AuthService authService;

    @Test
    void register_createsCustomerAndReturnsToken() {
        RegisterRequest req = new RegisterRequest("Riya Sharma", "riya@example.com", "9990001111", "password123");
        when(customerRepository.existsByEmail(req.email())).thenReturn(false);
        when(customerRepository.existsByPhoneNumber(req.phoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(req.password())).thenReturn("hashed");
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(jwtService.generateToken(1L, req.email(), "CUSTOMER")).thenReturn("mock-jwt");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        var response = authService.register(req);

        assertThat(response.token()).isEqualTo("mock-jwt");
        assertThat(response.customerId()).isEqualTo(1L);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void register_throwsConflict_whenEmailAlreadyExists() {
        RegisterRequest req = new RegisterRequest("Riya Sharma", "riya@example.com", "9990001111", "password123");
        when(customerRepository.existsByEmail(req.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(customerRepository, never()).save(any());
    }
}
