package org.neueda.rest.project.servicetest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neueda.rest.project.bot.BotResponse;
import org.neueda.rest.project.dto.PortfolioSummary;
import org.neueda.rest.project.service.AIService;
import org.neueda.rest.project.service.BotService;
import org.neueda.rest.project.service.InvestmentService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BotServiceTest {

    private InvestmentService investmentService;
    private AIService aiService;
    private BotService botService;

    @BeforeEach
    public void setUp() {
        investmentService = mock(InvestmentService.class);
        aiService = mock(AIService.class);
        botService = new BotService(investmentService, aiService);

        // by default, have AI service return the same text passed in
        when(aiService.explain(anyString())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    public void processQuery_summary_returnsSummaryResponse() {
        when(investmentService.getPortfolioSummary())
                .thenReturn(new PortfolioSummary(1000.0, 3, "AAPL", "TSLA"));

        BotResponse resp = botService.processQuery("please summarize my portfolio");

        assertNotNull(resp);
        assertEquals("SUMMARY", resp.getIntent());
        assertTrue(resp.getAnswer().contains("Your portfolio contains"));
        assertTrue(resp.getSuggestions().contains("Check risk"));
        verify(aiService).explain(anyString());
    }

    @Test
    public void processQuery_value_returnsValueResponse() {
        when(investmentService.getTotalPortfolioValue()).thenReturn(1234.56);

        BotResponse resp = botService.processQuery("what is my portfolio value?");

        assertNotNull(resp);
        assertEquals("VALUE", resp.getIntent());
        assertTrue(resp.getAnswer().contains("Your total portfolio value is"));
        verify(aiService).explain(anyString());
    }

    @Test
    public void processQuery_unknown_returnsHelpSuggestion() {
        BotResponse resp = botService.processQuery("gibberish query");

        assertNotNull(resp);
        assertEquals("UNKNOWN", resp.getIntent());
        assertTrue(resp.getAnswer().toLowerCase().contains("didn’t understand") || resp.getAnswer().toLowerCase().contains("didn't understand"));
        assertTrue(resp.getSuggestions().contains("Help"));
        verify(aiService).explain(anyString());
    }

    @Test
    public void processQuery_implicitReference_resolvesToLast() {
        when(investmentService.getPortfolioSummary())
                .thenReturn(new PortfolioSummary(500.0, 2, "MSFT", "IBM"));

        BotResponse first = botService.processQuery("summary");
        assertEquals("SUMMARY", first.getIntent());

        BotResponse second = botService.processQuery("this");
        assertEquals("SUMMARY", second.getIntent());
    }

    @Test
    public void processQuery_followUp_resolvesBasedOnPrevious() {
        when(investmentService.getPortfolioSummary())
                .thenReturn(new PortfolioSummary(500.0, 2, "MSFT", "IBM"));
        when(investmentService.getSectorDisribution()).thenReturn(Map.of("Tech", 60.0, "Finance", 40.0));

        BotResponse first = botService.processQuery("summary");
        assertEquals("SUMMARY", first.getIntent());

        BotResponse follow = botService.processQuery("and risk");
        assertEquals("RISK_LEVEL", follow.getIntent());
    }

    @Test
    public void processQuery_bestWorst_withEmptyPortfolio_returnsMessage() {
        when(investmentService.showPortfolio()).thenReturn(List.of());

        BotResponse resp = botService.processQuery("who is the best performer");

        assertNotNull(resp);
        assertEquals("BEST_WORST_PERFORMER", resp.getIntent());
        assertTrue(resp.getAnswer().toLowerCase().contains("portfolio is empty"));
    }
}
 
