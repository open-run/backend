package io.openur.domain.user.entity;


import io.openur.domain.user.model.Provider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Getter
@Setter
@Table(name = "tb_users")
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

    private String nickname;

    private Boolean identityAuthenticated;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private Boolean blacklisted;

    @CreatedDate
    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime lastLoginDate;

    // TODO: blockchain address 생성 및 기본 아바타 추가 기능
    private String blockchainAddress;

    private String profileImageStorageKey;

    private String runningPace;

    private Integer runningFrequency;

    private Integer feedback;

    public UserEntity(
        String userId,
        String nickname,
        Boolean identityAuthenticated,
        Provider provider,
        Boolean blacklisted,
        LocalDateTime createdDate,
        LocalDateTime lastLoginDate,
        String blockchainAddress,
        String runningPace,
        Integer runningFrequency,
        Integer feedback
    ) {
        this(
            userId,
            nickname,
            identityAuthenticated,
            provider,
            blacklisted,
            createdDate,
            lastLoginDate,
            blockchainAddress,
            null,
            runningPace,
            runningFrequency,
            feedback
        );
    }
}
