package io.openur.domain.admin.service;

import io.openur.domain.admin.dto.AdminChallengeDto;
import io.openur.domain.admin.dto.AdminChallengeRequestDto;
import io.openur.domain.admin.dto.AdminChallengeStageRequestDto;
import io.openur.domain.admin.exception.AdminChallengeConflictException;
import io.openur.domain.challenge.entity.ChallengeEntity;
import io.openur.domain.challenge.entity.ChallengeStageEntity;
import io.openur.domain.challenge.repository.ChallengeJpaRepository;
import io.openur.domain.userchallenge.repository.UserChallengeJpaRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminChallengeService {

    private final ChallengeJpaRepository challengeJpaRepository;
    private final UserChallengeJpaRepository userChallengeJpaRepository;

    @Transactional(readOnly = true)
    public List<AdminChallengeDto> getChallenges() {
        return challengeJpaRepository.findAllByOrderByChallengeIdAsc().stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public AdminChallengeDto getChallenge(Long challengeId) {
        return toDto(getChallengeEntity(challengeId));
    }

    @Transactional
    public AdminChallengeDto createChallenge(AdminChallengeRequestDto request) {
        validateRequest(request);
        if (request.getStages().stream().anyMatch(stage -> stage.getStageId() != null)) {
            throw new IllegalArgumentException("stageId must be empty when creating challenge");
        }

        ChallengeEntity challenge = new ChallengeEntity(
            null,
            request.getName().trim(),
            request.getDescription().trim(),
            request.getChallengeType(),
            request.getRewardType(),
            request.getCompletedType(),
            request.getConditionDate(),
            normalizeBlank(request.getConditionText()),
            new ArrayList<>()
        );

        getSortedStages(request).forEach(stageRequest -> challenge.addStage(new ChallengeStageEntity(
            null,
            stageRequest.getStageNumber(),
            stageRequest.getConditionCount(),
            challenge
        )));

        return toDto(challengeJpaRepository.saveAndFlush(challenge));
    }

    @Transactional
    public AdminChallengeDto updateChallenge(Long challengeId, AdminChallengeRequestDto request) {
        validateRequest(request);

        ChallengeEntity challenge = getChallengeEntity(challengeId);
        challenge.update(
            request.getName().trim(),
            request.getDescription().trim(),
            request.getChallengeType(),
            request.getRewardType(),
            request.getCompletedType(),
            request.getConditionDate(),
            normalizeBlank(request.getConditionText())
        );

        List<ChallengeStageEntity> existingStages = new ArrayList<>(getStages(challenge));
        Map<Long, ChallengeStageEntity> existingStagesById = existingStages.stream()
            .collect(Collectors.toMap(ChallengeStageEntity::getStageId, Function.identity()));
        Set<Long> requestedExistingStageIds = getSortedStages(request).stream()
            .map(AdminChallengeStageRequestDto::getStageId)
            .filter(stageId -> stageId != null)
            .collect(Collectors.toSet());

        for (AdminChallengeStageRequestDto stageRequest : getSortedStages(request)) {
            Long stageId = stageRequest.getStageId();
            if (stageId == null) {
                challenge.addStage(new ChallengeStageEntity(
                    null,
                    stageRequest.getStageNumber(),
                    stageRequest.getConditionCount(),
                    challenge
                ));
                continue;
            }

            ChallengeStageEntity existingStage = existingStagesById.get(stageId);
            if (existingStage == null) {
                throw new IllegalArgumentException("stageId does not belong to challenge: " + stageId);
            }

            existingStage.update(stageRequest.getStageNumber(), stageRequest.getConditionCount());
        }

        existingStages.stream()
            .filter(stage -> !requestedExistingStageIds.contains(stage.getStageId()))
            .forEach(stage -> {
                long assignedCount = getStageAssignedCount(stage.getStageId());
                if (assignedCount > 0) {
                    throw new AdminChallengeConflictException(
                        "Cannot remove assigned challenge stage: " + stage.getStageId()
                    );
                }
                challenge.removeStage(stage);
            });

        return toDto(challengeJpaRepository.saveAndFlush(challenge));
    }

    @Transactional
    public void deleteChallenge(Long challengeId) {
        ChallengeEntity challenge = getChallengeEntity(challengeId);
        long assignedCount = getChallengeAssignedCount(challenge.getChallengeId());
        if (assignedCount > 0) {
            throw new AdminChallengeConflictException("Cannot delete assigned challenge: " + challengeId);
        }

        challengeJpaRepository.delete(challenge);
    }

    private AdminChallengeDto toDto(ChallengeEntity challenge) {
        long challengeAssignedCount = getChallengeAssignedCount(challenge.getChallengeId());
        Map<Long, Long> stageAssignmentCounts = getStages(challenge).stream()
            .collect(Collectors.toMap(
                ChallengeStageEntity::getStageId,
                stage -> getStageAssignedCount(stage.getStageId())
            ));

        return AdminChallengeDto.from(challenge, challengeAssignedCount, stageAssignmentCounts);
    }

    private ChallengeEntity getChallengeEntity(Long challengeId) {
        if (challengeId == null) {
            throw new IllegalArgumentException("challengeId is required");
        }

        return challengeJpaRepository.findByChallengeId(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("challenge not found: " + challengeId));
    }

    private void validateRequest(AdminChallengeRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("request body is required");
        }
        if (request.getStages() == null || request.getStages().isEmpty()) {
            throw new IllegalArgumentException("stages is required");
        }

        Set<Integer> stageNumbers = new HashSet<>();
        Set<Long> stageIds = new HashSet<>();
        for (AdminChallengeStageRequestDto stage : request.getStages()) {
            if (stage == null) {
                throw new IllegalArgumentException("stage is required");
            }
            if (stage.getStageNumber() == null || stage.getStageNumber() <= 0) {
                throw new IllegalArgumentException("stageNumber must be positive");
            }
            if (stage.getConditionCount() == null || stage.getConditionCount() <= 0) {
                throw new IllegalArgumentException("conditionCount must be positive");
            }
            if (!stageNumbers.add(stage.getStageNumber())) {
                throw new IllegalArgumentException("stageNumber must be unique");
            }
            if (stage.getStageId() != null && !stageIds.add(stage.getStageId())) {
                throw new IllegalArgumentException("stageId must be unique");
            }
        }
    }

    private List<AdminChallengeStageRequestDto> getSortedStages(AdminChallengeRequestDto request) {
        return request.getStages().stream()
            .sorted(Comparator.comparing(AdminChallengeStageRequestDto::getStageNumber))
            .toList();
    }

    private List<ChallengeStageEntity> getStages(ChallengeEntity challenge) {
        if (challenge.getChallengeStages() == null) {
            return List.of();
        }

        return challenge.getChallengeStages();
    }

    private long getChallengeAssignedCount(Long challengeId) {
        return userChallengeJpaRepository.countByChallengeStageEntityChallengeEntityChallengeId(challengeId);
    }

    private long getStageAssignedCount(Long stageId) {
        return userChallengeJpaRepository.countByChallengeStageEntityStageId(stageId);
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
