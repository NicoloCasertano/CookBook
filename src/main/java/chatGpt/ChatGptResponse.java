package chatGpt;

public class ChatGptResponse {
    public String id;
    public ChatGptChoice[] choices;
    public String object;
    public long created;
    public String model;
    public ChatGptUsage usage;
    public String systemFingerPrint;
    @Override
    public String toString() {
        Object sb = new StringBuilder();
        for(ChatGptChoice chs: choices) {
            ((StringBuilder) sb).append(chs).append('\n');
        }
        return sb.toString(); //metodo toString di Object non di questa classe
    }
}
