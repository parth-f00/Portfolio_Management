package org.neueda.rest.project.ServiceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neueda.rest.project.dto.DashboardDTO;
import org.neueda.rest.project.dto.PortfolioSummary;
import org.neueda.rest.project.entity.Investment;
import org.neueda.rest.project.repository.InvestmentRepository;
import org.neueda.rest.project.service.FinnhubService;
import org.neueda.rest.project.service.InvestmentService;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InvestmentServiceTest {

    private InvestmentService service;
    private InvestmentRepository repository;
    private FinnhubService finnhubService;

    @BeforeEach
    public void setUp() throws Exception {
        repository = mock(InvestmentRepository.class);
        finnhubService = mock(FinnhubService.class);
        service = new InvestmentService();

        // inject mocks
        Field repoField = InvestmentService.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(service, repository);

        Field fhField = InvestmentService.class.getDeclaredField("finnhubService");
        fhField.setAccessible(true);
        fhField.set(service, finnhubService);
    }

    @Test
    public void showPortfolio_buildsDashboardCorrectly() {
        Investment inv = new Investment(1L, "AAPL", "Tech", 2, BigDecimal.valueOf(100.0), LocalDateTime.now());
        when(repository.findAll()).thenReturn(List.of(inv));
        when(finnhubService.getStockPrice("AAPL")).thenReturn(BigDecimal.valueOf(150.0));
        when(finnhubService.getCompanyHistory("AAPL")).thenReturn(Map.of("sma", 120.0));

        List<DashboardDTO> dashboard = service.showPortfolio();

        assertEquals(1, dashboard.size());
        DashboardDTO dto = dashboard.get(0);
        assertEquals("AAPL", dto.getTicker());
        assertEquals(0, dto.getProfitLoss().compareTo(BigDecimal.valueOf(100.0))); // (150-100)*2 =100
        assertEquals(150.0, dto.getCurrentPrice().doubleValue());
        assertEquals("UP", dto.getTrend());
    }

    @Test
    public void getSectorDisribution_calculatesTotals() {
        Investment a = new Investment(1L, "AAPL", "Tech", 2, BigDecimal.valueOf(100.0), LocalDateTime.now());
        Investment b = new Investment(2L, "JPM", "Finance", 3, BigDecimal.valueOf(50.0), LocalDateTime.now());
        when(repository.findAll()).thenReturn(List.of(a, b));
        when(finnhubService.getStockPrice("AAPL")).thenReturn(BigDecimal.valueOf(150.0));
        when(finnhubService.getStockPrice("JPM")).thenReturn(BigDecimal.valueOf(60.0));

        Map<String, Double> dist = service.getSectorDisribution();

        assertEquals(2, dist.size());
        assertEquals(300.0, dist.get("Tech")); //150*2
        assertEquals(180.0, dist.get("Finance")); //60*3
    }

    @Test
    public void getTotalPortfolioValue_sumsValues() {
        Investment a = new Investment(1L, "AAPL", "Tech", 2, BigDecimal.valueOf(100.0), LocalDateTime.now());
        Investment b = new Investment(2L, "JPM", "Finance", 3, BigDecimal.valueOf(50.0), LocalDateTime.now());
        when(repository.findAll()).thenReturn(List.of(a, b));
        when(finnhubService.getStockPrice("AAPL")).thenReturn(BigDecimal.valueOf(150.0));
        when(finnhubService.getStockPrice("JPM")).thenReturn(BigDecimal.valueOf(60.0));

        Double total = service.getTotalPortfolioValue();

        assertEquals(480.0, total); // (150*2)+(60*3)=300+180
    }

    @Test
    public void getPortfolioHIstory_aggregatesByDate() {
        Investment a = new Investment(1L, "AAPL", "Tech", 2, BigDecimal.valueOf(100.0), LocalDateTime.now());
        when(repository.findAll()).thenReturn(List.of(a));

        List<Map<String, Object>> prices = List.of(
                Map.of("date", "2023-01-01T00:00:00Z", "close", 10.0),
                Map.of("date", "2023-01-02T00:00:00Z", "close", 20.0)
        );
        when(finnhubService.getCompanyHistory("AAPL")).thenReturn(Map.of("prices", prices));

        Map<String, Double> history = service.getPortfolioHIstory();

        assertEquals(2, history.size());
        assertEquals(20.0, history.get("2023-01-01T00:00:00Z")); //10*2
        assertEquals(40.0, history.get("2023-01-02T00:00:00Z")); //20*2
    }

    @Test
    public void getAdvisorSUggestions_returnsEmptyPortfolioMessage_whenNoAssets() {
        when(repository.findAll()).thenReturn(List.of());

        List<String> suggestions = service.getAdvisorSUggestions();

        assertEquals(1, suggestions.size());
        assertTrue(suggestions.get(0).toLowerCase().contains("portfolio is empty"));
    }

    @Test
    public void parseCsv_parsesRows_correctly() throws Exception {
        String csv = "Ticker,Price,Quantity,Sector\nAAPL,150.5,2,Tech\nJPM,60,3,Finance\n";
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csv.getBytes()));

        List<Investment> preview = service.parseCsv(file);

        assertEquals(2, preview.size());
        assertEquals("AAPL", preview.get(0).getTicker());
        assertEquals(2, preview.get(0).getQuantity());
    }

    @Test
    public void getPortfolioSummary_calculatesTopAndWorst() {
        Investment a = new Investment(1L, "AAPL", "Tech", 2, BigDecimal.valueOf(100.0), LocalDateTime.now());
        Investment b = new Investment(2L, "JPM", "Finance", 3, BigDecimal.valueOf(50.0), LocalDateTime.now());
        when(repository.findAll()).thenReturn(List.of(a, b));
        when(finnhubService.getStockPrice("AAPL")).thenReturn(BigDecimal.valueOf(150.0));
        when(finnhubService.getStockPrice("JPM")).thenReturn(BigDecimal.valueOf(60.0));

        PortfolioSummary s = service.getPortfolioSummary();

        assertEquals(2, s.getTotalAssets());
        assertEquals(480.0, s.getTotalValue());
        assertEquals("AAPL", s.getTopHolding());
        assertEquals("JPM", s.getWorstHolding());
    }

    @Test
    public void save_delete_saveAll_delegateToRepository() {
        Investment inv = new Investment(null, "AAPL", "Tech", 1, BigDecimal.valueOf(100.0), LocalDateTime.now());
        when(repository.save(inv)).thenReturn(inv);
        when(repository.saveAll(any())).thenReturn(List.of(inv));

        Investment out = service.save(inv);
        assertEquals(inv, out);
        verify(repository).save(inv);

        service.deleteById(5L);
        verify(repository).deleteById(5L);

        List<Investment> saved = service.saveAll(List.of(inv));
        assertEquals(1, saved.size());
        verify(repository).saveAll(any());
    }
}
 
