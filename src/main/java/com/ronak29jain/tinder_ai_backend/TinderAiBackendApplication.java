package com.ronak29jain.tinder_ai_backend;

import com.ronak29jain.tinder_ai_backend.profiles.Gender;
import com.ronak29jain.tinder_ai_backend.profiles.Profile;
import com.ronak29jain.tinder_ai_backend.profiles.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TinderAiBackendApplication implements CommandLineRunner {

	@Autowired
	private ProfileRepository profileRepository;

	public static void main(String[] args) {
		SpringApplication.run(TinderAiBackendApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("My app is running");
		Profile profile = new Profile(
				"123",
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
	}
}
