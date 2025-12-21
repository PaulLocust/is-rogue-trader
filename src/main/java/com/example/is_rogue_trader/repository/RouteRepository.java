package com.example.is_rogue_trader.repository;

import com.example.is_rogue_trader.model.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByNavigatorId(Long navigatorId);
}

