package io.openur.domain.userbung.model;

import io.openur.domain.bung.model.Bung;
import io.openur.domain.user.model.User;
import io.openur.domain.userbung.entity.UserBungEntity;
import java.time.LocalDateTime;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class UserBung {

    private Long userBungId;
    private User user;
    private Bung bung;
    @Setter
    private boolean participationStatus;
    private LocalDateTime modifiedAt;
    private boolean isOwner;

    public UserBung(User user, Bung bung) {
        this.user = user;
        this.bung = bung;
        this.participationStatus = false;
        this.modifiedAt = LocalDateTime.now();
        this.isOwner = false;
    }

    public static UserBung isOwnerBung(User user, Bung bung) {
        UserBung userBung = new UserBung(user, bung);
        userBung.isOwner = true;
        return userBung;
    }

    public static UserBung from(UserBungEntity userBungEntity) {
        return new UserBung(
            userBungEntity.getUserBungId(),
            User.from(userBungEntity.getUserEntity()),
            Bung.from(userBungEntity.getBungEntity()),
            userBungEntity.isParticipationStatus(),
            userBungEntity.getModifiedAt(),
            userBungEntity.isOwner()
        );
    }

    public void disableOwnerBung() {
        this.isOwner = false;
    }

    public void enableOwnerBung() {
        this.isOwner = true;
    }

    public UserBungEntity toEntity() {
        return new UserBungEntity(
            this.userBungId,
            this.bung.toEntity(Collections.emptyList()),
            this.user.toEntity(),
            this.participationStatus,
            this.modifiedAt,
            this.isOwner
        );
    }

}
