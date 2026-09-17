package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.service.GeminiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeminiTestController {

    private final GeminiService geminiService;

    public GeminiTestController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/api/gemini/test")
    public String testGemini(
            @RequestParam(defaultValue = "سلام، خودت را کوتاه معرفی کن") String q) {

        return geminiService.ask(q);
    }
}