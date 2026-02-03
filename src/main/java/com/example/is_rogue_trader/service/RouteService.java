package com.example.is_rogue_trader.service;

import com.example.is_rogue_trader.model.entity.Navigator;
import com.example.is_rogue_trader.model.entity.Planet;
import com.example.is_rogue_trader.model.entity.Route;
import com.example.is_rogue_trader.repository.NavigatorRepository;
import com.example.is_rogue_trader.repository.PlanetRepository;
import com.example.is_rogue_trader.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final RouteRepository routeRepository;
    private final PlanetRepository planetRepository;
    private final NavigatorRepository navigatorRepository;

    public List<Route> getRoutesByNavigator(Long navigatorId) {
        return routeRepository.findByNavigatorId(navigatorId);
    }

    public List<Route> getRoutesByTrader(Long traderId) {
        return routeRepository.findByTraderId(traderId);
    }

    public Route getRouteById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Маршрут не найден"));
    }

    @Transactional
    public Route createRoute(Long fromPlanetId, Long toPlanetId, Long navigatorId) {
        Planet fromPlanet = planetRepository.findById(fromPlanetId)
                .orElseThrow(() -> new RuntimeException("Планета отправления не найдена"));
        Planet toPlanet = planetRepository.findById(toPlanetId)
                .orElseThrow(() -> new RuntimeException("Планета назначения не найдена"));
        Navigator navigator = navigatorRepository.findById(navigatorId)
                .orElseThrow(() -> new RuntimeException("Навигатор не найден"));

        // Проверяем, что маршрут между этими планетами (в любом направлении) ещё не существует
        if (!routeRepository.findRoutesBetweenPlanets(fromPlanetId, toPlanetId).isEmpty()) {
            throw new RuntimeException("Маршрут между этими планетами уже существует");
        }

        Route route = new Route();
        route.setFromPlanet(fromPlanet);
        route.setToPlanet(toPlanet);
        route.setNavigator(navigator);
        route.setIsStable(true);

        return routeRepository.save(route);
    }

    public Boolean checkRouteStability(Long routeId) {
        Route route = getRouteById(routeId);
        return route.getIsStable();
    }
}

