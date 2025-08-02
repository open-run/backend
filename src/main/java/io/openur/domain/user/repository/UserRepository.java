package io.openur.domain.user.repository;

import io.openur.domain.user.model.User;
import java.util.List;

public interface UserRepository {
    // QUESTION: What is the purpose of this interface repository?
    // Answer: 확장성 고려

    User save(User user);

    User findUser(User user); 

    User findById(String userId);

    List<User> findByUserNickname(String nickName);

    boolean existsByNickname(String nickname);

    void update(User user);

    void deleteUserInfo(User user);

    List<String> batchIncrementFeedback(List<String> targetUserIds);
}
