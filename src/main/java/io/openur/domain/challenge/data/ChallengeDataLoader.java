package io.openur.domain.challenge.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openur.domain.challenge.model.Challenge;
import io.openur.domain.challenge.repository.ChallengeRepositoryImpl;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Loads challenge data from JSON files during application startup. Challenge IDs are organized by type with specific
 * ranges: - Tutorial challenges: 1-1000 - Normal challenges: 1001-2000 - Hidden challenges: 2001-3000
 */
@Component
@RequiredArgsConstructor
public class ChallengeDataLoader implements CommandLineRunner {

    /**
     * Challenge ID ranges:
     * tuto-challenges.json    -> IDs: 1-1000
     * normal-challenges.json  -> IDs: 1001-2000
     * hidden-challenges.json  -> IDs: 2001-3000
     */
    private static final String[] CHALLENGE_FILES = {
        "challenges/tuto-challenges.json",
        "challenges/normal-challenges.json",
        "challenges/hidden-challenges.json"
    };
    // ID range constants for validation/reference
    private static final long TUTO_ID_START = 1L;
    private static final long NORMAL_ID_START = 1001L;
    private static final long HIDDEN_ID_START = 2001L;
    private static final long ID_RANGE_SIZE = 1000L;
    private final ChallengeRepositoryImpl challengeRepository;
    private final ObjectMapper objectMapper;  // Spring Boot autoconfigures this

    /**
     * Loads challenges from JSON files and saves them to the repository. Each challenge type has its own file and ID
     * range. Files are loaded independently, so missing files won't affect others.
     */
    @Override
    public void run(String... args) {
        List<Challenge> allChallenges = new ArrayList<>();

        for (String file : CHALLENGE_FILES) {
            try {
                InputStream inputStream = new ClassPathResource("data/" + file).getInputStream();
                Map<String, List<Challenge>> challengeData = objectMapper.readValue(inputStream,
                    new TypeReference<>() {
                    });
                List<Challenge> challenges = challengeData.get("challenges");
                validateChallengeIds(challenges, file);
                allChallenges.addAll(challenges);
            } catch (Exception e) {
                System.out.println("Warning: Could not load " + file + ": " + e.getMessage());
            }
        }

        allChallenges.forEach(challenge -> {
            if (!challengeRepository.existsById(challenge.getChallengeId())) {
                challengeRepository.save(challenge);
            }
        });
    }

    /**
     * Validates that challenge IDs are within the correct range for their type.
     *
     * @param challenges List of challenges to validate
     * @param filename   The JSON file being processed
     * @throws IllegalStateException if any challenge ID is out of range
     */
    private void validateChallengeIds(List<Challenge> challenges, String filename) {
        challenges.forEach(challenge -> {
            long id = challenge.getChallengeId();
            if (filename.contains("tuto") && (id < TUTO_ID_START || id >= NORMAL_ID_START)) {
                throw new IllegalStateException(
                    String.format("Tutorial challenge ID %d is out of range (must be between %d and %d)",
                        id, TUTO_ID_START, NORMAL_ID_START - 1));
            }
            if (filename.contains("normal") && (id < NORMAL_ID_START || id >= HIDDEN_ID_START)) {
                throw new IllegalStateException(
                    String.format("Normal challenge ID %d is out of range (must be between %d and %d)",
                        id, NORMAL_ID_START, HIDDEN_ID_START - 1));
            }
            if (filename.contains("hidden") && (id < HIDDEN_ID_START || id >= HIDDEN_ID_START + ID_RANGE_SIZE)) {
                throw new IllegalStateException(
                    String.format("Hidden challenge ID %d is out of range (must be between %d and %d)",
                        id, HIDDEN_ID_START, HIDDEN_ID_START + ID_RANGE_SIZE - 1));
            }
        });
    }
}
