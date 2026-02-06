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

        double realTotal = calculateTotalExposure(realPortfolio);
        Map<String, Double> oldSectors = calculateSectorPct(realPortfolio, realTotal);


        double simTotal = calculateTotalExposure(simulated);
        Map<String, Double> newSectors = calculateSectorPct(simulated, simTotal);


        ImpactAnalysis analysis = new ImpactAnalysis(oldSectors, newSectors);


        assessRisk(analysis, simulated, simTotal);

        return analysis;
    }



    private double calculateTotalExposure(List<Investment> list) {

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

        map.replaceAll((k, v) -> (v / total) * 100.0);
        return map;
    }

    private void assessRisk(ImpactAnalysis analysis, List<Investment> portfolio, double totalVal) {

        // 1. Calculate Single Stock Concentration (Existing Logic)
        double maxStockExposure = 0;
        String biggestTicker = "None";
        Map<String, Double> tickerMap = new HashMap<>();

        for (Investment inv : portfolio) {
            double val = inv.getBuyPrice().multiply(BigDecimal.valueOf(inv.getQuantity())).doubleValue();
            tickerMap.put(inv.getTicker(), tickerMap.getOrDefault(inv.getTicker(), 0.0) + val);
        }

        for (Map.Entry<String, Double> entry : tickerMap.entrySet()) {
            if (entry.getValue() > maxStockExposure) {
                maxStockExposure = entry.getValue();
                biggestTicker = entry.getKey();
            }
        }
        double stockConcPct = (totalVal > 0) ? (maxStockExposure / totalVal) * 100 : 0;
        analysis.setHighestStockPct(stockConcPct);
        analysis.setHighestStockTicker(biggestTicker);


        // 2. Calculate Sector Concentration (New Logic)
        // We use the map we already calculated in 'SimulateTrade'
        String dominantSector = "None";
        double maxSectorPct = 0;

        for (Map.Entry<String, Double> entry : analysis.getNewSectorAllocation().entrySet()) {
            if (entry.getValue() > maxSectorPct) {
                maxSectorPct = entry.getValue();
                dominantSector = entry.getKey();
            }
        }
        long sectorCount = portfolio.stream().map(Investment::getSector).distinct().count();


        // 3. ENHANCED RISK RULES (Hierarchical Check)

        // CASE A: Extreme Single Stock Risk (> 50%)
        if (stockConcPct > 50.0) {
            analysis.setRiskLevel("CRITICAL");
            analysis.setRiskMessage("🚨 CRITICAL: You are gambling! " + biggestTicker + " is " + (int)stockConcPct + "% of your entire wealth.");
        }
        // CASE B: Extreme Sector Risk (> 60%)
        else if (maxSectorPct > 60.0) {
            analysis.setRiskLevel("HIGH");
            analysis.setRiskMessage("⚠️ HIGH RISK: Too much exposure to " + dominantSector + " (" + (int)maxSectorPct + "%). If this sector crashes, you lose.");
        }
        // CASE C: High Single Stock Risk (> 30%)
        else if (stockConcPct > 30.0) {
            analysis.setRiskLevel("HIGH");
            analysis.setRiskMessage("⚠️ CONCENTRATED: " + biggestTicker + " is becoming too dominant (" + (int)stockConcPct + "%).");
        }
        // CASE D: Poor Diversification (< 3 Sectors)
        else if (sectorCount < 3) {
            analysis.setRiskLevel("MEDIUM");
            analysis.setRiskMessage("⚠️ WARNING: Poor Diversification. You only hold assets in " + sectorCount + " sectors.");
        }
        // CASE E: Perfect Balance (> 5 Sectors, No stock > 20%)
        else if (sectorCount >= 5 && stockConcPct < 20.0) {
            analysis.setRiskLevel("EXCELLENT");
            analysis.setRiskMessage("💎 EXCELLENT: Your portfolio is perfectly balanced and defensive.");
        }
        // CASE F: Standard Healthy
        else {
            analysis.setRiskLevel("LOW");
            analysis.setRiskMessage("✅ HEALTHY: Your portfolio is well diversified.");
        }
    }

}