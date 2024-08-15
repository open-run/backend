package io.openur.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.openur.config.TestSupport;
import io.openur.domain.userbung.entity.UserBungEntity;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class UserBungApiTest extends TestSupport {

	private static final String PREFIX = "/v1/bungs";

	@Test
	@DisplayName("멤버 제거")
	void kickMember_successTest() throws Exception {
		String token = getTestUserToken("test1@test.com");
		String bungId = "c0477004-1632-455f-acc9-04584b55921f";
		String userIdToKick = "91b4928f-8288-44dc-a04d-640911f0b2be";

		mockMvc.perform(
			delete(PREFIX + "/{bungId}/members/{userIdToKick}", bungId, userIdToKick)
				.header(AUTH_HEADER, token)
				.contentType(MediaType.APPLICATION_JSON)
		).andExpect(status().isAccepted());

		Optional<UserBungEntity> kickedUserBung = userBungJpaRepository
			.findByUserEntity_UserIdAndBungEntity_BungId(userIdToKick, bungId);
		assertThat(kickedUserBung).isEmpty();
	}

	@Nested
	@DisplayName("벙주 변경")
	class changeOwnerTest {

		@Test
		@DisplayName("200 Ok.")
		@Transactional
		void changeOwner_isOkTest() throws Exception {
			String token = getTestUserToken("test1@test.com");

			String bungId = "c0477004-1632-455f-acc9-04584b55921f";
			String newOwnerUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";
			String oldOwnerUserId = "9e1bfc60-f76a-47dc-9147-803653707192";

			mockMvc.perform(
				patch(PREFIX + "/{bungId}/change-owner?newOwnerUserId={newOwnerUserId}", bungId,
					newOwnerUserId)
					.header(AUTH_HEADER, token)
					.contentType(MediaType.APPLICATION_JSON)
			).andExpect(status().isOk());

			Optional<UserBungEntity> newOwnerBung = userBungJpaRepository
				.findByUserEntity_UserIdAndBungEntity_BungId(newOwnerUserId, bungId);
			assertThat(newOwnerBung).isPresent();
			assertThat(newOwnerBung.get().isOwner()).isTrue();

			Optional<UserBungEntity> oldOwnerBung = userBungJpaRepository
				.findByUserEntity_UserIdAndBungEntity_BungId(oldOwnerUserId, bungId);
			assertThat(oldOwnerBung).isPresent();
			assertThat(oldOwnerBung.get().isOwner()).isFalse();
		}

		@Test
		@DisplayName("403 Forbidden. Authorization Header 없음")
		@Transactional
		void changeOwner_isForbidden() throws Exception {
			String bungId = "c0477004-1632-455f-acc9-04584b55921f";
			String newOwnerUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";

			mockMvc.perform(
				patch(PREFIX + "/{bungId}/change-owner?newOwnerUserId={newOwnerUserId}", bungId,
					newOwnerUserId)
					.contentType(MediaType.APPLICATION_JSON)
			).andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("403 Forbidden. Bung owner 가 아닌 경우")
		@Transactional
		void changeOwner_isForbidden_notOwner() throws Exception {
			String notOwnerToken = getTestUserToken("test2@test.com");

			String bungId = "c0477004-1632-455f-acc9-04584b55921f";
			String newOwnerUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";

			mockMvc.perform(
				patch(PREFIX + "/{bungId}/change-owner?newOwnerUserId={newOwnerUserId}", bungId,
					newOwnerUserId)
					.header(AUTH_HEADER, notOwnerToken)
					.contentType(MediaType.APPLICATION_JSON)
			).andExpect(status().isForbidden());
		}

		@Test
		@DisplayName("401 Unauthorized. invalid Authorization Header")
		@Transactional
		void changeOwner_isUnauthorized() throws Exception {
			String invalidToken = "Bearer invalidToken";

			String bungId = "c0477004-1632-455f-acc9-04584b55921f";
			String newOwnerUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";

			mockMvc.perform(
				patch(PREFIX + "/{bungId}/change-owner?newOwnerUserId={newOwnerUserId}", bungId,
					newOwnerUserId)
					.header(AUTH_HEADER, invalidToken)
					.contentType(MediaType.APPLICATION_JSON)
			).andExpect(status().isUnauthorized());
		}

		@Test
		@DisplayName("401 Unauthorized. Unknown user token")
		@Transactional
		void changeOwner_isUnauthorized_unknownUser() throws Exception {
			String unknownUserToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ5ZWppbmtlbGx5am9vQGdtYWlsLmNvbSIsImV4cCI6MTcyMzYyNDgxMCwiaWF0IjoxNzIzNjIxMjEwfQ.wH-eJCvEBgFg_QjWr7CdxBpMqlQzGt45DLmrsWju-HU";

			String bungId = "c0477004-1632-455f-acc9-04584b55921f";
			String newOwnerUserId = "91b4928f-8288-44dc-a04d-640911f0b2be";

			mockMvc.perform(
				patch(PREFIX + "/{bungId}/change-owner?newOwnerUserId={newOwnerUserId}", bungId,
					newOwnerUserId)
					.header(AUTH_HEADER, unknownUserToken)
					.contentType(MediaType.APPLICATION_JSON)
			).andExpect(status().isUnauthorized());
		}
	}
}
