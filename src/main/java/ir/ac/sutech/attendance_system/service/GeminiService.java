package ir.ac.sutech.attendance_system.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private final Client client;

    public GeminiService() {

        String apiKey = System.getenv("GOOGLE_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            this.client = null;
        } else {
            this.client = new Client();
        }
    }

    public boolean isAvailable() {
        return client != null;
    }

    public String ask(String prompt) {

        if (!isAvailable()) {
            throw new IllegalStateException(
                    "سرویس هوش مصنوعی پیکربندی نشده است."
            );
        }

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3.6-flash",
                        prompt,
                        null
                );

        String text = response.text();

        if (text == null || text.isBlank()) {
            return "پاسخی از سرویس هوش مصنوعی دریافت نشد.";
        }

        return text.trim();
    }
}