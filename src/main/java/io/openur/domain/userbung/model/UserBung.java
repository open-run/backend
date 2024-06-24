package io.openur.domain.userbung.model;

import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.bung.model.Bung;
import io.openur.domain.user.entity.UserEntity;
import io.openur.domain.user.model.User;
import io.openur.domain.userbung.entity.UserBungEntity;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UserBung {
    private Long userBungId;
    private UserEntity user;
    private BungEntity bung;
    private boolean participationStatus;
    private LocalDateTime modifiedAt;

    public UserBung(User user, Bung bung) {
        this.user = user.toEntity();
        this.bung = bung.toEntity();
        this.participationStatus = true;
        this.modifiedAt = LocalDateTime.now();
    }

    public static UserBung from(UserBungEntity userBungEntity) {
        return new UserBung(
            userBungEntity.getUserBungId(),
            userBungEntity.getUserEntity(),
            userBungEntity.getBungEntity(),
            userBungEntity.isParticipationStatus(),
            userBungEntity.getModifiedAt()
        );
    }

    public UserBungEntity toEntity() {
        return new UserBungEntity(
            this.userBungId,
            this.bung,
            this.user,
            this.participationStatus,
            this.modifiedAt
        );
    }
}
