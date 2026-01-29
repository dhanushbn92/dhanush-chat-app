package com.dhanush.chat.dhanush_chat_bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final VectorStore vectorStore;
    private final ChatModel chatModel;

    @Value("classpath:prompts/rag-system-message.txt")
    private Resource ragSystemMessageResource;

    public Flux<String> getChatResponse(String query) {
        log.info("Received chat query: {}", query);

        // 1. Define the metadata filter expression using FilterExpressionBuilder (a fluent API)
//        Filter.Expression filterExpression = new FilterExpressionBuilder()
//                .eq("department", "legal") // Equivalent to "department == 'legal'" in String format
//                .and()
//                .gt("year", 2020)          // Equivalent to "year > 2020"
//                .build();


        RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .similarityThreshold(0.50)
//                        .filterExpression() //Add filter expression on metadata if needed
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .build())
                .build();

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(advisor)
                .defaultSystem(ragSystemMessageResource)
                .build();
        return chatClient.prompt().user(query).stream().content();
    }
}
