package org.neueda.rest.project.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



@Service
public class FinnhubService {

    @Value("${finnhub.api.url}")
    private String apiUrl;

    @Value("${finnhub.api.key}")
    private String apiKey;

//    @Value("${tiingo.api.key}")
//    private String tiingoKey;

//    public Map<String,Object> getHistory(String ticker){
//        try {
////            long to= Instant.now().getEpochSecond();
////            long to= 1735708800L;
////            long from=1738214400L;
////            long from=Instant.now().minus(30, ChronoUnit.DAYS).getEpochSecond();
////            String url=apiUrl.replace("quote?symbol=","stock/candle?symbol=")
////                    +ticker
////                    +"&resolution=D&from="+from+"&to="+to+"&token="+apiKey;
//            String url="https://finnhub.io/api/v1/stock/candle?symbol="
//                    +ticker
//                    +"&resolution=D&from="+from+"&to="+to+"&token="+apiKey;
//            System.out.println("fetchinghistory"+url);
////             apiUrl= apiUrl+"stock/candle?symbol=";
////            String finalUrl= apiUrl+ticker+"/history?token="+apiKey;
//            RestTemplate restTemplate= new RestTemplate();
//            Map<String,Object> response= restTemplate.getForObject(url, Map.class);
//            return response;
//        } catch (Exception e) {
////            throw new RuntimeException("Failed to fetch stock history from Finnhub", e);
//            System.out.println("Error fetching stock history: "+ticker + e.getMessage());
//            return Map.of("error","Failed to fetch stock history");
//        }
//    }

    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes
    private final Map<String, CachedItem<BigDecimal>> priceCache = new ConcurrentHashMap<>();
    private final Map<String, CachedItem<Map<String, Object>>> historyCache = new ConcurrentHashMap<>();

    private static class CachedItem<T>{
        T data;
        long timestamp;

        public CachedItem(T data){
            this.data=data;
            this.timestamp=System.currentTimeMillis();
        }

        boolean isExpired(){
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }




    public Map<String,Object> getCompanyHistory(String Ticker){
        if(historyCache.containsKey(Ticker)){
            CachedItem<Map<String,Object>> cachedItem= historyCache.get(Ticker);
            if(!cachedItem.isExpired()){
                System.out.println("Cache hit for history: "+Ticker);
                return cachedItem.data;
            }
        }
        System.out.println("Cache miss for history: "+Ticker);


        String tiingoKey = "I_AM_A_BAD_KEY";
        String startDate= LocalDate.now().minusDays(30).toString();
//        String startDate="2023-01-01";
        String url="https://api.tiingo.com/tiingo/daily/"+Ticker+"/prices?startDate="+startDate+"&token="+tiingoKey;
//https://api.tiingo.com/tiingo/daily/aapl/prices?startDate=2023-01-01&token=ed6323f960843c766b96a3ffe4ed866599aa1fa9
        try {
            RestTemplate restTemplate= new RestTemplate();
            List<Map<String,Object>> response= restTemplate.getForObject(url, List.class);
            if(response==null || response.isEmpty()){
                System.out.println("API RETURNED EMPTY, SWITCHING TO DUMMY DATA");
//                return Map.of("s", "no_data","error","Empty response from tiingo");
                return getMockHistory(Ticker);
            }

            List<Double> closes=new ArrayList<>();
            List<Long> timestamps=new ArrayList<>();
            double sum=0;
            DateTimeFormatter formatter=DateTimeFormatter.ISO_INSTANT;
            for(Map<String,Object> day: response){
                Double price=Double.valueOf(day.get("close").toString());
                closes.add(price);

                String rawDate=day.get("date").toString();
                long unixTime=java.time.Instant.parse(rawDate).getEpochSecond();
                timestamps.add(unixTime);
            }
            int windowSize=30;
            List<Double> trendPrices;

            if(closes.size()>windowSize){
                trendPrices=closes.subList( closes.size()-windowSize,closes.size());
            }
            else{
                trendPrices=closes;
            }

            for(Double p: trendPrices){
                sum+=p;
            }
            double sma=sum/trendPrices.size();

            Map<String, Object> finalResult= new HashMap<>();
            finalResult.put("c", closes);
            finalResult.put("t", timestamps);
            finalResult.put("sma", sma);
            finalResult.put("s", "ok");
            finalResult.put("prices", response);
            historyCache.put(Ticker, new CachedItem<>(finalResult));
            return finalResult;
        } catch (Exception e) {
            System.out.println("Error fetching company history: "+Ticker + e.getMessage());
//            return Map.of("s", "no_data","error","Failed to fetch company history");
            return getMockHistory(Ticker);
        }
    }

     private Map<String,Object> getMockHistory(String ticker){

        List<Double> closes=new ArrayList<>();
        List<Long> timestamps=new ArrayList<>();

        List<Map<String, Object>> priceList=new ArrayList<>();

        double currentPrice=240+(Math.random()*100);
        long now= LocalDate.now().atStartOfDay(java.time.ZoneOffset.UTC).toEpochSecond();;

        for(int i=30;i>0;i--) {
            double change = (Math.random() - 0.5) * 5;
            currentPrice += change;

            double price = Math.round(currentPrice * 100.0) / 100.0;
            long timeStamp= now-(i*86400L);
            String dateStr= Instant.ofEpochSecond(timeStamp).toString();
            closes.add(price);
            timestamps.add(timeStamp);

            Map<String, Object>dayEntry= new HashMap<>();
            dayEntry.put("date", dateStr);
            dayEntry.put("close", price);
            priceList.add(dayEntry);
        }
            double sum=0;
            for(Double p:closes){
                sum+=p;
            }
            double sma=sum/closes.size();
            Map<String, Object> mock=new HashMap<>();
            mock.put("c", closes);
            mock.put("t",timestamps);
            mock.put("sma",sma);
            mock.put("s", "ok");
            mock.put("prices",priceList);
            historyCache.put(ticker,new CachedItem<>(mock));
            return mock;
        }


    public BigDecimal getStockPrice(String ticker){
        if(priceCache.containsKey(ticker)){
            CachedItem<BigDecimal> cachedItem= priceCache.get(ticker);
            if(!cachedItem.isExpired()){
                System.out.println("Cache hit for price: "+ticker);
                return cachedItem.data;
            }
        }
        System.out.println("Cache miss for price: "+ticker);
        try {
            String finalUrl= apiUrl+ticker+"&token="+apiKey;
            RestTemplate restTemplate= new RestTemplate();
            FinnhubResponse response= restTemplate.getForObject(finalUrl, FinnhubResponse.class);
            if(response!=null && response.getCurrentPrice()!=null){
                BigDecimal price= BigDecimal.valueOf(response.getCurrentPrice());
                priceCache.put(ticker,new CachedItem<>(price));
                return BigDecimal.valueOf(response.getCurrentPrice());
            }
            return BigDecimal.ZERO;
//            return BigDecimal.valueOf(150.00); // Dummy stock price
        } catch (Exception e) {
//            throw new RuntimeException("Failed to fetch stock price from Finnhub", e);
            System.out.println("Error fetching stock price: "+ticker + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Data
    static class FinnhubResponse{
         @JsonProperty("c")
        private Double currentPrice;

    }

}
