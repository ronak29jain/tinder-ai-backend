package com.ronak29jain.tinder_ai_backend.conversations;

import com.ronak29jain.tinder_ai_backend.profiles.ProfileRepository;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class ConversationController {

    private final ConversationRepository conversationRepository;
    private final ProfileRepository profileRepository;

    @Autowired
    private OllamaChatModel ollamaChatModel;

    public ConversationController(ConversationRepository conversationRepository, ProfileRepository profileRepository) {
        this.conversationRepository = conversationRepository;
        this.profileRepository = profileRepository;
    }

    @PostMapping("/conversations")
    public Conversation createConversation(@RequestBody ConversationRequestModel conversationRequestModel) {
        profileRepository.findById(conversationRequestModel.receiverId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "unable to find profile with id " + conversationRequestModel.receiverId()));

        List<String> profileIds = List.of(conversationRequestModel.receiverId(), conversationRequestModel.senderId());
        conversationRepository.findByProfileIds(profileIds).ifPresent(i -> {
            throw new IllegalArgumentException("conversation already exists");
        });

        Conversation conversation = new Conversation(
                UUID.randomUUID().toString(),
                profileIds,
                new ArrayList<>()
        );

        conversationRepository.save(conversation);
        return conversation;
    }

    @PostMapping("/conversations/{conversationId}")
    public Conversation sendMessage(@PathVariable String conversationId, @RequestBody ConversationMessageRequestModel conversationMessageRequestModel) {
        profileRepository.findById(conversationMessageRequestModel.senderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "unable to find profile with id " + conversationMessageRequestModel.senderId()));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "unable to find conversation with id " + conversationId));

//        if (!conversation.receiverId().equals(conversationMessageRequestModel.recipientId())) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "recipientId id does not match the conversation profile id");
//        }

        ChatMessage message = new ChatMessage(
                conversationMessageRequestModel.message(),
                conversationMessageRequestModel.senderId(),
                LocalDateTime.now()
        );

        conversation.message().add(message);
        conversationRepository.save(conversation);
        return conversation;
    }

    @GetMapping("/conversations/{conversationId}")
    public Conversation getConversation(@PathVariable String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "unable to find conversation with id " + conversationId));
    }

    @GetMapping("/conversations")
    public List<Conversation> getAllConversations(@RequestParam String profileId) {
        return conversationRepository.findAllByProfileIds(profileId);
    }

    @GetMapping("/ollamaChat")
    public String getOllamaChatResponse(@RequestParam String userInput) {
        Prompt prompt = new Prompt(userInput);
        ChatResponse chatResponse = ollamaChatModel.call(prompt);
        return chatResponse.getResult().getOutput().getContent();
    }
}
