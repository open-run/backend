package io.openur.domain.userbung.entity;

import io.openur.domain.bung.entity.BungEntity;
import io.openur.domain.user.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Entity
@Table(name = "tb_users_bungs")
@NoArgsConstructor
@AllArgsConstructor
public class UserBungEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userBungId;

    @ManyToOne(targetEntity = BungEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "bung_id")
    private BungEntity bungEntity;

    @ManyToOne(targetEntity = UserEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @Column(name = "participation_status")
    private boolean participationStatus;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime modifiedAt;

    private boolean isOwner;
}
