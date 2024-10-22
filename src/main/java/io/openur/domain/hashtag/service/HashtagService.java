package io.openur.domain.hashtag.service;

import io.openur.domain.hashtag.model.Hashtag;
import io.openur.domain.hashtag.repository.HashtagRepositoryImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HashtagService {

	private final HashtagRepositoryImpl hashtagRepository;

	public List<String> getHashtagList(String substring) {
		return hashtagRepository.findByHashtagStrContaining(substring)
			.stream()
			.map(Hashtag::getHashtagStr)
			.toList();
	}
}
