package io.openur.domain.user.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "TB_USERS")
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private Boolean withdraw;
    private String nickname;
    private String email;
    private Boolean identityAuthenticated;
    private String provider;
    private Boolean blackListed;
    private Timestamp createdDate;
    private Timestamp lastLoginDate;
    private String blockchainAddress;
}
