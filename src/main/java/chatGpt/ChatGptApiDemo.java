package chatGpt;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ChatGptApiDemo implements Constants {
    //blocco statico
    final static String URL = "https://api.openai.com/v1/chat/completions";
    final static boolean DUMP_RAW = false; //se true stampa la risposta con il parsing

    //main che farà partire il prompt
    public static void main(String[] args) throws Exception {
        System.out.println(chatGPT(TEXT_PROMPT));
    }
    //metodo che torna una stringa che sarà la risposta di ChatGPT
    public static String chatGPT(String prompt) throws IOException, URISyntaxException {
        String apiKey = Constants.getOpenAPIKey();
        String model = "gpt-4o";
        HttpClient client = HttpClient.newHttpClient(); //creo un client per fare le operazioni http

        URI uri = new URI(URL); //uniform resourse identifier: rappresenta una sequenza di caratteri che
                                //identificano una risorsa in modo univoco

        // Send the request
        ChatGptRequest request = new ChatGptRequest(model, "user", prompt); //role è il ruolo della richiesta
                                                                                //se uso "user" sarà l'input che io darò a Chat da leggere
                                                                                //se uso "system", il context sarà il ruolo di chat gpt mentre legge il testo della richiesta
        String json = request.toString(); //qui invio la richiesta
        System.out.println("Sending this: " + json);
        HttpRequest webRequest = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey) //bearer sta per bearer token, che si usa per l'autenticazione
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build(); //ricordiamoci che "json" non è il formato ma la richiesta effettiva, ho copiato il nome da cookbook

        //settiamo la parte che riceve
        HttpResponse<String> response = null; //perchè String? perchè voglio stamparla e vederla
        try {
            response = client.send(webRequest,
                    HttpResponse.BodyHandlers.ofString()); //la body handlers è un interfaccia che decide come gestire il corpo della richiesta HTTP
            System.out.println("Response code: " + response.statusCode());
            System.out.println("Response body: " + response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        // Get a response from ChatGPT
        int n = response.statusCode();
        switch (n) {
            case 429:
                System.out.println("ChatGPT vuole i soooordi per rispondere");
                List<String> retryAfter = response.headers().map().get("retry-after");  //va a prendersi le intestazioni
                                                                                        // HTTP e se trova "retry after" guarda
                                                                                        // cosa c'è dopo e si
                                                                                        // ferma N secondi
                if (retryAfter != null && !retryAfter.isEmpty()) {
                    String rah = retryAfter.get(0);
                    System.out.println("Try again in: " + rah);
                }
//                System.out.println("Response body: " + response.body());
                return null;
            case 500:
            case 501:
            case 502:
                System.out.println("GPT Server error " + n + " " + response.body());
                return null;
            default:
                System.out.println("HTTP Status was " + n);
        }
        if (DUMP_RAW) {
            System.out.println("Response body: " + response.body());
            return "Answer dumped, no JSON parsing done.";
        } else {
            ObjectMapper mapper = new ObjectMapper();
            //Ci riguardiamo bene dai nuovi campi aggiunti alle nuove versioni di ChatGPT
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ChatGptResponse resp = mapper.readValue(response.body(), ChatGptResponse.class);
            System.out.println(resp);
            return resp.choices[0].message.content;
        }
    }
}

/*
status code OpenAi API Error
https://platform.openai.com/docs/guides/error-codes/api-errors

"system":
This role provides instructions or context for the model's behavior, acting as a guide for how the model should respond.

"developer":
This role is used to provide instructions or context specific to developers, often related to tools or integrations.

"user":
This role represents the user's input, including questions, commands, or requests.

"assistant":
This role represents the model's responses to the user's input.
*/