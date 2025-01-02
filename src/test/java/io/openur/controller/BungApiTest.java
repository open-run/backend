package io.openur.controller;

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
import io.openur.domain.bung.repository.BungJpaRepository;
import io.openur.domain.bunghashtag.repository.BungHashtagRepositoryImpl;
import io.openur.domain.hashtag.model.Hashtag;
import io.openur.domain.hashtag.repository.HashtagRepositoryImpl;
import io.openur.global.common.PagedResponse;
import io.openur.global.common.Response;
import io.openur.global.dto.ExceptionDto;
import io.openur.global.enums.CompleteBungResultEnum;
import io.openur.global.enums.JoinBungResultEnum;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class BungApiTest extends TestSupport {

	private static final String PREFIX = "/v1/bungs";
	@Autowired
	protected BungJpaRepository bungJpaRepository;
	@Autowired
	protected HashtagRepositoryImpl hashtagRepository;
	@Autowired
	protected BungHashtagRepositoryImpl bungHashtagRepository;

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

		MvcResult result = mockMvc.perform(
			post(PREFIX)
				.header(AUTH_HEADER, token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonify(submittedBung))
		).andExpect(status().isCreated()).andReturn();

		String location = result.getResponse().getHeader("Location");
		assert location != null;

		String bungId = location.substring(location.lastIndexOf('/') + 1);
		Optional<BungEntity> bungEntity = bungJpaRepository.findById(bungId);
		assert bungEntity.isPresent();
		assert bungId.equals(bungEntity.get().getBungId());

		assert bungHashtagRepository.findHashtagsByBungId(bungId).stream()
			.map(Hashtag::getHashtagStr).toList().containsAll(hashtags);
		assert hashtagRepository.findByHashtagStrIn(hashtags).stream().map(Hashtag::getHashtagStr)
			.toList().containsAll(hashtags);
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

			PagedResponse<BungInfoDto> response = parseResponse(
				result.getResponse().getContentAsString(),
				new TypeReference<>() {
				}
			);
			assert !response.isEmpty();
			List<BungInfoDto> bungInfoDtoList = response.getData();
			assert bungInfoDtoList.size() == 2;
			BungInfoDto oneBung = bungInfoDtoList.get(0);
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

			PagedResponse<BungInfoDto> response = parseResponse(
				result.getResponse().getContentAsString(),
				new TypeReference<>() {
				}
			);
			assert !response.isEmpty();
			List<BungInfoDto> bungInfoDtoList = response.getData();
			assert bungInfoDtoList.size() == 1;
			BungInfoDto oneBung = bungInfoDtoList.get(0);
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
            sortedStartDateTimes.sort(Comparator.reverseOrder());
            assert startDateTimes.equals(
                sortedStartDateTimes) : "The list is not in descending order of startDateTime";
		}
	}

	@Nested
	@DisplayName("벙 정보 상세보기")
	class getBungDetailTest {

		String bungId = "c0477004-1632-455f-acc9-04584b55921f";

		@Test
		@DisplayName("403 Forbidden. Authorization Header 없음")
		void getBungDetail_isForbidden() throws Exception {
			mockMvc.perform(
				get(PREFIX + "/" + bungId)
			).andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("401 Unauthorized. invalid Authorization Header")
		void getBungDetail_isUnauthorized() throws Exception {
			String invalidToken = "Bearer invalidToken";
			mockMvc.perform(
				get(PREFIX + "/" + bungId)
					.header(AUTH_HEADER, invalidToken)
			).andExpect(status().isUnauthorized());
		}

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
		}
	}

	@Nested
	@DisplayName("벙 삭제")
	class deleteBungTest {

		String bungId = "c0477004-1632-455f-acc9-04584b55921f";

		@Test
		@DisplayName("403 Forbidden. Authorization Header 없음")
		@Transactional
		void deleteBung_isForbidden() throws Exception {
			mockMvc.perform(
					delete(PREFIX + "/" + bungId)
				)
				.andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("403 Forbidden. Bung owner 가 아닌 경우")
		@Transactional
		void deleteBung_isForbidden_notOwner() throws Exception {
			String notOwnerToken = getTestUserToken("test2@test.com");
			mockMvc.perform(
				delete(PREFIX + "/" + bungId)
					.header(AUTH_HEADER, notOwnerToken)
			).andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("401 Unauthorized. invalid Authorization Header")
		@Transactional
		void deleteBung_isUnauthorized() throws Exception {
			String invalidToken = "Bearer invalidToken";
			mockMvc.perform(
				delete(PREFIX + "/" + bungId)
					.header(AUTH_HEADER, invalidToken)
			).andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("401 Unauthorized. Unknown user token")
		@Transactional
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
				get(PREFIX + "/" + bungId + "/join")
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
    @DisplayName("벙 완료하기")
    class completeBung {

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
