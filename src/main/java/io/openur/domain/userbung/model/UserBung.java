package io.openur.domain.userbung.model;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.user.model.User;
import io.openur.domain.userbung.entity.UserBungEntity;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UserBung {
    private Long userBungId;
    private User user;
    private Bung bung;
    private boolean participationStatus;
    private LocalDateTime modifiedAt;

    public UserBung(User user, Bung bung) {
        this.user = user;
        this.bung = bung;
        this.participationStatus = true;
        this.modifiedAt = LocalDateTime.now();
    }

    public static UserBung from(UserBungEntity userBungEntity) {
        return new UserBung(
            userBungEntity.getUserBungId(),
            User.from(userBungEntity.getUserEntity()),
            Bung.from(userBungEntity.getBungEntity()),
            userBungEntity.isParticipationStatus(),
            userBungEntity.getModifiedAt()
        );
    }

    public UserBungEntity toEntity() {
        return new UserBungEntity(
            this.userBungId,
            this.bung.toEntity(),
            this.user.toEntity(),
            this.participationStatus,
            this.modifiedAt
        );
    }
}
