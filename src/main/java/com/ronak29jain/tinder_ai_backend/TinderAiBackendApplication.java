package com.ronak29jain.tinder_ai_backend;

import com.ronak29jain.tinder_ai_backend.conversations.ChatMessage;
import com.ronak29jain.tinder_ai_backend.conversations.Conversation;
import com.ronak29jain.tinder_ai_backend.conversations.ConversationRepository;
import com.ronak29jain.tinder_ai_backend.profiles.Gender;
import com.ronak29jain.tinder_ai_backend.profiles.Profile;
import com.ronak29jain.tinder_ai_backend.profiles.ProfileRepository;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
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

    @Autowired
    private OllamaChatModel ollamaChatModel;

    public static void main(String[] args) {
        SpringApplication.run(TinderAiBackendApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("My app is running");

        Prompt prompt = new Prompt("what is java? tell me in 50 words");
        ChatResponse chatResponse = ollamaChatModel.call(prompt);
        System.out.println(chatResponse.getResult().getOutput().getContent());

        profileRepository.deleteAll();
        conversationRepository.deleteAll();

        Profile profile1 = new Profile(
                "1",
                "Ankit",
                "Sharma",
                30,
                Gender.MALE,
                "Asian",
                "I'm looking for beautiful girl",
                "ankit.jpg",
                "INTP"
        );
        Profile profile2 = new Profile(
                "2",
                "Ankita",
                "Bansal",
                28,
                Gender.FEMALE,
                "Asian",
                "I'm looking for a handsome boy who loves chai",
                "ankita.jpg",
                "INTP"
        );
        profileRepository.save(profile1);
        profileRepository.save(profile2);
        profileRepository.findAll().forEach(System.out::println);

        Conversation conversation = new Conversation(
                "26",
                List.of(profile1.id(), profile2.id()),
                List.of(new ChatMessage("hey there!", profile1.id(), LocalDateTime.now()))
        );

        conversationRepository.save(conversation);
        conversationRepository.findAll().forEach(System.out::println);
    }
}
