
package org.neueda.rest.project.bot;

import org.neueda.rest.project.ai.AIExplanationService;
import org.neueda.rest.project.dto.PortfolioSummary;
import org.springframework.stereotype.Service;
import org.neueda.rest.project.service.InvestmentService;

import java.util.List;


@Service
public class BotService {

    //Core portfolio service used to fetch and compute portfolio data
    private final InvestmentService investmentService;
    private final AIExplanationService aiExplanationService;
    private BotIntent lastIntent = null;


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

        if (intent == BotIntent.UNKNOWN && lastIntent != null) {
            intent = resolveFollowUp(normalized, lastIntent);
        }


        switch (intent) {

            case SUMMARY:
                lastIntent = BotIntent.SUMMARY;
                return new BotResponse(
                        buildSummaryResponse(),
                        "SUMMARY",
                        List.of("Check risk", "View sector allocation", "Get advice")
                );

            case VALUE:
                lastIntent = BotIntent.VALUE;
                return new BotResponse(
                        buildTotalValueResponse(),
                        "VALUE",
                        List.of("Summarize portfolio", "Check risk")
                );

            case RISK:
                lastIntent = BotIntent.RISK;
                return new BotResponse(
                        buildRiskResponse(),
                        "RISK",
                        List.of("Get advice", "View sectors")
                );

            case SECTOR:
                lastIntent = BotIntent.SECTOR;
                return new BotResponse(
                        buildSectorResponse(),
                        "SECTOR",
                        List.of("Check risk", "Get advice")
                );

            case BEST_WORST:
                lastIntent = BotIntent.BEST_WORST;
                return new BotResponse(
                        buildBestWorstResponse(),
                        "BEST_WORST",
                        List.of("Summarize portfolio", "Check risk")
                );

            case ADVICE:
                lastIntent = BotIntent.ADVICE;
                return new BotResponse(
                        buildAdviceResponse(),
                        "ADVICE",
                        List.of("Summarize portfolio", "Check risk")
                );

            case HELP:
                return new BotResponse(
                        buildHelpResponse(),
                        "HELP",
                        List.of(
                                "Summarize my portfolio",
                                "What is my portfolio value?",
                                "Is my portfolio risky?",
                                "Any advice?"
                        )
                );

            default:
                return new BotResponse(
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
        if (q.contains("risk") || q.contains("risky")) {
            return BotIntent.RISK;
        }
        if (q.contains("sector")) {
            return BotIntent.SECTOR;
        }
        if (q.contains("best") || q.contains("worst") || q.contains("top")) {
            return BotIntent.BEST_WORST;
        }
        if (q.contains("advice") || q.contains("suggest")) {
            return BotIntent.ADVICE;
        }
        if (q.contains("help") || q.contains("can you do")) {
            return BotIntent.HELP;
        }

        return BotIntent.UNKNOWN;
    }

    private BotIntent resolveFollowUp(String q, BotIntent previous) {

        if (q.contains("risk")) {
            return BotIntent.RISK;
        }
        if (q.contains("advice") || q.contains("suggest")) {
            return BotIntent.ADVICE;
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

    private String buildRiskResponse() {
        return "Your portfolio risk depends on diversification and sector concentration.";
    }

    private String buildSectorResponse() {
        return "You can view sector-wise allocation to understand diversification.";
    }

    private String buildBestWorstResponse() {
        PortfolioSummary s = investmentService.getPortfolioSummary();
        return "Best holding: " + s.getTopHolding() +
                ". Weakest holding: " + s.getWorstHolding() + ".";
    }

    private String buildAdviceResponse() {
        return String.join(" ", investmentService.getAdvisorSUggestions());
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


