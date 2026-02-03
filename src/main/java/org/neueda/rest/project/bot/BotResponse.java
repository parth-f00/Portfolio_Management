package org.neueda.rest.project.bot;

import java.util.List;

public class BotResponse {

    private String answer;
    private String intent;
    private List<String> suggestions;

    public BotResponse(String answer) {
        this.answer = answer;
    }

    public BotResponse(String answer, String intent, List<String> suggestions) {
        this.answer = answer;
        this.intent = intent;
        this.suggestions = suggestions;
    }

    public String getAnswer() {
        return answer;
    }

    public String getIntent() {
        return intent;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }
}
