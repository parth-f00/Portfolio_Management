
package org.neueda.rest.project.bot;

import org.neueda.rest.project.ai.AIExplanationService;
import org.neueda.rest.project.dto.DashboardDTO;
import org.neueda.rest.project.dto.PortfolioSummary;
import org.springframework.stereotype.Service;
import org.neueda.rest.project.service.InvestmentService;

import java.util.List;
import java.util.Map;


@Service
public class BotService {

    //Core portfolio service used to fetch and compute portfolio data
    private final InvestmentService investmentService;
    private final AIExplanationService aiExplanationService;
    private BotIntent lastIntent = null;
    private BotIntent lastResolvedIntent = null;



    //Injecting portfolio business logic into the bot layer
    public BotService(InvestmentService investmentService,
                      AIExplanationService aiExplanationService) {
        this.investmentService = investmentService;
        this.aiExplanationService = aiExplanationService;
    }


    //Processes user query and returns portfolio service
    public BotResponse processQuery(String query) {

        String normalized = query.toLowerCase();
        BotIntent intent = detectIntent(normalized);

// Handle implicit references like "this", "that", "and that"
        if (intent == BotIntent.UNKNOWN && isImplicitReference(normalized)) {
            intent = resolveImplicitIntent();
        }

        if (intent == BotIntent.UNKNOWN && lastIntent != null) {
            intent = resolveFollowUp(normalized, lastIntent);
        }


        switch (intent) {

            case SUMMARY:
                lastIntent = BotIntent.SUMMARY;
                lastResolvedIntent = intent;
                return respondWithAI(
                        buildSummaryResponse(),
                        "SUMMARY",
                        List.of("Check risk", "View sector allocation", "Get advice")
                );

            case VALUE:
                lastIntent = BotIntent.VALUE;
                lastResolvedIntent = intent;
                return respondWithAI(
                        buildTotalValueResponse(),
                        "VALUE",
                        List.of("Summarize portfolio", "Check risk")
                );

            case SECTOR:
                lastIntent = BotIntent.SECTOR;
                lastResolvedIntent = intent;
                return respondWithAI(
                        buildSectorResponse(),
                        "SECTOR",
                        List.of("Check risk", "Get advice")
                );

            case BEST_WORST_PERFORMER:
                lastIntent = BotIntent.BEST_WORST_PERFORMER;
                lastResolvedIntent = intent;
                return respondWithAI(
                        buildBestWorstPerformerResponse(),
                        "BEST_WORST_PERFORMER",
                        List.of("Check profit/loss", "View sectors", "Check risk")
                );


            case ADVISOR_INSIGHT:
                lastIntent = BotIntent.ADVISOR_INSIGHT;
                lastResolvedIntent = intent;
                return respondWithAI(
                        buildAdvisorInsightResponse(),
                        "ADVISOR_INSIGHT",
                        List.of("Check risk", "View sectors", "Check profit/loss")
                );


            case HELP:
                return respondWithAI(
                        buildHelpResponse(),
                        "HELP",
                        List.of(
                                "Summarize my portfolio",
                                "What is my portfolio value?",
                                "Is my portfolio risky?",
                                "Any advice?"
                        )
                );

            case PROFIT_LOSS:
                lastIntent = BotIntent.PROFIT_LOSS;
                lastResolvedIntent = intent;
                return respondWithAI(
                        buildProfitLossResponse(),
                        "PROFIT_LOSS",
                        List.of("View sectors", "Check risk", "Get advice")
                );

            case RISK_LEVEL:
                lastIntent = BotIntent.RISK_LEVEL;
                lastResolvedIntent = intent;
                return respondWithAI(
                        buildRiskLevelResponse(),
                        "RISK_LEVEL",
                        List.of("View sectors", "Check profit/loss", "Get advice")
                );

            case MOST_INVESTED_SECTOR:
                lastIntent = BotIntent.MOST_INVESTED_SECTOR;
                lastResolvedIntent = intent;
                return respondWithAI(
                        buildMostInvestedSectorResponse(),
                        "MOST_INVESTED_SECTOR",
                        List.of("Check risk", "View sectors", "Get advice")
                );


            default:
                return respondWithAI(
                        "I didn’t understand that. Type 'help' to see what I can do.",
                        "UNKNOWN",
                        List.of("Help", "Summarize my portfolio")
                );

        }
    }

