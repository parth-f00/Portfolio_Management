package org.neueda.rest.project.controller;

import org.neueda.rest.project.dto.DashboardDTO;
import org.neueda.rest.project.dto.ImpactAnalysis;
import org.neueda.rest.project.entity.Investment;
import org.neueda.rest.project.service.FinnhubService;
import org.neueda.rest.project.service.InvestmentService;
import org.neueda.rest.project.service.Simulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "*")
public class InvestmentController {
    @Autowired
    private InvestmentService investmentService;

    @Autowired
    private FinnhubService finnhubService;

    @Autowired
    private Simulator simulatorService;

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

        @PostMapping("/preview-csv")
        public List<Investment> previewCSV(@RequestParam("file")MultipartFile file){
            return investmentService.parseCsv(file);
        }

        @PostMapping("/batch")
        public List<Investment> saveBatch(@RequestBody List<Investment> investments){
            return investmentService.saveAll(investments);
        }

        @PostMapping("/simulate")
        public ImpactAnalysis simulateImpact(@RequestBody Investment investment){
          return simulatorService.SimulateTrade(investment);
        }

}
