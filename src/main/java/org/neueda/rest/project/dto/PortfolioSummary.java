package org.neueda.rest.project.dto;

public class PortfolioSummary {

    private double totalValue;
    private int totalAssets;
    private String topHolding;
    private String worstHolding;

    public PortfolioSummary(double totalValue, int totalAssets,
                            String topHolding, String worstHolding) {
        this.totalValue = totalValue;
        this.totalAssets = totalAssets;
        this.topHolding = topHolding;
        this.worstHolding = worstHolding;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public int getTotalAssets() {
        return totalAssets;
    }

    public String getTopHolding() {
        return topHolding;
    }

    public String getWorstHolding() {
        return worstHolding;
    }
}
