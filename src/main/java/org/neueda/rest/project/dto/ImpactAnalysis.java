package org.neueda.rest.project.dto;

import java.util.Map;

public class ImpactAnalysis {

    // SECTOR SHIFT (Before vs After)
    private Map<String, Double> oldSectorAllocation;
    private Map<String, Double> newSectorAllocation;

    // RISK ASSESSMENT
    private String riskLevel;       // "LOW", "MEDIUM", "HIGH"
    private String riskMessage;     // "Concentration in Tech is too high"
    private double highestStockPct; // "AAPL is 40% of portfolio"
    private String highestStockTicker;

    public ImpactAnalysis(Map<String, Double> oldSec, Map<String, Double> newSec) {
        this.oldSectorAllocation = oldSec;
        this.newSectorAllocation = newSec;
    }

    // Getters and Setters...
    public Map<String, Double> getOldSectorAllocation() { return oldSectorAllocation; }
    public Map<String, Double> getNewSectorAllocation() { return newSectorAllocation; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getRiskMessage() { return riskMessage; }
    public void setRiskMessage(String riskMessage) { this.riskMessage = riskMessage; }

    public double getHighestStockPct() { return highestStockPct; }
    public void setHighestStockPct(double highestStockPct) { this.highestStockPct = highestStockPct; }

    public String getHighestStockTicker() { return highestStockTicker; }
    public void setHighestStockTicker(String highestStockTicker) { this.highestStockTicker = highestStockTicker; }
}