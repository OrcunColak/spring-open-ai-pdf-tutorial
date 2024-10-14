package com.colak.springtutorial.controller;

import com.colak.springtutorial.component.LLMService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ai/document")

@RequiredArgsConstructor
public class DocumentController {

    private final LLMService llmService;

    @GetMapping("/chat")
    public ResponseEntity<String> generateResponse(@RequestParam String query) {
        String aiResponse = llmService.generateResponse(query, 3);
        return ResponseEntity.ok(aiResponse);
    }


}
