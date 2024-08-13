package com.ronak29jain.tinder_ai_backend;

import com.ronak29jain.tinder_ai_backend.conversations.ChatMessage;
import com.ronak29jain.tinder_ai_backend.conversations.Conversation;
import com.ronak29jain.tinder_ai_backend.conversations.ConversationRepository;
import com.ronak29jain.tinder_ai_backend.profiles.Gender;
import com.ronak29jain.tinder_ai_backend.profiles.Profile;
import com.ronak29jain.tinder_ai_backend.profiles.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
public class TinderAiBackendApplication implements CommandLineRunner {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    public static void main(String[] args) {
        SpringApplication.run(TinderAiBackendApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("My app is running");
        Profile profile = new Profile(
                "124",
                "Ankit",
                "Sharma",
                30,
                Gender.MALE,
                "Asian",
                "I'm looking for a job in the tech industry.",
                "ankit.jpg",
                "INTP"
        );
        profileRepository.save(profile);
        profileRepository.findAll().forEach(System.out::println);

        Conversation conversation = new Conversation(
                "26",
                profile.id(),
                List.of(new ChatMessage("hey there!", profile.id(), LocalDateTime.now()))
        );

        conversationRepository.save(conversation);
        conversationRepository.findAll().forEach(System.out::println);
    }
}
