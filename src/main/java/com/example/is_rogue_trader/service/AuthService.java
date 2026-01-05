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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RogueTraderRepository rogueTraderRepository;
    private final GovernorRepository governorRepository;
    private final AstropathRepository astropathRepository;
    private final NavigatorRepository navigatorRepository;
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
        Long traderId = null;
        Long planetId = null;
        Long navigatorId = null;
        Long astropathId = null;
        Integer psiLevel = null;
        String houseName = null;

        if (request.getRole() == UserRole.TRADER) {
            RogueTrader trader = new RogueTrader();
            trader.setUser(user);
            trader.setDynastyName(request.getDynastyName() != null ? request.getDynastyName() : "Unknown Dynasty");
            trader.setWarrantNumber(request.getWarrantNumber());
            trader = rogueTraderRepository.save(trader);
            traderId = trader.getId();
        } else if (request.getRole() == UserRole.GOVERNOR) {
            if (request.getPlanetId() == null) {
                throw new RuntimeException("Для губернатора необходимо указать ID планеты");
            }
            Planet planet = planetRepository.findById(request.getPlanetId())
                    .orElseThrow(() -> new RuntimeException("Планета не найдена"));
            Governor governor = new Governor();
            governor.setUser(user);
            governor.setPlanet(planet);
            governor = governorRepository.save(governor);
            planetId = planet.getId();
        } else if (request.getRole() == UserRole.ASTROPATH) {
            Astropath astropath = new Astropath();
            astropath.setUser(user);
            astropath.setPsiLevel(request.getPsiLevel() != null ? request.getPsiLevel() : 5);
            astropath = astropathRepository.save(astropath);
            astropathId = astropath.getId();
            psiLevel = astropath.getPsiLevel();
        } else if (request.getRole() == UserRole.NAVIGATOR) {
            Navigator navigator = new Navigator();
            navigator.setUser(user);
            navigator.setHouseName(request.getHouseName() != null ? request.getHouseName() : "Unknown House");
            navigator = navigatorRepository.save(navigator);
            navigatorId = navigator.getId();
            houseName = navigator.getHouseName();
        }

        // Генерируем токен
        String token = tokenProvider.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        return new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), user.getRole().name(),
                traderId, planetId, navigatorId, astropathId, psiLevel, houseName);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Неверный email или пароль"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Неверный email или пароль");
        }

        // Получаем дополнительную информацию в зависимости от роли
        Long traderId = null;
        Long planetId = null;
        Long navigatorId = null;
        Long astropathId = null;
        Integer psiLevel = null;
        String houseName = null;

        if (user.getRole() == UserRole.TRADER) {
            traderId = rogueTraderRepository.findByUserId(user.getId())
                    .map(RogueTrader::getId)
                    .orElse(null);
        } else if (user.getRole() == UserRole.GOVERNOR) {
            planetId = governorRepository.findByUserId(user.getId())
                    .map(governor -> governor.getPlanet().getId())
                    .orElse(null);
        } else if (user.getRole() == UserRole.ASTROPATH) {
            // Используем Optional для получения значений
            Optional<Astropath> astropathOpt = astropathRepository.findByUserId(user.getId());
            if (astropathOpt.isPresent()) {
                Astropath astropath = astropathOpt.get();
                astropathId = astropath.getId();
                psiLevel = astropath.getPsiLevel();
            }
        } else if (user.getRole() == UserRole.NAVIGATOR) {
            // Используем Optional для получения значений
            Optional<Navigator> navigatorOpt = navigatorRepository.findByUserId(user.getId());
            if (navigatorOpt.isPresent()) {
                Navigator navigator = navigatorOpt.get();
                navigatorId = navigator.getId();
                houseName = navigator.getHouseName();
            }
        }

        String token = tokenProvider.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        return new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), user.getRole().name(),
                traderId, planetId, navigatorId, astropathId, psiLevel, houseName);
    }
}