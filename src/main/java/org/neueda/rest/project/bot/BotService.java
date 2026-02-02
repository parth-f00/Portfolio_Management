
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

        PortfolioSummary summary = investmentService.getPortfolioSummary();
        String normalized = query.toLowerCase();

        String responseText;

        // Intent: portfolio summary
        if (normalized.contains("summary")
                || normalized.contains("summarize")
                || normalized.contains("overview")) {
            return new BotResponse(buildSummaryResponse(summary));
        }

        // Intent: risk / diversification
        else if (normalized.contains("risk")
                || normalized.contains("risky")
                || normalized.contains("safe")) {
            return new BotResponse(buildRiskResponse());
        }

        // Default fallback
        else {
            responseText =
                    "I can help summarize your portfolio, assess risk, or give insights. Try asking: 'Summarize my portfolio'.";
        }

        // AI explanation layer (safe fallback)
        try {
            return new BotResponse(aiExplanationService.explain(responseText));
        } catch (Exception e) {
            // If AI fails, return deterministic response
            return new BotResponse(responseText);
        }
    }

    private String buildSummaryResponse(PortfolioSummary summary) {
        return String.format(
                "Your portfolio contains %d assets worth %.2f in total. " +
                        "Top holding: %s. Weakest holding: %s.",
                summary.getTotalAssets(),
                summary.getTotalValue(),
                summary.getTopHolding(),
                summary.getWorstHolding()
        );
    }

    private String buildRiskResponse() {
        return "Risk analysis is based on diversification and sector exposure. " +
                "You may consider reducing overexposure to a single asset or sector.";
    }

}


