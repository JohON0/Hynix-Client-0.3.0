package io.hynix.managers.telegramsender;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TelegramWebHook {

    private static final String TELEGRAM_BOT_TOKEN = ""; // Замените на ваш токен
    private static final String CHAT_ID = ""; // Замените на ваш chat_id
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendMessage";




    public static void sendTelegramMessage(String message, String photoUrl) {
        try {
            String jsonPayload = String.format("{\"chat_id\": \"%s\", \"caption\": \"%s\", \"photo\": \"%s\"}", CHAT_ID, escapeJson(message), photoUrl);
            URL url = new URL("https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendPhoto");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Message and photo sent successfully");
            } else {
                System.out.println("Failed to send message and photo: " + conn.getResponseMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String escapeJson(String message) {
        return message.replace("\"", "\\\"").replace("\n", "\\n");
    }
}
