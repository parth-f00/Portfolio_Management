package org.neueda.rest.project.service;
import org.neueda.rest.project.dto.PortfolioSummary;

import org.neueda.rest.project.dto.DashboardDTO;
import org.neueda.rest.project.entity.Investment;
import org.neueda.rest.project.repository.InvestmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class InvestmentService {

    @Autowired
    private InvestmentRepository repository;

    @Autowired
    private FinnhubService finnhubService;

    public List<DashboardDTO> showPortfolio(){
        List<Investment> investments=repository.findAll();
        List<DashboardDTO>dashboard=new ArrayList<>();

        for(int i=0;i<investments.size();i++){
            Investment inv=investments.get(i);
            DashboardDTO dto=new DashboardDTO();
            dto.setSector(inv.getSector());
            dto.setId(inv.getId());
            dto.setTicker(inv.getTicker());
            dto.setQuantity(inv.getQuantity());
            dto.setBuyPrice(inv.getBuyPrice());
//            dto.setCompanyName(inv.getCompany);
            BigDecimal currentPrice=finnhubService.getStockPrice(inv.getTicker());
            dto.setCurrentPrice(currentPrice);
//            dto.setTotalReturn((currentPrice-inv.getPurchasePrice())*inv.getQuantity());

            BigDecimal diff=currentPrice.subtract(inv.getBuyPrice());
            BigDecimal quantity=BigDecimal.valueOf(inv.getQuantity());
            BigDecimal pl=diff.multiply(quantity);
            dto.setProfitLoss(pl);

            if(inv.getBuyPrice().compareTo(BigDecimal.ZERO)>0) {
                BigDecimal percent = diff.divide(inv.getBuyPrice(), 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
                dto.setPercentageChange(percent.doubleValue());
            }
            else{
                dto.setPercentageChange(0.0);
            }

            dashboard.add(dto);

        }
        return dashboard;
    }

    public Investment save(Investment investment){
        return repository.save(investment);
    }

    public void deleteById(Long id){
        repository.deleteById(id);
    }

   public Map<String,Double> getSectorDisribution(){
        List<Investment> investments= repository.findAll();
        Map<String,Double> distribution=new HashMap<>();

        Map<String,Double> priceCache=new HashMap<>();



        for(Investment inv:investments){
            String ticker=inv.getTicker();
            double price;
            if(priceCache.containsKey(ticker)){
                price=priceCache.get(ticker);
            }
            else{
                BigDecimal priceBd=finnhubService.getStockPrice(inv.getTicker());
                price=(priceBd!=null)? priceBd.doubleValue() : inv.getBuyPrice().doubleValue();
                priceCache.put(ticker,price);
            }

            Double totalValue=price*inv.getQuantity();
            String sector=inv.getSector();
            if(sector==null||sector.isEmpty()){
                sector="Others";
            }
            distribution.put(sector,distribution.getOrDefault(sector,0.0)+totalValue);

        }
        return distribution;
   }

   public Double getTotalPortfolioValue(){
        List<Investment> investments= repository.findAll();
        double totalValue=0.0;

        Map<String,Double> priceCache=new HashMap<>();

        for(Investment inv:investments){
            String ticker=inv.getTicker();
            double price;
            if(priceCache.containsKey(ticker)){
                price=priceCache.get(ticker);
            }
            else{
                BigDecimal priceBd=finnhubService.getStockPrice(inv.getTicker());
//                price=(priceBd!=null)? priceBd.doubleValue() : inv.getBuyPrice().doubleValue();
                price=priceBd.doubleValue();
                priceCache.put(ticker,price);
            }
            totalValue+=price*inv.getQuantity();
        }
        return totalValue;
   }

   public Map<String, Double> getPortfolioHIstory(){
        List<Investment> investments= repository.findAll();

        Map<String,Double>historyMap=new TreeMap<>();// keeps sorted by date

       for(Investment inv:investments){
           Map<String,Object>StockHistory=finnhubService.getCompanyHistory(inv.getTicker());
           List<Map<String,Object>> priceList=(List<Map<String,Object>>)StockHistory.get("prices");
           if(priceList==null){
               continue;
           }
           for(Map<String,Object> day:priceList){
               String date= (String)day.get("date");
               Object closeObj=day.get("close");
               Double price=(closeObj instanceof Double) ? (Double)closeObj : ((Integer)closeObj).doubleValue();
               double valueOnThatDay=price*inv.getQuantity();
                historyMap.put(date,historyMap.getOrDefault(date,0.0)+valueOnThatDay);
           }
       }
           return historyMap;

   }


   public List<String> getAdvisorSUggestions() {
       List<String> suggestions = new ArrayList<>();
       Map<String, Double> sectorDistribution = getSectorDisribution();
       double totalValue = 0.0;
       for (Double val : sectorDistribution.values()) {
           totalValue += val;
       }
       if (totalValue == 0.0) {
           suggestions.add("Your portfolio is empty. Consider adding investments to diversify your portfolio.");
           return suggestions;
       }
       for (Map.Entry<String, Double> entry : sectorDistribution.entrySet()) {
           String sector = entry.getKey();
           Double value = entry.getValue();
           double percentage = (value / totalValue) * 100;

           if (percentage > 40.0) {
               suggestions.add("Your investment in the " + sector + " sector is quite high (" + String.format("%.2f", percentage) + "%). Consider diversifying into other sectors to reduce risk.");
           } else if (percentage < 10.0) {
               suggestions.add("Your investment in the " + sector + " sector is relatively low (" + String.format("%.2f", percentage) + "%). You might want to explore opportunities in this sector for better diversification.");
           } else {
               suggestions.add("Your investment in the " + sector + " sector is well-balanced (" + String.format("%.2f", percentage) + "%). Keep up the good work!");
           }
       }
       return suggestions;
   }

    public PortfolioSummary getPortfolioSummary() {

        List<Investment> investments = repository.findAll();

        if (investments.isEmpty()) {
            return new PortfolioSummary(0.0, 0, "N/A", "N/A");
        }

        double totalValue = 0.0;
        String topHolding = "";
        String worstHolding = "";
        double maxValue = Double.MIN_VALUE;
        double minValue = Double.MAX_VALUE;

        Map<String, Double> priceCache = new HashMap<>();

        for (Investment inv : investments) {

            String ticker = inv.getTicker();
            double price;

            if (priceCache.containsKey(ticker)) {
                price = priceCache.get(ticker);
            } else {
                BigDecimal priceBd = finnhubService.getStockPrice(ticker);
                price = (priceBd != null)
                        ? priceBd.doubleValue()
                        : inv.getBuyPrice().doubleValue();
                priceCache.put(ticker, price);
            }

            double value = price * inv.getQuantity();
            totalValue += value;

            if (value > maxValue) {
                maxValue = value;
                topHolding = ticker;
            }

            if (value < minValue) {
                minValue = value;
                worstHolding = ticker;
            }
        }

        return new PortfolioSummary(
                totalValue,
                investments.size(),
                topHolding,
                worstHolding
        );
    }

}
