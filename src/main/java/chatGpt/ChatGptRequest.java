package chatGpt;

public record ChatGptRequest(String model, String role, String prompt) {

    @Override
    public String toString() {
        return """
           {"model": "%s", "messages": [{"role": "%s", "content":
           "%s"}]}""".formatted(model, role, prompt);
    }
}
