package com.dhanush.chat.dhanush_chat_bot.service;

import com.dhanush.chat.dhanush_chat_bot.response.ConversationHistory;
import com.dhanush.chat.dhanush_chat_bot.response.ConversationTitle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    //To change to persistent memory repository need to add the dependency and implement it here
    // (currently in memory)
    private final ChatMemoryRepository chatMemoryRepository;

    private Map<String, String> conversationNames = new TreeMap<>();

    @Value("classpath:prompts/rag-system-message.txt")
    private Resource ragSystemMessageResource;

    @Value("classpath:prompts/conversation-title-system-message.txt")
    private Resource conversationNameSystemMessageResource;

    public String startConversation() {
        String conversationId = UUID.randomUUID().toString();
        chatMemoryRepository.saveAll(conversationId,new ArrayList<>());
        return conversationId;
    }

    public Flux<String> chat(String conversationId, String query) {
        log.info("Received chat query: {}", query);

        // 1. Define the metadata filter expression using FilterExpressionBuilder (a fluent API)
//        Filter.Expression filterExpression = new FilterExpressionBuilder()
//                .eq("department", "legal") // Equivalent to "department == 'legal'" in String format
//                .and()
//                .gt("year", 2020)          // Equivalent to "year > 2020"
//                .build();


        RetrievalAugmentationAdvisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .similarityThreshold(0.50)
//                        .filterExpression() //Add filter expression on metadata if needed
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .build())
                .build();

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();

        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build();

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(messageChatMemoryAdvisor, ragAdvisor)
                .defaultSystem(ragSystemMessageResource)
                .build();
       populateConversationName(conversationId, query);
        return chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(query).stream().content();
    }

    public List<ConversationHistory> getConversationHistory(String conversationId) {
        List<Message> byConversationId = chatMemoryRepository.findByConversationId(conversationId);
        return byConversationId.stream().map(message -> new ConversationHistory(message.getText(),
                message.getMessageType().getValue())).collect(Collectors.toList());
    }

    public void populateConversationName(String conversationId, String query) {
        if (conversationNames.get(conversationId) != null) {
            return;
        }
        ChatClient chatClient = ChatClient.builder(chatModel)
                .build();
//        String prompt =
//                messages.stream()
//                        .map(msg -> msg.getMessageType().getValue() + ": " + msg.getText())
//                        .collect(Collectors.joining("\n"));
        conversationNames.put(conversationId, chatClient.prompt()
                .system(conversationNameSystemMessageResource)
                .user(query)
                .call().content());
    }

    public List<ConversationTitle> getConversationIdsWithTitle() {
        //TODO : to address multiple users, need to get the list of conversation ID from a persistent storage
        return conversationNames.entrySet().stream()
                .map(entry -> new ConversationTitle(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public void deleteConversation(String conversationId) {
        chatMemoryRepository.deleteByConversationId(conversationId);
        conversationNames.remove(conversationId);
        log.info("Deleted conversation with id: {}", conversationId);
    }
}
