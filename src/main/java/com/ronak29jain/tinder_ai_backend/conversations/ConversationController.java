package com.ronak29jain.tinder_ai_backend.conversations;

import com.ronak29jain.tinder_ai_backend.profiles.ProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@RestController
public class ConversationController {

    private final ConversationRepository conversationRepository;
    private final ProfileRepository profileRepository;

    public ConversationController(ConversationRepository conversationRepository, ProfileRepository profileRepository) {
        this.conversationRepository = conversationRepository;
        this.profileRepository = profileRepository;
    }

    @PostMapping("/conversations")
    public Conversation createConversation(@RequestBody ConversationRequestModel conversationRequestModel) {
        profileRepository.findById(conversationRequestModel.profileId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "unable to find profile with id " + conversationRequestModel.profileId()));

        conversationRepository.findByProfileId(conversationRequestModel.profileId()).ifPresent(i -> {
            throw new IllegalArgumentException("conversation already exists");
        });
        Conversation conversation = new Conversation(
                UUID.randomUUID().toString(),
                conversationRequestModel.profileId(),
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

//        if (!conversation.profileId().equals(conversationMessageRequestModel.recipientId())) {
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
}
