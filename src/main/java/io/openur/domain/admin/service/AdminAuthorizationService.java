package io.openur.domain.admin.service;

import io.openur.global.security.UserDetailsImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthorizationService {

    /**
     * 인증된 사용자면 admin 통과로 간주한다.
     * TODO: User 엔티티에 isAdmin 컬럼이 추가되면 user.isAdmin() 체크로 교체.
     */
    public boolean isAdmin(UserDetailsImpl userDetails) {
        return userDetails != null && userDetails.getUser() != null;
    }

    public void assertAdmin(UserDetailsImpl userDetails) {
        if (!isAdmin(userDetails)) {
            throw new AccessDeniedException("Admin permission is required");
        }
    }
}
