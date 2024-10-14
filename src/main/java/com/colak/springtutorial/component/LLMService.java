package com.colak.springtutorial.component;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LLMService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    // Uses the vector store to search for relevant documents, embeds them in a system message, and generates a response based on the documents retrieved.
    public String generateResponse(@RequestParam String query, int topK) {
        SearchRequest searchRequest = SearchRequest.query(query).withTopK(topK);
        List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

        String systemMessageTemplate = """
                Answer the following question based only in the provided CONTEXT
                If the answer is not found respond : "I don't know".
                CONTEXT :
                    {CONTEXT}
                """;

        Message systemMessage = new SystemPromptTemplate(systemMessageTemplate)
                .createMessage(Map.of("CONTEXT", similarDocuments));
        UserMessage userMessage = new UserMessage(query);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        return getAIResponse(prompt);
    }

    // OpenAI Chat Mode: The application backend leverages the OpenAI Chat Completion API
    private String getAIResponse(Prompt prompt) {
        ChatResponse chatResponse = call(prompt);

        Generation chatResponseResult = chatResponse.getResult();
        AssistantMessage assistantMessage = chatResponseResult.getOutput();
        // Content is raw Json
        return assistantMessage.getContent();
    }

    private ChatResponse call(Prompt prompt) {
        return chatClient.prompt(prompt).call().chatResponse();
    }
}
