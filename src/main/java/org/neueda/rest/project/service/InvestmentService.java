package org.neueda.rest.project.service;
import org.neueda.rest.project.dto.PortfolioSummary;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.neueda.rest.project.dto.DashboardDTO;
import org.neueda.rest.project.entity.Investment;
import org.neueda.rest.project.repository.InvestmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
            dto.setPurchaseDate(inv.getPurchaseDate());
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
            String trend="neutral";
            try{
                Map<String, Object> history=finnhubService.getCompanyHistory(inv.getTicker());
                if(history.containsKey("sma")) {
                    Double sma = (Double) history.get("sma");

                    if (currentPrice.compareTo(BigDecimal.valueOf(sma))>0) {
                        trend="UP";
                    } else {
                        trend="DOWN";
                    }
                }
            }catch (Exception e){
                System.out.println("trend calculation failed for "+inv.getTicker());
            }
            dto.setTrend(trend);
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

   public List<Investment> parseCsv(MultipartFile file){
        List<Investment> previewList=new ArrayList<>();
       try(Reader reader=new InputStreamReader(file.getInputStream());
           CSVReader csvReader=new CSVReader(reader)){

           String[] headers=csvReader.readNext();
           if(headers==null){

               return null;
           }

           Map<String,Integer>headerIndexMap=new HashMap<>();
           for(int i=0;i<headers.length;i++){
               String cleanHeader=headers[i].trim().toUpperCase().replace(" ","").replace("_","");
                headerIndexMap.put(cleanHeader,i);
           }
           if(!headerIndexMap.containsKey("TICKER") && !headerIndexMap.containsKey("SYMBOL")){
               throw new RuntimeException("CSV must contain Ticker or Symbol column");
           }

           String[] row;
           while((row=csvReader.readNext())!=null){
               try {
                   Integer tickeridx = headerIndexMap.getOrDefault("TICKER", headerIndexMap.get("SYMBOL"));
                   if (tickeridx == null || tickeridx >= row.length) {
                       continue;
                   }
                   String ticker = row[tickeridx].trim().toUpperCase();
                   if (ticker.isEmpty()) {
                       continue;

                   }
                   Integer priceIdx = headerIndexMap.get("PRICE");
                   if (priceIdx == null) priceIdx = headerIndexMap.get("BUYPRICE");
                   if (priceIdx == null) priceIdx = headerIndexMap.get("COST");
                   if (priceIdx == null || priceIdx >= row.length) {
                       continue;
                   }
                   BigDecimal price = new BigDecimal(row[priceIdx].trim());

                   Integer qtyIdx = headerIndexMap.get("QUANTITY");
                   if (qtyIdx == null) qtyIdx = headerIndexMap.get("QTY");
                   if (qtyIdx == null) qtyIdx = headerIndexMap.get("SHARES");
                   if (qtyIdx == null || qtyIdx >= row.length) {
                       continue;
                   }
                   int quantity = 1;
                   if (qtyIdx != null && qtyIdx < row.length && !row[qtyIdx].trim().isEmpty()) {
                       quantity = Integer.parseInt(row[qtyIdx].trim());
                   }

                   Integer sectorIdx = headerIndexMap.get("SECTOR");
                   if (sectorIdx == null) sectorIdx = headerIndexMap.get("INDUSTRY");
                   String sector = "Other";
                   if (sectorIdx != null && sectorIdx < row.length) {
                       sector = row[sectorIdx].trim();
                   }
                   Investment inv = new Investment();
                   inv.setTicker(ticker);
                   inv.setBuyPrice(price);
                   inv.setQuantity(quantity);
                   inv.setSector(sector);


//                   repository.save(inv);
                   previewList.add(inv);
               }catch (Exception e){
                   //log and continue
                   System.out.println("Error processing row: "+ Arrays.toString(row)+" , error: "+e.getMessage());
               }
           }
       } catch (IOException | RuntimeException  |CsvValidationException e) {
           throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
       }
       return previewList;
   }

   public List<Investment> saveAll(List<Investment> investments){
        return repository.saveAll(investments);
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
