package com.starfinance.repository;

import com.starfinance.entity.GoldRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface GoldRateRepository extends JpaRepository<GoldRate, Integer> {

}