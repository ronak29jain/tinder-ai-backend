package com.ronak29jain.tinder_ai_backend.profiles;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

//import static java.lang.StringTemplate.STR;

@Service
public class ProfileGenerationService {

    private static final String STABLE_DIFFUSION_URL = "http://127.0.0.1:7860/sdapi/v1/txt2img";

    //    @Autowired
    private final HttpClient httpClient;

//    @Autowired
//    private HttpRequest httpRequest;

    private HttpRequest.Builder stableDiffusionRequestBuilder;

    //    @Autowired
    private OllamaChatModel ollamaChatModel;

//    @Autowired
//    private OpenAiChatModel openAiChatModel;

    private final List<Profile> generatedProfiles = new ArrayList<>();

    private static final String PROFILES_FILE_PATH = "profiles.json";

    @Value("${startup-actions.initialiseProfiles}")
    private Boolean initialiseProfiles;
    @Value("${tinderAi.LookingForGender}")
    private String lookingForGender;

    public ProfileGenerationService(OllamaChatModel ollamaChatModel) {
        this.ollamaChatModel = ollamaChatModel;
        this.httpClient = HttpClient.newHttpClient();
//        this.httpRequest = httpRequest;
        this.stableDiffusionRequestBuilder = HttpRequest.newBuilder()
                .setHeader("Content-type", "application/json")
                .uri(URI.create(STABLE_DIFFUSION_URL));
    }

//    public ProfileGenerationService() {
//    }


    public void generateProfiles(int numberOfProfiles) {

        if (!initialiseProfiles) {
            return;
        }

//      this method will not work with ollama, we need to use openai only. comment ollama code and uncomment openai code.
        List<Integer> ages = new ArrayList<>(List.of(20, 22, 25, 28, 30, 35, 37, 40, 45));
//        List<Integer> ages = new ArrayList<>(List.of(23, 24, 25, 23, 24, 25, 23, 24, 25, 23, 24, 25, 23, 24, 25, 23, 24, 25, 23, 24, 25));
//        List<Gender> genders = new ArrayList<>(List.of(Gender.MALE, Gender.FEMALE, Gender.OTHER));
//        List<String> ethnicities = new ArrayList<>(List.of("Asian", "Caucasian", "Indian", "Middle Eastern", "White", "Black", "Russian", "Italian", "African", "Native American"));
        List<String> ethnicities = new ArrayList<>(List.of("Indian"));

        Collections.shuffle(ages);
//        Collections.shuffle(genders);
        Collections.shuffle(ethnicities);

        String profileGender = lookingForGender;

        for (int age : ages) {
//            for (Gender gender: genders) {
//            profileGender = gender.toString();
            for (String ethnicity : ethnicities) {
                if (this.generatedProfiles.size() >= numberOfProfiles) {
//                      save the generated profiles into the json file
                    saveProfilesToJson(this.generatedProfiles);
                    return;
                }

                String promptString = "create a tinder profile persona of a " + age + " years old " + ethnicity + " " + profileGender +
                        " including first name, last name and Myers Briggs personality type and tinder bio. don't repeat the names from the previous generated result. please save the profile using the function saveProfile";

                System.out.println(promptString);
                ChatResponse response = ollamaChatModel.call(new Prompt(promptString));
//                    ChatResponse response = openAiChatModel.call(new Prompt(promptString,
//                            OpenAiChatOptions.builder().withFunction("saveProfile").build()));

                System.out.println("printing response: ");
                System.out.println(response.getResult().getOutput().getContent());
            }
//            }
        }
    }

    private void saveProfilesToJson(List<Profile> generatedProfiles) {
        generateImageForExistingProfiles();
    }

