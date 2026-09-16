package com.starfinance.repository;

import com.starfinance.entity.GoldRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



import java.util.Optional;

@Repository
public interface GoldRateRepository extends JpaRepository<GoldRate, Integer> {
    Optional<GoldRate> findByKaratLabel(String karatLabel);
}