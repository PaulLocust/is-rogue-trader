package com.example.is_rogue_trader.repository;

import com.example.is_rogue_trader.model.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByNavigatorId(Long navigatorId);

    @Query("SELECT r FROM Route r " +
           "WHERE (r.fromPlanet.id = :fromId AND r.toPlanet.id = :toId) " +
           "   OR (r.fromPlanet.id = :toId AND r.toPlanet.id = :fromId)")
    List<Route> findRoutesBetweenPlanets(@Param("fromId") Long fromId, @Param("toId") Long toId);

    @Query("SELECT r FROM Route r " +
           "WHERE r.fromPlanet.trader.id = :traderId OR r.toPlanet.trader.id = :traderId")
    List<Route> findByTraderId(@Param("traderId") Long traderId);
}

