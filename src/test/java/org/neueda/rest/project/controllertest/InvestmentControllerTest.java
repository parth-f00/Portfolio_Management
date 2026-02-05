package org.neueda.rest.project.controllertest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neueda.rest.project.controller.InvestmentController;
import org.neueda.rest.project.dto.DashboardDTO;
import org.neueda.rest.project.dto.ImpactAnalysis;
import org.neueda.rest.project.entity.Investment;
import org.neueda.rest.project.service.FinnhubService;
import org.neueda.rest.project.service.InvestmentService;
import org.neueda.rest.project.service.Simulator;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class InvestmentControllerTest {

    private InvestmentService investmentService;
    private FinnhubService finnhubService;
    private Simulator simulatorService;
    private InvestmentController controller;

    @BeforeEach
    public void setUp() throws Exception {
        investmentService = mock(InvestmentService.class);
        finnhubService = mock(FinnhubService.class);
        simulatorService = mock(Simulator.class);
        controller = new InvestmentController();
        
        // Inject mocks into controller using reflection (since services are @Autowired)
        Field investmentServiceField = InvestmentController.class.getDeclaredField("investmentService");
        investmentServiceField.setAccessible(true);
        investmentServiceField.set(controller, investmentService);
        
        Field finnhubServiceField = InvestmentController.class.getDeclaredField("finnhubService");
        finnhubServiceField.setAccessible(true);
        finnhubServiceField.set(controller, finnhubService);
        
        Field simulatorServiceField = InvestmentController.class.getDeclaredField("simulatorService");
        simulatorServiceField.setAccessible(true);
        simulatorServiceField.set(controller, simulatorService);
    }

    @Test
    public void getPortfolio_returnsListOfDashboardDTO() {
        List<DashboardDTO> mockPortfolio = new ArrayList<>();
        DashboardDTO dto1 = new DashboardDTO();
        dto1.setTicker("AAPL");
        dto1.setSector("Tech");
        dto1.setQuantity(100);
        mockPortfolio.add(dto1);
        
        when(investmentService.showPortfolio()).thenReturn(mockPortfolio);

        List<DashboardDTO> result = controller.getPortfolio();

        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getTicker());
        verify(investmentService, times(1)).showPortfolio();
    }

    @Test
    public void getPortfolio_returnsEmptyList() {
        when(investmentService.showPortfolio()).thenReturn(new ArrayList<>());

        List<DashboardDTO> result = controller.getPortfolio();

        assertTrue(result.isEmpty());
        verify(investmentService, times(1)).showPortfolio();
    }

    @Test
    public void addInvestment_savesAndReturnsInvestment() {
        Investment investment = new Investment(1L, "GOOGL", "Tech", 10, BigDecimal.valueOf(2800.0), LocalDateTime.now());
        when(investmentService.save(any(Investment.class))).thenReturn(investment);

        Investment result = controller.addInvestment(investment);

        assertNotNull(result);
        assertEquals("GOOGL", result.getTicker());
        assertEquals(10, result.getQuantity());
        verify(investmentService, times(1)).save(investment);
    }

    @Test
    public void deleteInvestment_callsServiceWithCorrectId() {
        Long investmentId = 5L;

        controller.deleteInvestment(investmentId);

        verify(investmentService, times(1)).deleteById(investmentId);
    }

    @Test
    public void getTotalValue_returnsPortfolioTotal() {
        Double totalValue = 500000.0;
        when(investmentService.getTotalPortfolioValue()).thenReturn(totalValue);

        Double result = controller.getTotalValue();

        assertEquals(500000.0, result);
        verify(investmentService, times(1)).getTotalPortfolioValue();
    }

    @Test
    public void getTotalValue_returnsZeroForEmptyPortfolio() {
        when(investmentService.getTotalPortfolioValue()).thenReturn(0.0);

        Double result = controller.getTotalValue();

        assertEquals(0.0, result);
        verify(investmentService, times(1)).getTotalPortfolioValue();
    }

    @Test
    public void getSectorDistribution_returnsMapOfSectorWeights() {
        Map<String, Double> sectorMap = new HashMap<>();
        sectorMap.put("Tech", 45.0);
        sectorMap.put("Finance", 30.0);
        sectorMap.put("Healthcare", 25.0);
        when(investmentService.getSectorDisribution()).thenReturn(sectorMap);

        Map<String, Double> result = controller.getSectorDistribution();

        assertEquals(3, result.size());
        assertEquals(45.0, result.get("Tech"));
        verify(investmentService, times(1)).getSectorDisribution();
    }

    @Test
    public void getPortfolioHistory_returnsHistoricalData() {
        Map<String, Double> history = new HashMap<>();
        history.put("2024-01-01", 100000.0);
        history.put("2024-02-01", 105000.0);
        when(investmentService.getPortfolioHIstory()).thenReturn(history);

        Map<String, Double> result = controller.getPortfolioHistory();

        assertEquals(2, result.size());
        assertTrue(result.containsKey("2024-01-01"));
        verify(investmentService, times(1)).getPortfolioHIstory();
    }

    @Test
    public void getAdvisorSuggestions_returnsListOfRecommendations() {
        List<String> suggestions = Arrays.asList("Diversify tech holdings", "Consider healthcare sector", "Reduce overlap in holdings");
        when(investmentService.getAdvisorSUggestions()).thenReturn(suggestions);

        List<String> result = controller.getAdvisorSuggestions();

        assertEquals(3, result.size());
        assertEquals("Diversify tech holdings", result.get(0));
        verify(investmentService, times(1)).getAdvisorSUggestions();
    }

    @Test
    public void previewCSV_parsesCsvAndReturnsInvestmentList() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "portfolio.csv",
                "text/csv",
                "AAPL,Tech,100,150.0".getBytes()
        );
        List<Investment> parsedInvestments = Arrays.asList(
                new Investment(1L, "AAPL", "Tech", 100, BigDecimal.valueOf(150.0), LocalDateTime.now())
        );
        when(investmentService.parseCsv(any(MultipartFile.class))).thenReturn(parsedInvestments);

        List<Investment> result = controller.previewCSV(file);

        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getTicker());
        verify(investmentService, times(1)).parseCsv(file);
    }

    @Test
    public void previewCSV_returnsEmptyListForInvalidFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);
        when(investmentService.parseCsv(any(MultipartFile.class))).thenReturn(new ArrayList<>());

        List<Investment> result = controller.previewCSV(file);

        assertTrue(result.isEmpty());
        verify(investmentService, times(1)).parseCsv(file);
    }

    @Test
    public void saveBatch_savesMultipleInvestments() {
        List<Investment> investments = Arrays.asList(
                new Investment(1L, "AAPL", "Tech", 100, BigDecimal.valueOf(150.0), LocalDateTime.now()),
                new Investment(2L, "JPM", "Finance", 50, BigDecimal.valueOf(150.0), LocalDateTime.now())
        );
        when(investmentService.saveAll(anyList())).thenReturn(investments);

        List<Investment> result = controller.saveBatch(investments);

        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getTicker());
        assertEquals("JPM", result.get(1).getTicker());
        verify(investmentService, times(1)).saveAll(investments);
    }

    @Test
    public void saveBatch_returnsEmptyListForEmptyInput() {
        List<Investment> emptyList = new ArrayList<>();
        when(investmentService.saveAll(anyList())).thenReturn(new ArrayList<>());

        List<Investment> result = controller.saveBatch(emptyList);

        assertTrue(result.isEmpty());
        verify(investmentService, times(1)).saveAll(emptyList);
    }

    @Test
    public void simulateImpact_returnsImpactAnalysisResult() {
        Investment investment = new Investment(1L, "AAPL", "Tech", 10, BigDecimal.valueOf(150.0), LocalDateTime.now());
        Map<String, Double> oldSector = new HashMap<>();
        oldSector.put("Tech", 50.0);
        Map<String, Double> newSector = new HashMap<>();
        newSector.put("Tech", 55.0);
        ImpactAnalysis expectedImpact = new ImpactAnalysis(oldSector, newSector);
        when(simulatorService.SimulateTrade(any(Investment.class))).thenReturn(expectedImpact);

        ImpactAnalysis result = controller.simulateImpact(investment);

        assertNotNull(result);
        verify(simulatorService, times(1)).SimulateTrade(investment);
    }

    @Test
    public void simulateImpact_handlesNegativeImpact() {
        Investment investment = new Investment(1L, "RISKY", "Tech", 100, BigDecimal.valueOf(50.0), LocalDateTime.now());
        Map<String, Double> oldSector = new HashMap<>();
        oldSector.put("Tech", 40.0);
        Map<String, Double> newSector = new HashMap<>();
        newSector.put("Tech", 45.0);
        ImpactAnalysis negativeImpact = new ImpactAnalysis(oldSector, newSector);
        negativeImpact.setRiskLevel("HIGH");
        when(simulatorService.SimulateTrade(any(Investment.class))).thenReturn(negativeImpact);

        ImpactAnalysis result = controller.simulateImpact(investment);

        assertNotNull(result);
        verify(simulatorService, times(1)).SimulateTrade(investment);
    }
}