    public void generateImageForExistingProfiles() {
        try {
            Gson gson = new Gson();
            List<Profile> existingProfiles = gson.fromJson(
                    new FileReader(PROFILES_FILE_PATH),
                    new TypeToken<List<Profile>>() {
                    }.getType()
            );
            generatedProfiles.addAll(existingProfiles);
            List<Profile> profilesWithImages = new ArrayList<>();
            for (Profile profile : generatedProfiles) {
                if (profile.photoUrl() == null) {
                    profile = generateProfileImage(profile);
                }
                profilesWithImages.add(profile);
            }
            String jsonString = gson.toJson(profilesWithImages);
            FileWriter writer = new FileWriter(PROFILES_FILE_PATH);
            writer.write(jsonString);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Profile generateProfileImage(Profile profile) {

        String uuid = UUID.randomUUID().toString();
        profile = new Profile(
                uuid,
                profile.firstName(),
                profile.lastName(),
                profile.age(),
                profile.gender(),
                profile.ethnicity(),
                profile.bio(),
                uuid + ".jpg", // replace with actual image URL
                profile.myersBriggsPersonalityType()
        );

//      get the profile details to generate image
        String prompt = "Generate a profile image of " +
                profile.age() + " years old " +
                profile.ethnicity() + " " + profile.gender().toString() + " with ";
        prompt = prompt + "Personality: " + profile.myersBriggsPersonalityType() + ", and ";
        prompt = prompt + "bio: " + profile.bio() + "";

        prompt = prompt + ". Ultrarealistic, 4k DSLR, ultrarealistic, best quality, masterpiece.";

        String negativePromt = "lowres, text, error, cropped, worst quality, low quality, jpeg artifacts, ugly, duplicate, morbid, mutilated, " +
                "out of frame, extra fingers, mutated hands, poorly drawn hands, poorly drawn face, mutation, deformed, blurry, dehydrated, b" +
                "ad anatomy, bad proportions, extra limbs, cloned face, disfigured, gross proportions, malformed limbs, missing arms, missing " +
                "legs, extra arms, extra legs, fused fingers, too many fingers, long neck, username, watermark, signature";

//        String jsonString = STR. """
//                {"prompt": \{prompt}, "negative_prompt": \{negativePromt} }
//                """;
        String jsonString = "{" +
                "\"prompt\": \"" + prompt + "\", " +
                "\"negative_prompt\": \"" + negativePromt + "\", " +
                "\"steps\": 40" +
                "}";
//        String jsonString = """
//                {"promt": }
//                """
//                "{" +
//                "\"prompt\": \"" + prompt + "\", " +
//                "\"negative_prompt\": \"" + negativePromt + "\" " +
//                "\"steps\": 40" +
//                "}";

//        HttpRequest.Builder stableDiffusionRequestBuilder = httpRequest.newBuilder()
//                .setHeader("Content-type", "application/json")
//                .uri(URI.create(STABLE_DIFFUSION_URL));

        HttpRequest request = this.stableDiffusionRequestBuilder.POST(
                HttpRequest.BodyPublishers.ofString(jsonString)
        ).build();

        HttpResponse response;
        try {
            response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

//        ChatResponse response = ollamaChatModel.call(new Prompt(prompt));
//        String imageUrl = response.getResult().getOutput().getContent();

//      save the image url in the photoUrl field in the profile
        record ImageResponse(List<String> images) {
        }
        ;

        Gson gson = new Gson();
        ImageResponse imageResponse = gson.fromJson((String) response.body(), ImageResponse.class);
        if (imageResponse.images() != null && !imageResponse.images().isEmpty()) {
            String base64Image = imageResponse.images().getFirst();

            //Decode Base64 to binary
            byte[] decodedBytes = Base64.getDecoder().decode(base64Image);
            String directoryPath = "src/main/resources/static/images/";
            String filePath = directoryPath + profile.photoUrl();
            Path directory = Paths.get(directoryPath);

            if (!Files.exists(directory)) {
                try {
                    Files.createDirectories(directory);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            try (FileOutputStream imageOutputStream = new FileOutputStream(filePath)) {
                imageOutputStream.write(decodedBytes);
            } catch (IOException e) {
                return null;
            }
        }


//      generate profile image using stable diffusion running locally


//      save the generated profile image in the resource folder


//      save the image url in the photoUrl field in the profile


        return profile;
    }

    @Bean
    @Description("save the profile information")
    public Function<Profile, Boolean> saveProfile() {
        return (profile) -> {
            System.out.println("this function needs to be called by spring ai by looking at the ollama response");
            System.out.println(profile);
            this.generatedProfiles.add(profile);
            return true;
        };
    }
}
