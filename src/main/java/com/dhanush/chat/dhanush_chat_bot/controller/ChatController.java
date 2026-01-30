package com.dhanush.chat.dhanush_chat_bot.controller;


import com.dhanush.chat.dhanush_chat_bot.response.ConversationHistory;
import com.dhanush.chat.dhanush_chat_bot.response.ConversationTitle;
import com.dhanush.chat.dhanush_chat_bot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/new")
    public Map<String, String> createConversation() {
        return Map.of("conversationId", chatService.startConversation());
    }

    @PostMapping
    public Flux<String> chat(@RequestParam String conversationId,
                             @RequestParam String userQuery) {
        if(StringUtils.isEmpty(conversationId)) {
            conversationId = chatService.startConversation();
        }
        return chatService.chat(conversationId, userQuery);
    }

    @GetMapping("/history/{conversationId}")
    public List<ConversationHistory> getConversationHistory(@PathVariable String conversationId) {
        return chatService.getConversationHistory(conversationId);
    }

    @GetMapping
    public List<ConversationTitle> getConversationIdsWithTitles() {
        return chatService.getConversationIdsWithTitle();
    }

    @DeleteMapping
    public String deleteConversation(@RequestParam String conversationId) {
        chatService.deleteConversation(conversationId);
        return "Conversation deleted successfully!";
    }
}