    private BotIntent detectIntent(String q) {

        if (q.contains("summary") || q.contains("summarize") || q.contains("overview")) {
            return BotIntent.SUMMARY;
        }
        if (q.contains("value") || q.contains("worth")) {
            return BotIntent.VALUE;
        }
        if (q.contains("sector")) {
            return BotIntent.SECTOR;
        }
        if (q.contains("best performer")
                || q.contains("worst performer")
                || q.contains("best stock")
                || q.contains("worst stock")
                || q.contains("biggest gainer")
                || q.contains("biggest loser")) {
            return BotIntent.BEST_WORST_PERFORMER;
        }

        if (q.contains("improve")
                || q.contains("advisor")
                || q.contains("insight")
                || q.contains("recommend")
                || q.contains("what should i do")) {
            return BotIntent.ADVISOR_INSIGHT;
        }

        if (q.contains("help") || q.contains("can you do")) {
            return BotIntent.HELP;
        }
        if (q.contains("profit") || q.contains("loss") || q.contains("p&l")) {
            return BotIntent.PROFIT_LOSS;
        }
        if (q.contains("risk") || q.contains("risky") || q.contains("safe")) {
            return BotIntent.RISK_LEVEL;
        }
        if (q.contains("most invested")
                || q.contains("largest sector")
                || q.contains("where am i invested")
                || q.contains("highest exposure")) {
            return BotIntent.MOST_INVESTED_SECTOR;
        }


        return BotIntent.UNKNOWN;
    }

    private BotIntent resolveFollowUp(String q, BotIntent previous) {

        if (q.contains("risk")) {
            return BotIntent.RISK_LEVEL;
        }
        if (q.contains("advice") || q.contains("suggest")) {
            return BotIntent.ADVISOR_INSIGHT;
        }
        if (q.contains("sector")) {
            return BotIntent.SECTOR;
        }
        if (q.contains("value") || q.contains("worth")) {
            return BotIntent.VALUE;
        }
        if (q.contains("what about") || q.contains("and")) {
            return previous;
        }

        return BotIntent.UNKNOWN;
    }

    private BotResponse respondWithAI(String text, String intent, List<String> suggestions) {

        String finalText = text;

        try {
            finalText = aiExplanationService.explain(text);
        } catch (Exception ignored) {
            // fallback already handled
        }

        return new BotResponse(finalText, intent, suggestions);
    }

    private boolean isImplicitReference(String q) {
        return q.equals("this")
                || q.equals("that")
                || q.equals("it")
                || q.contains("what about that")
                || q.contains("and that")
                || q.contains("explain that");
    }

    private BotIntent resolveImplicitIntent() {

        if (lastResolvedIntent == null) {
            return BotIntent.UNKNOWN;
        }

        return switch (lastResolvedIntent) {

            case SUMMARY -> BotIntent.SUMMARY;

            case SECTOR -> BotIntent.RISK_LEVEL;

            case RISK_LEVEL -> BotIntent.ADVISOR_INSIGHT;

            case PROFIT_LOSS -> BotIntent.BEST_WORST_PERFORMER;

            case BEST_WORST_PERFORMER -> BotIntent.ADVISOR_INSIGHT;

            default -> lastResolvedIntent;
        };
    }

    private String buildBestWorstPerformerResponse() {

        List<DashboardDTO> dashboard = investmentService.showPortfolio();

        if (dashboard.isEmpty()) {
            return "Your portfolio is empty, so best and worst performers cannot be determined.";
        }

        DashboardDTO best = null;
        DashboardDTO worst = null;

        for (DashboardDTO dto : dashboard) {
            if (dto.getProfitLoss() == null) continue;

            if (best == null || dto.getProfitLoss().compareTo(best.getProfitLoss()) > 0) {
                best = dto;
            }

            if (worst == null || dto.getProfitLoss().compareTo(worst.getProfitLoss()) < 0) {
                worst = dto;
            }
        }

        if (best == null || worst == null) {
            return "Unable to calculate best and worst performers at the moment.";
        }

        return String.format(
                "Best performer: %s (%+.2f). Worst performer: %s (%+.2f).",
                best.getTicker(),
                best.getProfitLoss().doubleValue(),
                worst.getTicker(),
                worst.getProfitLoss().doubleValue()
        );
    }

