package com.example.is_rogue_trader.repository;

import com.example.is_rogue_trader.model.entity.Planet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanetRepository extends JpaRepository<Planet, Long> {
    List<Planet> findByTraderId(Long traderId);
    
    @Query("SELECT p FROM Planet p WHERE p.trader.id = :traderId AND p.isRebellious = true")
    List<Planet> findRebelliousPlanetsByTraderId(@Param("traderId") Long traderId);
}

