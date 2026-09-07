package com.telecomx.customer.service;

import com.telecomx.customer.config.JwtService;
import com.telecomx.customer.domain.Customer;
import com.telecomx.customer.dto.*;
import com.telecomx.customer.exception.ConflictException;
import com.telecomx.customer.repository.CustomerRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (customerRepository.existsByEmail(req.email())) {
            throw new ConflictException("An account with this email already exists");
        }
        if (customerRepository.existsByPhoneNumber(req.phoneNumber())) {
            throw new ConflictException("An account with this phone number already exists");
        }
        Customer customer = Customer.builder()
                .fullName(req.fullName())
                .email(req.email())
                .phoneNumber(req.phoneNumber())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role("CUSTOMER")
                .build();
        customer = customerRepository.save(customer);

        String token = jwtService.generateToken(customer.getId(), customer.getEmail(), customer.getRole());
        return new AuthResponse(token, jwtService.getExpirationMs(), customer.getId(), customer.getRole());
    }

    public AuthResponse login(LoginRequest req) {
        Customer customer = customerRepository.findByEmail(req.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(req.password(), customer.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        String token = jwtService.generateToken(customer.getId(), customer.getEmail(), customer.getRole());
        return new AuthResponse(token, jwtService.getExpirationMs(), customer.getId(), customer.getRole());
    }
}