    private String buildSummaryResponse() {
        PortfolioSummary s = investmentService.getPortfolioSummary();
        return String.format(
                "Your portfolio contains %d assets worth %.2f in total. " +
                        "Top holding: %s. Weakest holding: %s.",
                s.getTotalAssets(),
                s.getTotalValue(),
                s.getTopHolding(),
                s.getWorstHolding()
        );
    }

    private String buildTotalValueResponse() {
        double value = investmentService.getTotalPortfolioValue();
        return "Your total portfolio value is " + String.format("%.2f", value) + ".";
    }

    private String buildRiskLevelResponse() {

        Map<String, Double> sectorMap = investmentService.getSectorDisribution();
        int assetCount = investmentService.getPortfolioSummary().getTotalAssets();

        if (sectorMap.isEmpty() || assetCount == 0) {
            return "Risk level cannot be determined because your portfolio is empty.";
        }

        double total = sectorMap.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        double maxSectorPercent = sectorMap.values()
                .stream()
                .mapToDouble(v -> (v / total) * 100)
                .max()
                .orElse(0.0);

        String riskLevel;
        String reason;

        if (assetCount < 3 || maxSectorPercent > 60) {
            riskLevel = "High";
            reason = "Low diversification or heavy concentration in a single sector.";
        } else if (maxSectorPercent > 40) {
            riskLevel = "Medium";
            reason = "Moderate concentration in one sector.";
        } else {
            riskLevel = "Low";
            reason = "Good diversification across sectors.";
        }

        return String.format(
                "Risk level: %s. Reason: %s",
                riskLevel,
                reason
        );
    }

    private String buildMostInvestedSectorResponse() {

        Map<String, Double> sectorMap = investmentService.getSectorDisribution();

        if (sectorMap.isEmpty()) {
            return "Your portfolio does not have enough data to determine the most invested sector.";
        }

        double total = sectorMap.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        Map.Entry<String, Double> maxEntry = sectorMap.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if (maxEntry == null || total == 0) {
            return "Unable to calculate the most invested sector at the moment.";
        }

        double percentage = (maxEntry.getValue() / total) * 100;

        return String.format(
                "You are most invested in the %s sector, which accounts for %.2f%% of your portfolio.",
                maxEntry.getKey(),
                percentage
        );
    }


    private String buildSectorResponse() {

        Map<String, Double> sectorMap = investmentService.getSectorDisribution();

        if (sectorMap.isEmpty()) {
            return "Your portfolio does not have any sector data yet.";
        }

        double total = sectorMap.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        StringBuilder sb = new StringBuilder("Sector-wise allocation:\n");

        for (Map.Entry<String, Double> entry : sectorMap.entrySet()) {
            double percentage = (entry.getValue() / total) * 100;
            sb.append(String.format(
                    "- %s: %.2f%%\n",
                    entry.getKey(),
                    percentage
            ));
        }

        return sb.toString();
    }

    private String buildProfitLossResponse() {

        List<DashboardDTO> dashboard = investmentService.showPortfolio();

        if (dashboard.isEmpty()) {
            return "Your portfolio is empty, so profit or loss cannot be calculated yet.";
        }

        double totalPL = 0.0;

        for (DashboardDTO dto : dashboard) {
            if (dto.getProfitLoss() != null) {
                totalPL += dto.getProfitLoss().doubleValue();
            }
        }

        if (totalPL > 0) {
            return String.format(
                    "Your portfolio is currently in profit of %.2f.",
                    totalPL
            );
        } else if (totalPL < 0) {
            return String.format(
                    "Your portfolio is currently in loss of %.2f.",
                    Math.abs(totalPL)
            );
        } else {
            return "Your portfolio is currently break-even.";
        }
    }


    private String buildAdvisorInsightResponse() {

        String risk = buildRiskLevelResponse();
        String sectors = buildSectorResponse();
        String pl = buildProfitLossResponse();

        return """
    Portfolio Advisor Insight:
    %s
    %s
    %s
    """.formatted(risk, sectors, pl);
    }

    private String buildHelpResponse() {
        return """
    I can help you with:
    - Portfolio summary
    - Total value
    - Risk analysis
    - Sector allocation
    - Investment suggestions
    """;
    }


}


