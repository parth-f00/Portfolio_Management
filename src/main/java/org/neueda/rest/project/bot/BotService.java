
package org.neueda.rest.project.bot;

import org.neueda.rest.project.ai.AIExplanationService;
import org.neueda.rest.project.dto.PortfolioSummary;
import org.springframework.stereotype.Service;
import org.neueda.rest.project.service.InvestmentService;


@Service
public class BotService {

    //Core portfolio service used to fetch and compute portfolio data
    private final InvestmentService investmentService;
    private final AIExplanationService aiExplanationService;


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

        switch (intent) {

            case SUMMARY:
                return new BotResponse(buildSummaryResponse());

            case VALUE:
                return new BotResponse(buildTotalValueResponse());

            case RISK:
                return new BotResponse(buildRiskResponse());

            case SECTOR:
                return new BotResponse(buildSectorResponse());

            case BEST_WORST:
                return new BotResponse(buildBestWorstResponse());

            case ADVICE:
                return new BotResponse(buildAdviceResponse());

            case HELP:
                return new BotResponse(buildHelpResponse());

            default:
                return new BotResponse(
                        "I didn’t understand that. Type 'help' to see what I can do."
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


