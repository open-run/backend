package io.openur.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import io.openur.config.TestSupport;
import io.openur.domain.bung.dto.BungInfoDto;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.dto.BungInfoWithOwnershipDto;
import io.openur.domain.bung.enums.CompleteBungResultEnum;
import io.openur.domain.bung.enums.EditBungResultEnum;
import io.openur.domain.bung.enums.GetBungResultEnum;
import io.openur.domain.bung.enums.JoinBungResultEnum;
import io.openur.domain.bung.exception.GetBungException;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.repository.BungRepository;
import io.openur.domain.bunghashtag.repository.BungHashtagRepository;
import io.openur.domain.hashtag.model.Hashtag;
import io.openur.domain.hashtag.repository.HashtagRepository;
import io.openur.domain.userbung.repository.UserBungJpaRepository;
import io.openur.domain.userchallenge.entity.UserChallengeEntity;
import io.openur.domain.userchallenge.repository.UserChallengeJpaRepository;
import io.openur.global.dto.PagedResponse;
import io.openur.global.dto.Response;
import io.openur.global.dto.ExceptionDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class BungApiTest extends TestSupport {

    private static final String PREFIX = "/v1/bungs";
    @Autowired
    protected UserBungJpaRepository userBungJpaRepository;
    @Autowired
    protected HashtagRepository hashtagRepository;
    @Autowired
    protected BungHashtagRepository bungHashtagRepository;
    @Autowired
    private BungRepository bungRepository;
    @Autowired
    private UserChallengeJpaRepository userChallengeJpaRepository;

    @Test
    @DisplayName("벙 생성")
    @Transactional
    void createBungTest() throws Exception {
        String token = getTestUserToken1();

        var submittedBung = new HashMap<>();
        List<String> hashtags = Arrays.asList("LSD", "음악있음", "밤산책");
        submittedBung.put("name", "이름");
        submittedBung.put("description", "설명");
        submittedBung.put("location", "장소");
        submittedBung.put("startDateTime", LocalDateTime.now().plusDays(3).toString());
        submittedBung.put("endDateTime", LocalDateTime.now().plusDays(4).toString());
        submittedBung.put("distance", "10.5");
        submittedBung.put("pace", "5'55\"");
        submittedBung.put("memberNumber", 5);
        submittedBung.put("hasAfterRun", false);
        submittedBung.put("afterRunDescription", "");
        submittedBung.put("hashtags", hashtags);
        submittedBung.put("mainImage", "image1.jpg");

        MvcResult result = mockMvc.perform(
            post(PREFIX)
                .header(AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonify(submittedBung))
        ).andExpect(status().isCreated()).andReturn();

        String location = result.getResponse().getHeader("Location");
        assertNotNull(location, "Location header must not be null");

        String bungId = location.substring(location.lastIndexOf('/') + 1);
        Bung bungEntity = bungRepository.findBungById(bungId);
        assert bungId.equals(bungEntity.getBungId());

        assert bungHashtagRepository.findHashtagsByBungId(bungId).stream()
            .map(Hashtag::getHashtagStr).toList().containsAll(hashtags);
        assert hashtagRepository.findByHashtagStrIn(hashtags).stream().map(Hashtag::getHashtagStr)
            .toList().containsAll(hashtags);
        assert bungEntity.getMainImage().equals("image1.jpg");
    }

    @Test
    @DisplayName("벙 생성 시 벙 생성 도전과제만 카운트된다")
    @Transactional
    void createBung_countsCreateChallenges() throws Exception {
        // user2: 도전과제 진행 기록이 없는 유저 (lazy 초기화부터 트리거까지 전체 경로 검증)
        String token = getTestUserToken2();
        String userId = "91b4928f-8288-44dc-a04d-640911f0b2be";

        createBung(token, "도전과제 트리거 벙");

        List<UserChallengeEntity> rows = findUserChallenges(userId);

        // 벙 생성 매핑 과제 2번(repetitive): 1단계(조건 1) 완료 + 2단계 발급, 누적 1 승계
        UserChallengeEntity challenge2Stage1 = findRow(rows, 2L, 1);
        assertThat(challenge2Stage1.getCompletedDate()).isNotNull();
        assertThat(challenge2Stage1.getCurrentCount()).isEqualTo(1);

        UserChallengeEntity challenge2Stage2 = findRow(rows, 2L, 2);
        assertThat(challenge2Stage2.getCompletedDate()).isNull();
        assertThat(challenge2Stage2.getCurrentCount()).isEqualTo(1);

        // 벙 생성 매핑 과제 6번(normal, 조건 1회): 첫 생성으로 완료
        UserChallengeEntity challenge6 = findRow(rows, 6L, 1);
        assertThat(challenge6.getCompletedDate()).isNotNull();
        assertThat(challenge6.getCurrentCount()).isEqualTo(1);

        // 벙 생성과 무관한 과제(1번)는 초기화만 되고 카운트되지 않는다
        UserChallengeEntity unrelated = findRow(rows, 1L, 1);
        assertThat(unrelated.getCompletedDate()).isNull();
        assertThat(unrelated.getCurrentCount()).isZero();

        // 2번째 생성: 2단계(누적 3회 조건) 미달이므로 카운트만 오른다 (raise 경로)
        createBung(token, "도전과제 트리거 벙 2");

        UserChallengeEntity stage2After = findRow(findUserChallenges(userId), 2L, 2);
        assertThat(stage2After.getCompletedDate()).isNull();
        assertThat(stage2After.getCurrentCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("진행 기록이 있는 기존 유저에게도 새 도전과제가 차집합 발급되고 트리거가 동작한다")
    @Transactional
    void createBung_issuesNewChallengesToExistingUser() throws Exception {
        // user1: 시드에 과제 1, 2 진행 기록이 이미 있는 유저
        String token = getTestUserToken1();
        String userId = "9e1bfc60-f76a-47dc-9147-803653707192";

        createBung(token, "기존 유저 차집합 발급 벙");

        List<UserChallengeEntity> rows = findUserChallenges(userId);

        // 기록이 없던 과제 6은 새로 발급되고, 벙 생성 트리거로 즉시 완료된다
        UserChallengeEntity challenge6 = findRow(rows, 6L, 1);
        assertThat(challenge6.getCompletedDate()).isNotNull();
        assertThat(challenge6.getCurrentCount()).isEqualTo(1);

        // 이미 row가 있는 과제 2는 차집합 발급 대상이 아니다 (기존 완료 row 1개 그대로)
        List<UserChallengeEntity> challenge2Rows = rows.stream()
            .filter(uc -> uc.getChallengeStageEntity().getChallengeEntity()
                .getChallengeId().equals(2L))
            .toList();
        assertThat(challenge2Rows).hasSize(1);
        assertThat(challenge2Rows.get(0).getCompletedDate()).isNotNull();

        // 벙 생성과 무관한 신규 과제(3번)는 발급만 되고 카운트되지 않는다
        UserChallengeEntity challenge3 = findRow(rows, 3L, 1);
        assertThat(challenge3.getCompletedDate()).isNull();
        assertThat(challenge3.getCurrentCount()).isZero();
    }

    private void createBung(String token, String name) throws Exception {
        var submittedBung = new HashMap<>();
        submittedBung.put("name", name);
        submittedBung.put("description", "설명");
        submittedBung.put("location", "장소");
        submittedBung.put("startDateTime", LocalDateTime.now().plusDays(3).toString());
        submittedBung.put("endDateTime", LocalDateTime.now().plusDays(4).toString());
        submittedBung.put("distance", "5.0");
        submittedBung.put("pace", "6'00\"");
        submittedBung.put("memberNumber", 5);
        submittedBung.put("hasAfterRun", false);
        submittedBung.put("afterRunDescription", "");
        submittedBung.put("hashtags", List.of());
        submittedBung.put("mainImage", "image1.jpg");

        mockMvc.perform(
            post(PREFIX)
                .header(AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonify(submittedBung))
        ).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("벙 참여 시 벙 참여 도전과제만 카운트된다")
    @Transactional
    void joinBung_countsJoinChallenges() throws Exception {
        // user2가 user3 소유의 미시작 벙에 참여한다
        String token = getTestUserToken2();
        String userId = "91b4928f-8288-44dc-a04d-640911f0b2be";
        String bungId = "90477004-1422-4551-acce-04584b34612e";

        mockMvc.perform(
            get(PREFIX + "/{bungId}/join", bungId)
                .header(AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        List<UserChallengeEntity> rows = findUserChallenges(userId);

        // 벙 참여 매핑 과제 3번(repetitive): 1단계(조건 1) 완료 + 2단계 발급, 누적 1 승계
        UserChallengeEntity challenge3Stage1 = findRow(rows, 3L, 1);
        assertThat(challenge3Stage1.getCompletedDate()).isNotNull();
        assertThat(challenge3Stage1.getCurrentCount()).isEqualTo(1);

        UserChallengeEntity challenge3Stage2 = findRow(rows, 3L, 2);
        assertThat(challenge3Stage2.getCompletedDate()).isNull();
        assertThat(challenge3Stage2.getCurrentCount()).isEqualTo(1);

        // 벙 참여 매핑 과제 5번(normal, 조건 1회): 첫 참여로 완료
        UserChallengeEntity challenge5 = findRow(rows, 5L, 1);
        assertThat(challenge5.getCompletedDate()).isNotNull();
        assertThat(challenge5.getCurrentCount()).isEqualTo(1);

        // 벙 생성 매핑 과제(2번, 6번)는 참여로는 오르지 않는다
        UserChallengeEntity challenge2 = findRow(rows, 2L, 1);
        assertThat(challenge2.getCompletedDate()).isNull();
        assertThat(challenge2.getCurrentCount()).isZero();

        UserChallengeEntity challenge6 = findRow(rows, 6L, 1);
        assertThat(challenge6.getCompletedDate()).isNull();
        assertThat(challenge6.getCurrentCount()).isZero();
    }

    @Test
    @DisplayName("완주 과제는 벙주는 완료 시점, 벙원은 피드백 제출 시점에 카운트된다")
    @Transactional
    void completeBung_countsOwnerThenMemberOnFeedback() throws Exception {
        // 시드: user2(벙주), user3(벙원)
        String bungId = "b1234567-89ab-cdef-0123-456789abcdef";
        String ownerUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";
        String memberUserId = "5d22bd65-f1ed-4e7b-bc7b-0a59580d3176";

        mockMvc.perform(
            patch(PREFIX + "/{bungId}/complete", bungId)
                .header(AUTH_HEADER, getTestUserToken2())
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        // 벙주: 완료 즉시 19번 1단계 완료 + 2단계 승계(누적 1), 7번 완료
        List<UserChallengeEntity> ownerRows = findUserChallenges(ownerUserId);
        assertThat(findRow(ownerRows, 19L, 1).getCompletedDate()).isNotNull();
        assertThat(findRow(ownerRows, 19L, 2).getCurrentCount()).isEqualTo(1);
        assertThat(findRow(ownerRows, 7L, 1).getCompletedDate()).isNotNull();

        // 벙원: 완료 시점에는 아직 카운트되지 않는다 (진행표 발급 전)
        assertThat(findUserChallenges(memberUserId)).isEmpty();

        // 벙원이 피드백 제출 → 완주 과제 카운트
        submitFeedback(getTestUserToken3(), bungId);
        List<UserChallengeEntity> memberRows = findUserChallenges(memberUserId);
        assertThat(findRow(memberRows, 19L, 1).getCompletedDate()).isNotNull();
        assertThat(findRow(memberRows, 19L, 2).getCurrentCount()).isEqualTo(1);
        assertThat(findRow(memberRows, 7L, 1).getCompletedDate()).isNotNull();

        // 벙원 피드백 재제출은 무시된다
        submitFeedback(getTestUserToken3(), bungId);
        assertThat(findRow(findUserChallenges(memberUserId), 19L, 2).getCurrentCount())
            .isEqualTo(1);

        // 벙주는 피드백을 제출해도 다시 카운트되지 않는다
        submitFeedback(getTestUserToken2(), bungId);
        assertThat(findRow(findUserChallenges(ownerUserId), 19L, 2).getCurrentCount())
            .isEqualTo(1);
    }

    private void submitFeedback(String token, String bungId) throws Exception {
        var feedbackRequest = new HashMap<>();
        feedbackRequest.put("bungId", bungId);
        feedbackRequest.put("targetUserIds", List.of());

        mockMvc.perform(
            patch("/v1/users/feedback")
                .header(AUTH_HEADER, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonify(feedbackRequest))
        ).andExpect(status().isOk());
    }

    private List<UserChallengeEntity> findUserChallenges(String userId) {
        return userChallengeJpaRepository.findAll().stream()
            .filter(uc -> uc.getUserEntity().getUserId().equals(userId))
            .toList();
    }

    private UserChallengeEntity findRow(
        List<UserChallengeEntity> rows, Long challengeId, int stageNumber
    ) {
        return rows.stream()
            .filter(uc -> uc.getChallengeStageEntity().getChallengeEntity()
                .getChallengeId().equals(challengeId))
            .filter(uc -> uc.getChallengeStageEntity().getStageNumber() == stageNumber)
            .findFirst().orElseThrow();
    }

    @Nested
    @DisplayName("벙 목록 조회")
    class getBungListTest {

        @Test
        @DisplayName("200 OK.")
        void getBungList_isOk() throws Exception {
            String token = getTestUserToken2();
            MvcResult result = mockMvc.perform(
                get(PREFIX)
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].memberList[0].profileImageUrl")
                    .value("https://storage.googleapis.com/openrun-nft/profile-images/users/"
                        + "5d22bd65-f1ed-4e7b-bc7b-0a59580d3176/profile.png"))
                .andReturn();

            PagedResponse<BungInfoWithMemberListDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
            );
            assert !response.isEmpty();
            List<BungInfoWithMemberListDto> bungInfoDtoList = response.getData();
            assert bungInfoDtoList.size() == 1;
            BungInfoWithMemberListDto oneBung = bungInfoDtoList.get(0);
            assert !oneBung.getHashtags().isEmpty();
        }
    }

    @Nested
    @DisplayName("벙 통합 검색")
    class searchBungsTest {

        @Test
        @DisplayName("벙 이름 일부로 검색하면 이름 카테고리에 결과가 반환된다")
        @Transactional
        void searchBungs_matchesNameByPartialKeyword() throws Exception {
            String token = getTestUserToken2();
            createBung(
                token,
                "[MOCK] 아침 초보환영 런 #057",
                "서울시 강남구",
                List.of("초보", "아침런")
            );

            mockMvc.perform(
                get(PREFIX + "/search")
                    .header(AUTH_HEADER, token)
                    .param("keyword", "초보환영")
                    .param("limit", "5")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.keyword").value("초보환영"))
                .andExpect(jsonPath("$.data.categories", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.categories[0].category").value("NAME"))
                .andExpect(jsonPath("$.data.categories[0].label").value("이름"))
                .andExpect(jsonPath("$.data.categories[0].data[0].name").value("[MOCK] 아침 초보환영 런 #057"))
                .andExpect(jsonPath("$.data.categories[0].empty").value(false));
        }

        @Test
        @DisplayName("위치 일부로 검색하면 위치 카테고리에 결과가 반환된다")
        void searchBungs_matchesLocationByPartialKeyword() throws Exception {
            String token = getTestUserToken2();

            mockMvc.perform(
                get(PREFIX + "/search")
                    .header(AUTH_HEADER, token)
                    .param("keyword", "Seou")
                    .param("limit", "5")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.keyword").value("Seou"))
                .andExpect(jsonPath("$.data.categories", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.categories[0].category").value("LOCATION"))
                .andExpect(jsonPath("$.data.categories[0].label").value("위치"))
                .andExpect(jsonPath("$.data.categories[0].data[0].location").value("Seoul"));
        }

        @Test
        @DisplayName("해시태그 일부로 검색하면 해시태그 카테고리에 결과가 반환된다")
        void searchBungs_matchesHashtagByPartialKeyword() throws Exception {
            String token = getTestUserToken2();

            mockMvc.perform(
                get(PREFIX + "/search")
                    .header(AUTH_HEADER, token)
                    .param("keyword", "런린")
                    .param("limit", "5")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.keyword").value("런린"))
                .andExpect(jsonPath("$.data.categories", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.categories[0].category").value("HASHTAG"))
                .andExpect(jsonPath("$.data.categories[0].label").value("해시태그"))
                .andExpect(jsonPath("$.data.categories[0].data[0].hashtags[1]").value("런린이"));
        }

        @Test
        @DisplayName("멤버 닉네임 일부로 검색하면 멤버 카테고리에 결과가 반환된다")
        void searchBungs_matchesMemberByPartialKeyword() throws Exception {
            String token = getTestUserToken2();

            mockMvc.perform(
                get(PREFIX + "/search")
                    .header(AUTH_HEADER, token)
                    .param("keyword", "est3")
                    .param("limit", "5")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.keyword").value("est3"))
                .andExpect(jsonPath("$.data.categories", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.categories[0].category").value("MEMBER"))
                .andExpect(jsonPath("$.data.categories[0].label").value("멤버"))
                .andExpect(jsonPath("$.data.categories[0].data[0].memberList[0].nickname").value("test3"));
        }

        @Test
        @DisplayName("카테고리를 지정하면 해당 카테고리만 페이지 조회된다")
        void searchBungs_withCategoryReturnsOneCategoryPage() throws Exception {
            String token = getTestUserToken2();

            mockMvc.perform(
                get(PREFIX + "/search")
                    .header(AUTH_HEADER, token)
                    .param("keyword", "test1")
                    .param("category", "NAME")
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.keyword").value("test1"))
                .andExpect(jsonPath("$.data.categories", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.categories[0].category").value("NAME"))
                .andExpect(jsonPath("$.data.categories[0].totalElements").value(1))
                .andExpect(jsonPath("$.data.categories[0].first").value(true))
                .andExpect(jsonPath("$.data.categories[0].last").value(true))
                .andExpect(jsonPath("$.data.categories[0].data[0].name").value("test1_bung"));
        }

        @Test
        @DisplayName("검색어가 없거나 두 글자 미만이면 검색할 수 없다")
        void searchBungs_rejectsInvalidKeyword() throws Exception {
            String token = getTestUserToken2();

            mockMvc.perform(
                get(PREFIX + "/search")
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isBadRequest());

            mockMvc.perform(
                get(PREFIX + "/search")
                    .header(AUTH_HEADER, token)
                    .param("keyword", "a")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isBadRequest());
        }

        private void createBung(String token, String name, String location, List<String> hashtags) throws Exception {
            var submittedBung = new HashMap<>();
            submittedBung.put("name", name);
            submittedBung.put("description", "통합 검색 테스트 벙");
            submittedBung.put("location", location);
            submittedBung.put("startDateTime", LocalDateTime.now().plusDays(3).toString());
            submittedBung.put("endDateTime", LocalDateTime.now().plusDays(4).toString());
            submittedBung.put("distance", "5.7");
            submittedBung.put("pace", "6'00\"");
            submittedBung.put("memberNumber", 5);
            submittedBung.put("hasAfterRun", false);
            submittedBung.put("afterRunDescription", "");
            submittedBung.put("hashtags", hashtags);
            submittedBung.put("mainImage", "search-test.png");

            mockMvc.perform(
                post(PREFIX)
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonify(submittedBung))
            ).andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("내 벙 목록 조회")
    class getMyBungListTest {

        @Test
        @DisplayName("200 OK. isOwned = null, status = null. 내가 소유 및 참가했던 모든 벙. 가장 먼 미래 순으로.")
        void getMyBungList_isOk() throws Exception {
            String token = getTestUserToken1();
            MvcResult result = mockMvc.perform(
                get(PREFIX + "/my-bungs")
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            PagedResponse<BungInfoWithOwnershipDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
            );
            assert !response.getData().isEmpty();
            assert !response.getData().get(0).getHashtags().isEmpty();

            List<LocalDateTime> startDateTimes = response.getData().stream()
                .map(BungInfoDto::getStartDateTime)
                .toList();
            List<LocalDateTime> sortedStartDateTimes = new ArrayList<>(startDateTimes);
            sortedStartDateTimes.sort(Comparator.naturalOrder());
            assert startDateTimes.equals(
                sortedStartDateTimes) : "The list is not in descending order of startDateTime";
        }

        @Test
        @DisplayName("200 OK. isOwned = false, status = ACCOMPLISHED. 일반 참여자가 완료된 벙 피드백 창구로 볼 수 있다.")
        @Transactional
        void getMyBungList_completedParticipantBungsForFeedback() throws Exception {
            String completedBungId = "a1234567-89ab-cdef-0123-456789abcdef";

            mockMvc.perform(
                patch(PREFIX + "/" + completedBungId + "/complete")
                    .header(AUTH_HEADER, getTestUserToken2())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk());

            MvcResult result = mockMvc.perform(
                get(PREFIX + "/my-bungs")
                    .header(AUTH_HEADER, getTestUserToken1())
                    .param("isOwned", "false")
                    .param("status", "ACCOMPLISHED")
                    .param("feedbackPending", "true")
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            PagedResponse<BungInfoWithOwnershipDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
            );

            assertThat(response.getData())
                .extracting(BungInfoDto::getBungId)
                .containsExactly(completedBungId);
            assertThat(response.getData())
                .allMatch(bung -> !bung.isHasOwnership());
        }

        @Test
        @DisplayName("200 OK. feedbackPending = true. 피드백 제출을 완료한 벙은 목록에서 제외된다.")
        @Transactional
        void getMyBungList_feedbackPendingExcludesSubmittedBungs() throws Exception {
            String completedBungId = "a1234567-89ab-cdef-0123-456789abcdef";

            mockMvc.perform(
                patch(PREFIX + "/" + completedBungId + "/complete")
                    .header(AUTH_HEADER, getTestUserToken2())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk());

            var feedbackRequest = new HashMap<>();
            feedbackRequest.put("bungId", completedBungId);
            feedbackRequest.put("targetUserIds", List.of());

            mockMvc.perform(
                patch("/v1/users/feedback")
                    .header(AUTH_HEADER, getTestUserToken1())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonify(feedbackRequest))
            ).andExpect(status().isOk());

            MvcResult pendingResult = mockMvc.perform(
                get(PREFIX + "/my-bungs")
                    .header(AUTH_HEADER, getTestUserToken1())
                    .param("isOwned", "false")
                    .param("status", "ACCOMPLISHED")
                    .param("feedbackPending", "true")
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            PagedResponse<BungInfoWithOwnershipDto> pendingResponse = parseResponse(
                pendingResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
            );

            assertThat(pendingResponse.getData())
                .extracting(BungInfoDto::getBungId)
                .doesNotContain(completedBungId);

            MvcResult allCompletedResult = mockMvc.perform(
                get(PREFIX + "/my-bungs")
                    .header(AUTH_HEADER, getTestUserToken1())
                    .param("isOwned", "false")
                    .param("status", "ACCOMPLISHED")
                    .param("page", "0")
                    .param("limit", "10")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            PagedResponse<BungInfoWithOwnershipDto> allCompletedResponse = parseResponse(
                allCompletedResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
            );

            assertThat(allCompletedResponse.getData())
                .extracting(BungInfoDto::getBungId)
                .contains(completedBungId);
        }
    }

    @Nested
    @DisplayName("벙 정보 상세보기")
    class getBungDetailTest {

        String bungId = "c0477004-1632-455f-acc9-04584b55921f";

        @Test
        @DisplayName("200 OK.")
        void getBungDetail_isOk() throws Exception {
            String token = getTestUserToken3();  // not owner of the bung
            MvcResult result = mockMvc.perform(
                get(PREFIX + "/" + bungId)
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            Response<BungInfoWithMemberListDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
            );

            assert response.getData().getHashtags() != null && !response.getData().getHashtags().isEmpty();
            assert !response.getData().getMemberList().isEmpty();
            assert response.getData().getMemberList().size() == response.getData()
                .getMemberNumber();
            assert response.getData().getMainImage().equals("image1.png");
        }
    }

    @Nested
    @DisplayName("벙 삭제")
    class deleteBungTest {

        String bungId = "c0477004-1632-455f-acc9-04584b55921f";

        @Test
        @DisplayName("403 Forbidden. Authorization Header 없음")
        void deleteBung_isForbidden() throws Exception {
            mockMvc.perform(
                    delete(PREFIX + "/" + bungId)
                )
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("403 Forbidden. Bung owner 가 아닌 경우")
        void deleteBung_isForbidden_notOwner() throws Exception {
            String notOwnerToken = getTestUserToken2();
            mockMvc.perform(
                delete(PREFIX + "/" + bungId)
                    .header(AUTH_HEADER, notOwnerToken)
            ).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("401 Unauthorized. invalid Authorization Header")
        void deleteBung_isUnauthorized() throws Exception {
            String invalidToken = "Bearer invalidToken";
            mockMvc.perform(
                delete(PREFIX + "/" + bungId)
                    .header(AUTH_HEADER, invalidToken)
            ).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("401 Unauthorized. Unknown user token")
        void deleteBung_isUnauthorized_unknownUser() throws Exception {
            String unknownUserToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ5ZWppbmtlbGx5am9vQGdtYWlsLmNvbSIsImV4cCI6MTcyMzYyNDgxMCwiaWF0IjoxNzIzNjIxMjEwfQ.wH-eJCvEBgFg_QjWr7CdxBpMqlQzGt45DLmrsWju-HU";
            mockMvc.perform(
                delete(PREFIX + "/" + bungId)
                    .header(AUTH_HEADER, unknownUserToken)
            ).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("200 Ok.")
        @Transactional
        void deleteBung_isOk() throws Exception {
            String token = getTestUserToken1();
            mockMvc.perform(
                delete(PREFIX + "/" + bungId)
                    .header(AUTH_HEADER, token)
            ).andExpect(status().isOk());
            
            GetBungException exception = assertThrows(
                GetBungException.class, () -> bungRepository.findBungById(bungId)
            );

            assertThat(exception.getMessage().equals(GetBungResultEnum.BUNG_NOT_FOUND.toString()));
            assertThat(userBungJpaRepository.findByBungEntity_BungId(bungId)).isEmpty();
        }

    }

    @Nested
    @DisplayName("벙 참가")
    class joinBungTest {

        @Test
        @DisplayName("409 Conflict. 이미 참가한 경우")
        void joinBung_isConflict_alreadyJoined() throws Exception {
            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
            String token = getTestUserToken1();
            MvcResult result = mockMvc.perform(
                get(PREFIX + "/" + bungId + "/join")
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isConflict()).andReturn();

            ExceptionDto response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
            assert Objects.equals(response.getMessage(),
                JoinBungResultEnum.USER_HAS_ALREADY_JOINED.toString());
        }

        @Test
        @DisplayName("409 Conflict. 벙이 이미 시작된 경우")
        void joinBung_isConflict_alreadyStarted() throws Exception {
            String bungId = "a1234567-89ab-cdef-0123-456789abcdef";
            String token = getTestUserToken1();
            MvcResult result = mockMvc.perform(
                get(PREFIX + "/" + bungId + "/join")
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isConflict()).andReturn();

            ExceptionDto response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
            assert Objects.equals(response.getMessage(),
                JoinBungResultEnum.BUNG_HAS_ALREADY_STARTED.toString());
        }

        @Test
        @DisplayName("409 Conflict. 벙 인원이 다 찬 경우")
        void joinBung_isConflict_isFull() throws Exception {
            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
            String token = getTestUserToken3();
            MvcResult result = mockMvc.perform(
                get(PREFIX + "/" + bungId + "/join")
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isConflict()).andReturn();

            ExceptionDto response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
            assert Objects.equals(response.getMessage(), JoinBungResultEnum.BUNG_IS_FULL.toString());
        }

        @Test
        @DisplayName("200 OK.")
        @Transactional
        void joinBung_isOk() throws Exception {
            String bungId = "90477004-1422-4551-acce-04584b34612e";
            String token = getTestUserToken2();
            MvcResult result = mockMvc.perform(
                get(PREFIX + "/{bungId}/join", bungId)
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            Response<JoinBungResultEnum> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
            assert response.getData() == JoinBungResultEnum.SUCCESSFULLY_JOINED;
        }
    }

    @Nested
    @DisplayName("벙 수정하기")
    class editBungTest {

        HashMap<Object, Object> getEditBungData() {
            var editBungData = new HashMap<>();
            editBungData.put("name", "새로운 이름");
            editBungData.put("description", "새로운 설명");
            editBungData.put("memberNumber", 5);
            editBungData.put("hasAfterRun", true);
            editBungData.put("afterRunDescription", "단백질 보충 가시죠! 고기고기");
            editBungData.put("hashtags", getHashtags());
            editBungData.put("mainImage", "image2.jpg");
            return editBungData;
        }

        List<String> getHashtags() {
            return Arrays.asList("LSD", "음악있음", "고수만");

        }


        @Test
        @Transactional
        @DisplayName("400 Bad Request. No elements.")
        void editBung_isOk_noElements() throws Exception {
            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
            String ownerToken = getTestUserToken1();
            mockMvc.perform(
                patch(PREFIX + "/" + bungId)
                    .header(AUTH_HEADER, ownerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonify(new HashMap<>()))
            ).andExpect(status().isBadRequest());
        }

        @Test
        @Transactional
        @DisplayName("403 Forbidden. Not owner of the bung")
        void editBung_isForbidden_notOwner() throws Exception {
            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
            String notOwnerToken = getTestUserToken2();
            mockMvc.perform(
                patch(PREFIX + "/" + bungId)
                    .header(AUTH_HEADER, notOwnerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonify(getEditBungData()))
            ).andExpect(status().isForbidden());
        }

        @Test
        @Transactional
        @DisplayName("403 Forbidden. Bung has already completed")
        void editBung_isForbidden_alreadyCompleted() throws Exception {
            String bungId = "a1234567-89ab-cdef-0123-1982ey1kbjas";
            String ownerToken = getTestUserToken3();

            MvcResult result = mockMvc.perform(
                patch(PREFIX + "/" + bungId)
                    .header(AUTH_HEADER, ownerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonify(getEditBungData()))
            ).andExpect(status().isForbidden()).andReturn();

            ExceptionDto response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
            assert Objects.equals(response.getMessage(), 
                EditBungResultEnum.BUNG_HAS_ALREADY_COMPLETED.toString());
        }

        @Test
        @Transactional
        @DisplayName("200 Ok. All elements.")
        void edit_isOk() throws Exception {
            String bungId = "c0477004-1632-455f-acc9-04584b55921f";
            String ownerToken = getTestUserToken1();
            Set<String> previousHashtags =
                bungHashtagRepository.findHashtagsByBungId(bungId).stream()
                    .map(Hashtag::getHashtagStr)
                    .collect(Collectors.toSet());

            HashMap<Object, Object> editBungData = getEditBungData();
            List<String> hashtags = getHashtags();

            mockMvc.perform(
                    patch(PREFIX + "/{bungId}" , bungId)
                        .header(AUTH_HEADER, ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonify(editBungData)))
                .andExpect(status().isOk())
                .andReturn();

            Bung bungEntity = bungRepository.findBungById(bungId);
            assertThat(bungEntity.getName()).isEqualTo(editBungData.get("name"));
            assertThat(bungEntity.getDescription()).isEqualTo(editBungData.get("description"));
            assertThat(bungEntity.getMemberNumber()).isEqualTo(editBungData.get("memberNumber"));
            assertThat(bungEntity.getHasAfterRun()).isEqualTo(editBungData.get("hasAfterRun"));
            assertThat(bungEntity.getAfterRunDescription())
                .isEqualTo(editBungData.get("afterRunDescription"));
            assertThat(bungEntity.getMainImage()).isEqualTo(editBungData.get("mainImage"));

            assert bungHashtagRepository.findHashtagsByBungId(bungId).stream()
                .map(Hashtag::getHashtagStr).toList().containsAll(hashtags);
            assert bungHashtagRepository.findHashtagsByBungId(bungId).stream()
                .map(Hashtag::getHashtagStr).noneMatch(previousHashtags::contains);
            assert hashtagRepository.findByHashtagStrIn(hashtags).stream()
                .map(Hashtag::getHashtagStr).toList().containsAll(hashtags);
        }
    }

    @Nested
    @DisplayName("벙 완료하기")
    class completeBungTest {

        @Test
        @DisplayName("409 Already Completed")
        void complete_isConflict_alreadyCompleted() throws Exception {
            String bungId = "a1234567-89ab-cdef-0123-1982ey1kbjas";
            String token = getTestUserToken3();

            MvcResult result = mockMvc.perform(
                patch(PREFIX + "/" + bungId + "/complete")
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isConflict()).andReturn();

            ExceptionDto response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
            assert Objects.equals(response.getMessage(),
                CompleteBungResultEnum.BUNG_HAS_ALREADY_COMPLETED.toString());
        }

        @Test
        @DisplayName("409 Conflict. Bung has not started")
        void complete_isConflict_hasNotStarted() throws Exception {
            String bungId = "90477004-1422-4551-acce-04584b34612e";
            String token = getTestUserToken3();
            MvcResult result = mockMvc.perform(
                patch(PREFIX + "/" + bungId + "/complete")
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isConflict()).andReturn();

            ExceptionDto response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
            assert Objects.equals(response.getMessage(),
                CompleteBungResultEnum.BUNG_HAS_NOT_STARTED.toString());
        }

        @Test
        @DisplayName("200 OK.")
        void complete_isOk() throws Exception {
            String bungId = "a1234567-89ab-cdef-0123-456789abcdef";
            String token = getTestUserToken2();
            MvcResult result = mockMvc.perform(
                patch(PREFIX + "/" + bungId + "/complete")
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            Response<CompleteBungResultEnum> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
            assert response.getData() == CompleteBungResultEnum.SUCCESSFULLY_COMPLETED;
        }
    }
}
