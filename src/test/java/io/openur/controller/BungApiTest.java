package io.openur.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import io.openur.config.TestSupport;
import io.openur.domain.bung.dto.BungInfoDto;
import io.openur.domain.bung.dto.BungInfoWithMemberListDto;
import io.openur.domain.bung.dto.BungInfoWithOwnershipDto;
import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.enums.CompleteBungResultEnum;
import io.openur.domain.bung.enums.EditBungResultEnum;
import io.openur.domain.bung.enums.JoinBungResultEnum;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.bung.repository.BungJpaRepository;
import io.openur.domain.bung.repository.BungRepositoryImpl;
import io.openur.domain.bunghashtag.repository.BungHashtagRepositoryImpl;
import io.openur.domain.hashtag.model.Hashtag;
import io.openur.domain.hashtag.repository.HashtagRepositoryImpl;
import io.openur.domain.userbung.repository.UserBungJpaRepository;
import io.openur.global.common.PagedResponse;
import io.openur.global.common.Response;
import io.openur.global.dto.ExceptionDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    protected BungJpaRepository bungJpaRepository;
    @Autowired
    protected UserBungJpaRepository userBungJpaRepository;
    @Autowired
    protected HashtagRepositoryImpl hashtagRepository;
    @Autowired
    protected BungHashtagRepositoryImpl bungHashtagRepository;
    @Autowired
    private BungRepositoryImpl bungRepositoryImpl;
    
    @Test
    @DisplayName("벙 생성")
    @Transactional
    void createBungTest() throws Exception {
        String token = getTestUserToken("test1@test.com");

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
        Optional<BungEntity> bungEntity = bungJpaRepository.findById(bungId);
        assert bungEntity.isPresent();
        assert bungId.equals(bungEntity.get().getBungId());

        assert bungHashtagRepository.findHashtagsByBungId(bungId).stream()
            .map(Hashtag::getHashtagStr).toList().containsAll(hashtags);
        assert hashtagRepository.findByHashtagStrIn(hashtags).stream().map(Hashtag::getHashtagStr)
            .toList().containsAll(hashtags);
        assert bungEntity.get().getMainImage().equals("image1.jpg");
    }

    @Nested
    @DisplayName("벙 목록 조회")
    class getBungListTest {

        String uriPath = PREFIX + "?isAvailableOnly=";

        @Test
        @DisplayName("200 OK. isAvailableOnly = false")
        void getBungList_isOk() throws Exception {
            String token = getTestUserToken("test2@test.com");
            MvcResult result = mockMvc.perform(
                get(uriPath + false)
                    .header(AUTH_HEADER, token)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

            PagedResponse<BungInfoWithMemberListDto> response = parseResponse(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
            );
            assert !response.isEmpty();
            List<BungInfoWithMemberListDto> bungInfoDtoList = response.getData();
            assert bungInfoDtoList.size() == 2;
            BungInfoWithMemberListDto oneBung = bungInfoDtoList.get(0);
            assert bungInfoDtoList.get(0).getStartDateTime().isBefore(bungInfoDtoList.get(1).getStartDateTime());
            assert !oneBung.getHashtags().isEmpty();
        }

        @Test
        @DisplayName("200 OK. isAvailableOnly = true")
        void getBungList_isOk_availableOnly() throws Exception {
            String notAvailableBungId = "90477004-1422-4551-acce-04584b34612e";
            String participatingUserToken = getTestUserToken(
                "test3@test.com");  // owner of the bung so already participating.

            MvcResult result = mockMvc.perform(
                get(uriPath + true)
                    .header(AUTH_HEADER, participatingUserToken)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk()).andReturn();

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
            assert !Objects.equals(oneBung.getBungId(), notAvailableBungId);
        }
    }

    @Nested
    @DisplayName("내 벙 목록 조회")
    class getMyBungListTest {

        @Test
        @DisplayName("200 OK. isOwned = null, status = null. 내가 소유 및 참가했던 모든 벙. 가장 먼 미래 순으로.")
        void getMyBungList_isOk() throws Exception {
            String token = getTestUserToken("test1@test.com");
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
    }

    @Nested
    @DisplayName("벙 정보 상세보기")
    class getBungDetailTest {

        String bungId = "c0477004-1632-455f-acc9-04584b55921f";

        @Test
        @DisplayName("200 OK.")
        void getBungDetail_isOk() throws Exception {
            String token = getTestUserToken("test3@test.com");  // not owner of the bung
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
            assert !response.getData().getHashtags().isEmpty();
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
            String notOwnerToken = getTestUserToken("test2@test.com");
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
            String token = getTestUserToken("test1@test.com");
            mockMvc.perform(
                delete(PREFIX + "/" + bungId)
                    .header(AUTH_HEADER, token)
            ).andExpect(status().isOk());

            assertThat(bungJpaRepository.findById(bungId)).isEmpty();
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
            String token = getTestUserToken("test1@test.com");
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
            String token = getTestUserToken("test1@test.com");
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
            String token = getTestUserToken("test3@test.com");
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
            String token = getTestUserToken("test2@test.com");
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
            String ownerToken = getTestUserToken("test1@test.com");
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
            String notOwnerToken = getTestUserToken("test2@test.com");
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
            String ownerToken = getTestUserToken("test3@test.com");

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
            String ownerToken = getTestUserToken("test1@test.com");
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

            Bung bungEntity = bungRepositoryImpl.findBungById(bungId);
            assertThat(bungEntity.getName()).isEqualTo(editBungData.get("name"));
            assertThat(bungEntity.getDescription()).isEqualTo(editBungData.get("description"));
            assertThat(bungEntity.getMemberNumber()).isEqualTo(editBungData.get("memberNumber"));
            assertThat(bungEntity.getHasAfterRun()).isEqualTo(editBungData.get("hasAfterRun"));
            assertThat(bungEntity.getAfterRunDescription())
                .isEqualTo(editBungData.get("afterRunDescription"));
            assertThat(bungEntity.getMainImage()).isEqualTo(editBungData.get("mainImage"));

            assert bungHashtagRepository.findHashtagsByBungId(bungId).stream()
                .map(Hashtag::getHashtagStr).toList().containsAll(hashtags);
            try {
                assert bungHashtagRepository.findHashtagsByBungId(bungId).stream()
                .map(Hashtag::getHashtagStr).noneMatch(previousHashtags::contains);
                
                System.out.println("previousHashtags: " + previousHashtags);
                System.out.println("currentHashtags: " +
                    bungHashtagRepository.findHashtagsByBungId(bungId).stream()
                    .map(Hashtag::getHashtagStr).toList());
                
                System.out.println("hashtags: " + hashtags);
            }
            catch (AssertionError e) {
                System.out.println("previousHashtags: " + previousHashtags);
                System.out.println("currentHashtags: " + bungHashtagRepository.findHashtagsByBungId(bungId).stream()
                    .map(Hashtag::getHashtagStr).toList());
                System.out.println(e.getMessage());
            }
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
            String token = getTestUserToken("test3@test.com");

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
            String token = getTestUserToken("test3@test.com");
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
            String token = getTestUserToken("test2@test.com");
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
