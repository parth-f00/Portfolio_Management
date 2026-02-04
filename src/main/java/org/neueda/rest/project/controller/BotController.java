
package org.neueda.rest.project.controller;

import org.neueda.rest.project.bot.BotRequest;
import org.neueda.rest.project.bot.BotResponse;
import org.neueda.rest.project.service.BotService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot")
@CrossOrigin
public class BotController {

    //Service layer handling bot logic and portfolio analysis
    private final BotService botService;

    //Constructor-based dependency injection
    public BotController(BotService botService) {
        this.botService = botService;
    }

    //Endpoint to handle natural language portfolio queries
    @PostMapping("/query")
    public BotResponse handleQuery(@RequestBody BotRequest request) {
        return botService.processQuery(request.getQuery());
    }
}
