package org.neueda.rest.project.service;

import org.neueda.rest.project.dto.ImpactAnalysis;
import org.neueda.rest.project.entity.Investment;
import org.neueda.rest.project.repository.InvestmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Simulator {

    @Autowired
    private InvestmentRepository repository;

    public ImpactAnalysis SimulateTrade(Investment proposedTrade) {
        List<Investment> realPortfolio = repository.findAll();

        // 1. Create Simulation List (Deep Copy)
        List<Investment> simulated = new ArrayList<>();
        for (Investment inv : realPortfolio) {
            Investment copy = new Investment();
            copy.setTicker(inv.getTicker());
            copy.setSector(inv.getSector());
            copy.setQuantity(inv.getQuantity());
            copy.setBuyPrice(inv.getBuyPrice());
            simulated.add(copy);
        }
        simulated.add(proposedTrade);

        // 2. Calculate Sector % BEFORE Trade
        double realTotal = calculateTotalExposure(realPortfolio);
        Map<String, Double> oldSectors = calculateSectorPct(realPortfolio, realTotal);

        // 3. Calculate Sector % AFTER Trade
        double simTotal = calculateTotalExposure(simulated);
        Map<String, Double> newSectors = calculateSectorPct(simulated, simTotal);

        // 4. Build Analysis
        ImpactAnalysis analysis = new ImpactAnalysis(oldSectors, newSectors);

        // 5. Check Risk on the NEW Portfolio
        assessRisk(analysis, simulated, simTotal);

        return analysis;
    }

    // --- HELPER METHODS ---

    private double calculateTotalExposure(List<Investment> list) {
        // We use this ONLY for calculating percentages, not for displaying Value
        if (list == null || list.isEmpty()) return 0.0;
        return list.stream()
                .mapToDouble(i -> i.getBuyPrice().multiply(BigDecimal.valueOf(i.getQuantity())).doubleValue())
                .sum();
    }

    private Map<String, Double> calculateSectorPct(List<Investment> list, double total) {
        Map<String, Double> map = new HashMap<>();
        if (total == 0) return map;

        for (Investment inv : list) {
            double val = inv.getBuyPrice().multiply(BigDecimal.valueOf(inv.getQuantity())).doubleValue();
            map.put(inv.getSector(), map.getOrDefault(inv.getSector(), 0.0) + val);
        }
        // Convert to Percentage
        map.replaceAll((k, v) -> (v / total) * 100.0);
        return map;
    }

    private void assessRisk(ImpactAnalysis analysis, List<Investment> portfolio, double totalVal) {
        // 1. Concentration Risk (Single Stock)
        double maxExposure = 0;
        String biggestTicker = "None";
        Map<String, Double> tickerMap = new HashMap<>();

        for (Investment inv : portfolio) {
            double val = inv.getBuyPrice().multiply(BigDecimal.valueOf(inv.getQuantity())).doubleValue();
            tickerMap.put(inv.getTicker(), tickerMap.getOrDefault(inv.getTicker(), 0.0) + val);
        }

        for (Map.Entry<String, Double> entry : tickerMap.entrySet()) {
            if (entry.getValue() > maxExposure) {
                maxExposure = entry.getValue();
                biggestTicker = entry.getKey();
            }
        }

        double concPct = (totalVal > 0) ? (maxExposure / totalVal) * 100 : 0;
        analysis.setHighestStockPct(concPct);
        analysis.setHighestStockTicker(biggestTicker);

        // 2. Diversity Risk (Sector Count)
        long sectorCount = portfolio.stream().map(Investment::getSector).distinct().count();

        // 3. Verdict
        if (concPct > 30.0) {
            analysis.setRiskLevel("HIGH");
            analysis.setRiskMessage("⚠️ DANGER: " + biggestTicker + " dominates " + (int)concPct + "% of your portfolio.");
        } else if (sectorCount < 3) {
            analysis.setRiskLevel("MEDIUM");
            analysis.setRiskMessage("⚠️ WARNING: Poor Diversification (Only " + sectorCount + " sectors).");
        } else {
            analysis.setRiskLevel("LOW");
            analysis.setRiskMessage("✅ HEALTHY: Your portfolio is well balanced.");
        }
    }
}