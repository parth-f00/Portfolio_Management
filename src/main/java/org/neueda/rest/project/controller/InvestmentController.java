package org.neueda.rest.project.controller;

import org.neueda.rest.project.dto.DashboardDTO;
import org.neueda.rest.project.entity.Investment;
import org.neueda.rest.project.service.FinnhubService;
import org.neueda.rest.project.service.InvestmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class InvestmentController {
    @Autowired
    private InvestmentService investmentService;

    @Autowired
    private FinnhubService finnhubService;

    @GetMapping("/")
    public List<DashboardDTO> getPortfolio() {
        return investmentService.showPortfolio();
    }


    @PostMapping("/")
    public Investment addInvestment(@RequestBody Investment investment) {
        return investmentService.save(investment);
    }

    @DeleteMapping("/{id}")
    public void deleteInvestment(@PathVariable Long id) {
        investmentService.deleteById(id);
    }

//    @GetMapping("/history/{ticker}")
//    public Map<String, Object> getInvestmentHistory(@PathVariable String ticker){
//        return finnhubService.getCompanyHistory(ticker);
//    }

    @GetMapping("/total-value")
        public Double getTotalValue(){
            return investmentService.getTotalPortfolioValue();
        }


     @GetMapping("/sector-distrinbution")
        public Map<String, Double> getSectorDistribution(){
            return investmentService.getSectorDisribution();
        }


      @GetMapping("/history/portfolio")
        public Map<String,Double> getPortfolioHistory(){
            return investmentService.getPortfolioHIstory();
        }

        @GetMapping("/recommendations/portfolio")
        public List<String> getAdvisorSuggestions(){
            return investmentService.getAdvisorSUggestions();
        }


}
