
package org.neueda.rest.project.bot;

import org.neueda.rest.project.dto.PortfolioSummary;
import org.springframework.stereotype.Service;
import org.neueda.rest.project.service.InvestmentService;

@Service
public class BotService {

    //Core portfolio service used to fetch and compute portfolio data
    private final InvestmentService investmentService;

    //Injecting portfolio business logic into the bot layer
    public BotService(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    //Processes user query and returns portfolio service
    public BotResponse processQuery(String query) {

        PortfolioSummary summary = investmentService.getPortfolioSummary();


        //Simple rule-based response
        String response = String.format(
                "Your portfolio has %d assets with a total value of %.2f. " +
                        "Top holding: %s. Weakest holding: %s.",
                summary.getTotalAssets(),
                summary.getTotalValue(),
                summary.getTopHolding(),
                summary.getWorstHolding()
        );

        return new BotResponse(response);
    }
}


