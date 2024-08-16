package com.ronak29jain.tinder_ai_backend.conversations;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, String> {
    Optional<Conversation> findByProfileIds(List<String> profileIds);

    List<Conversation> findAllByProfileIds(String profileId);
}
