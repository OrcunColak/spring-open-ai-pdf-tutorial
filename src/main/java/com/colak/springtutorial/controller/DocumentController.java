package com.colak.springtutorial.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("ai/document")

@RequiredArgsConstructor
public class DocumentController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;


    @GetMapping("/chat")
    public ResponseEntity<String> generateResponse(@RequestParam String query) {
        List<Document> similarDocuments = vectorStore.similaritySearch(query);
        String information = similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining(System.lineSeparator()));

        String response = "Please answer 'Not enough knowledge.' if you don't know.\n\n" + information;

        return ResponseEntity.ok(getAIResponse(query, response));
    }

    // OpenAI Chat Mode: The application backend leverages the OpenAI Chat Completion API
    private String getAIResponse(String query, String response) {
        SystemPromptTemplate systemPrompt = new SystemPromptTemplate(response);
        PromptTemplate userPrompt = new PromptTemplate(query);

        Prompt prompt = new Prompt(List.of(systemPrompt.createMessage(), userPrompt.createMessage()));

        ChatResponse chatResponse = chatClient.call(prompt);
        Generation chatResponseResult = chatResponse.getResult();
        AssistantMessage assistantMessage = chatResponseResult.getOutput();
        // Content is raw Json
        return assistantMessage.getContent();
    }
}
