package com.starfinance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starfinance.dto.GoldRateResponse;
import com.starfinance.entity.GoldRate;
import com.starfinance.repository.GoldRateRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BullionService {

    private static final Logger log = LoggerFactory.getLogger(BullionService.class);
    private static final String GOLD_API_URL = "https://api.gold-api.com/price/XAU/INR";
    private static final double TROY_OUNCE_TO_GRAMS = 31.1034768;

    private final GoldRateRepository goldRateRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Fetch all current rates from database (or populate if empty).
     */
    public List<GoldRateResponse> getAllRates() {
        List<GoldRate> entities = goldRateRepository.findAll();
        if (entities.isEmpty()) {
            refreshLiveGoldRates();
            entities = goldRateRepository.findAll();
        }

        return entities.stream()
                .map(entity -> new GoldRateResponse(
                        entity.getKaratLabel(),
                        entity.getRatePerGram().doubleValue()
                ))
                .toList();
    }

    /**
     * Automatically update gold rates on startup, and every 6 hours.
     */
    @PostConstruct
    public void init() {
        refreshLiveGoldRates();
    }

    @Scheduled(fixedRate = 21600000) // Every 6 hours
    public void scheduledUpdate() {
        log.info("Running scheduled bullion rate update from live market API...");
        refreshLiveGoldRates();
    }

    /**
     * Connect to free live gold market API and update rates in the database.
     */
    public synchronized void refreshLiveGoldRates() {
        try {
            log.info("Fetching real-time gold price (XAU/INR) from {}", GOLD_API_URL);
            String jsonResponse = restTemplate.getForObject(GOLD_API_URL, String.class);
            if (jsonResponse == null || jsonResponse.isBlank()) {
                log.warn("Received empty response from gold API. Keeping existing database rates.");
                return;
            }

            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode priceNode = root.get("price");
            if (priceNode == null || !priceNode.isNumber()) {
                log.warn("Missing 'price' field in gold API response: {}", jsonResponse);
                return;
            }

            double xauInrOunce = priceNode.asDouble();
            // Convert troy ounce to 1 gram:
            double rawRatePerGram24K = xauInrOunce / TROY_OUNCE_TO_GRAMS;
            
            // Indian retail gold benchmark typically includes domestic customs/duties (~5.5%)
            double benchmark24K = Math.round(rawRatePerGram24K * 1.055);
            double benchmark22K = Math.round(benchmark24K * (22.0 / 24.0));
            double benchmark18K = Math.round(benchmark24K * (18.0 / 24.0));
            double benchmark14K = Math.round(benchmark24K * (14.0 / 24.0));
            double benchmark8K  = Math.round(benchmark24K * (8.0 / 24.0));

            log.info("Live market calculated Indian rates -> 24K: ₹{}/g, 22K: ₹{}/g", benchmark24K, benchmark22K);

            updateOrInsertRate("24 Karat", BigDecimal.valueOf(benchmark24K));
            updateOrInsertRate("22 Karat", BigDecimal.valueOf(benchmark22K));
            updateOrInsertRate("18 Karat", BigDecimal.valueOf(benchmark18K));
            updateOrInsertRate("14 Karat", BigDecimal.valueOf(benchmark14K));
            updateOrInsertRate("8 Karat",  BigDecimal.valueOf(benchmark8K));

            log.info("Successfully updated real-time bullion rates in database.");
        } catch (Exception e) {
            log.error("Failed to fetch or update live bullion rates from external API: {}. Fallback to DB.", e.getMessage());
            ensureFallbackSeedRates();
        }
    }

    private void updateOrInsertRate(String karatLabel, BigDecimal ratePerGram) {
        Optional<GoldRate> existing = goldRateRepository.findByKaratLabel(karatLabel);
        GoldRate rate;
        if (existing.isPresent()) {
            rate = existing.get();
            rate.setRatePerGram(ratePerGram.setScale(2, RoundingMode.HALF_UP));
        } else {
            rate = new GoldRate();
            rate.setKaratLabel(karatLabel);
            rate.setRatePerGram(ratePerGram.setScale(2, RoundingMode.HALF_UP));
        }
        goldRateRepository.save(rate);
    }

    private void ensureFallbackSeedRates() {
        if (goldRateRepository.count() == 0) {
            log.info("No gold rates in database. Seeding standard benchmarks.");
            updateOrInsertRate("24 Karat", BigDecimal.valueOf(9150.00));
            updateOrInsertRate("22 Karat", BigDecimal.valueOf(8380.00));
            updateOrInsertRate("18 Karat", BigDecimal.valueOf(6860.00));
            updateOrInsertRate("14 Karat", BigDecimal.valueOf(5330.00));
            updateOrInsertRate("8 Karat",  BigDecimal.valueOf(3050.00));
        }
    }
}