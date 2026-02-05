package org.neueda.rest.project.ControllerTest;

import org.junit.jupiter.api.Test;
import org.neueda.rest.project.bot.BotRequest;
import org.neueda.rest.project.bot.BotResponse;
import org.neueda.rest.project.controller.BotController;
import org.neueda.rest.project.service.BotService;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BotControllerTest {

    @Test
    public void handleQuery_returnsBotResponse_directCall() {
        BotService botService = mock(BotService.class);
        BotController controller = new BotController(botService);

        BotResponse resp = new BotResponse("Here is your portfolio summary", "portfolio_summary", Arrays.asList("detail","help"));
        when(botService.processQuery(eq("Tell me about my portfolio"))).thenReturn(resp);

        BotRequest req = new BotRequest();
        req.setQuery("Tell me about my portfolio");

        BotResponse result = controller.handleQuery(req);

        assertEquals("Here is your portfolio summary", result.getAnswer());
        assertEquals("portfolio_summary", result.getIntent());
        assertEquals("detail", result.getSuggestions().get(0));

        verify(botService, times(1)).processQuery(eq("Tell me about my portfolio"));
    }

    @Test
    public void handleQuery_whenServiceThrows_propagatesException() {
        BotService botService = mock(BotService.class);
        BotController controller = new BotController(botService);

        when(botService.processQuery(eq("fail case"))).thenThrow(new RuntimeException("processing failed"));

        BotRequest req = new BotRequest();
        req.setQuery("fail case");

        assertThrows(RuntimeException.class, () -> controller.handleQuery(req));

        verify(botService, times(1)).processQuery(eq("fail case"));
    }
}
