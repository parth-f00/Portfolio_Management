
package org.neueda.rest.project.bot;

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
