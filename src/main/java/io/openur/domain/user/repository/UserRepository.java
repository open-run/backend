package io.openur.domain.user.repository;


import io.openur.domain.user.model.User;


public interface UserRepository {
    // QUESTION: What is the purpose of this interface repository?
    // Answer: 확장성 고려

    User save(User user);

    User findByEmail(String email);

    User findById(String userId);

    boolean existsByNickname(String nickname);

    void update(User user);

    void deleteUserInfo(User user);
}
