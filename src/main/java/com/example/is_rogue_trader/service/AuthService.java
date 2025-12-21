package com.example.is_rogue_trader.service;

import com.example.is_rogue_trader.dto.AuthResponse;
import com.example.is_rogue_trader.dto.LoginRequest;
import com.example.is_rogue_trader.dto.RegisterRequest;
import com.example.is_rogue_trader.model.entity.*;
import com.example.is_rogue_trader.model.enums.UserRole;
import com.example.is_rogue_trader.repository.*;
import com.example.is_rogue_trader.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final RogueTraderRepository rogueTraderRepository;
    private final PlanetRepository planetRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Проверяем, не существует ли уже пользователь с таким email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        // Создаем пользователя
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user = userRepository.save(user);

        // Создаем профиль в зависимости от роли
        if (request.getRole() == UserRole.TRADER) {
            RogueTrader trader = new RogueTrader();
            trader.setUser(user);
            trader.setDynastyName(request.getDynastyName() != null ? request.getDynastyName() : "Unknown Dynasty");
            trader.setWarrantNumber(request.getWarrantNumber());
            rogueTraderRepository.save(trader);
        }

        // Генерируем токен
        String token = tokenProvider.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        return new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Неверный email или пароль"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Неверный email или пароль");
        }

        String token = tokenProvider.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        return new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), user.getRole().name());
    }
}

