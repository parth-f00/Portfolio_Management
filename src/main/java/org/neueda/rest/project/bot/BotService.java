
package org.neueda.rest.project.bot;

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

        double totalValue = investmentService.getTotalPortfolioValue();

        //Simple rule-based response
        String response = "Your total portfolio value is " + totalValue;

        return new BotResponse(response);
    }
}


