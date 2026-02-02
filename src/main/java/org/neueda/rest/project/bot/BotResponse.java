
package org.neueda.rest.project.bot;

public class BotResponse {

    //Bot generated response for the user query
    private String answer;

    public BotResponse(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
