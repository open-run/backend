-- tb_users 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_users
(
    user_id                VARCHAR(36)                      DEFAULT (UUID()) PRIMARY KEY NOT NULL,
    nickname               VARCHAR(16)                      DEFAULT NULL,
    email                  VARCHAR(255)                     DEFAULT NULL,
    identity_authenticated BOOLEAN                          DEFAULT FALSE,
    provider               ENUM ('naver', 'kakao', 'smart_wallet') NOT NULL,
    blacklisted            BOOLEAN                          DEFAULT FALSE,
    created_date           TIMESTAMP               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_date        TIMESTAMP               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    blockchain_address     VARCHAR(42)             NOT NULL DEFAULT '0x',
    running_pace           VARCHAR(8)                       DEFAULT NULL,
    running_frequency      SMALLINT(4)                      DEFAULT NULL,
    feedback               INT(4) UNSIGNED                  DEFAULT 0 NOT NULL
);

-- tb_bungs 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_bungs
(
    bung_id               VARCHAR(36)   DEFAULT (UUID()) PRIMARY KEY NOT NULL,
    name                  VARCHAR(192)                               NOT NULL DEFAULT '',
    description           VARCHAR(255),
    location              VARCHAR(128)                               NOT NULL DEFAULT '',
    latitude              DECIMAL(10, 6)                           NOT NULL,
    longitude             DECIMAL(11, 6)                           NOT NULL,
    start_datetime        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    NOT NULL,
    end_datetime          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP    NOT NULL,
    distance              SMALLINT                                   NOT NULL DEFAULT 0,
    pace                  VARCHAR(8)                                 NOT NULL,
    member_number         SMALLINT                                   NOT NULL,
    current_member_number SMALLINT                                   NOT NULL DEFAULT 1,
    has_after_run         BOOLEAN       DEFAULT FALSE,
    after_run_description VARCHAR(4096) DEFAULT NULL,
    is_completed          BOOLEAN       DEFAULT FALSE                NOT NULL,
    main_image            VARCHAR(255)  DEFAULT NULL
);

-- tb_users_bungs 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_users_bungs
(
    user_bung_id         BIGINT(20) AUTO_INCREMENT PRIMARY KEY NOT NULL,
    bung_id              VARCHAR(36) DEFAULT (UUID())          NOT NULL,
    user_id              VARCHAR(36) DEFAULT (UUID())          NOT NULL,
    FOREIGN KEY (bung_id) REFERENCES tb_bungs (bung_id),
    FOREIGN KEY (user_id) REFERENCES tb_users (user_id),
    participation_status BOOLEAN     DEFAULT FALSE             NOT NULL, -- TODO: 벙 참여 인증 완료 했을 때 사용할 값
    modified_at          TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_owner             BOOLEAN     DEFAULT FALSE             NOT NULL
);

-- tb_challenges 테이블 생성
CREATE TABLE IF NOT EXISTS tb_challenges
(
    challenge_id BIGINT(20) AUTO_INCREMENT PRIMARY KEY NOT NULL,
    name         VARCHAR(255)                          NOT NULL,
    description  VARCHAR(255)                          NOT NULL,
    challenge_type VARCHAR(30)                         NOT NULL,
    reward_type VARCHAR(30)                            NOT NULL,
    reward_percentage FLOAT4                           NOT NULL,
    completed_type VARCHAR(30)                         NOT NULL,
    condition_count BIGINT(20)                             NULL,
    condition_date TIMESTAMP                               NULL,
    condition_text VARCHAR(50)                             NULL
);

-- tb_users_challenges 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_users_challenges
(
    user_challenge_id BIGINT(20) AUTO_INCREMENT PRIMARY KEY NOT NULL,
    user_id           VARCHAR(36) DEFAULT (UUID())          NOT NULL,
    FOREIGN KEY (user_id) REFERENCES tb_users (user_id),
    challenge_id      BIGINT(20)                            NOT NULL,
    FOREIGN KEY (challenge_id) REFERENCES tb_challenges (challenge_id),
    completed_date    TIMESTAMP   DEFAULT NULL,
    nft_completed     BOOLEAN     DEFAULT FALSE             NOT NULL,
-- TODO: 유저가 받은 nft 정보 추가
    current_count     BIGINT(20)                            NULL,
    FOREIGN KEY (user_id) REFERENCES tb_users (user_id),
    FOREIGN KEY (challenge_id) REFERENCES tb_challenges (challenge_id)
);

-- tb_hashtags 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_hashtags
(
    hashtag_id  BIGINT(36) AUTO_INCREMENT PRIMARY KEY NOT NULL,
    hashtag_str VARCHAR(36) UNIQUE                    NOT NULL
);

-- tb_bung_hashtags 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_bungs_hashtags
(
    bung_hashtag_id BIGINT(20) AUTO_INCREMENT PRIMARY KEY NOT NULL,
    bung_id         VARCHAR(36) DEFAULT (UUID())          NOT NULL,
    hashtag_id      BIGINT(36)                            NOT NULL,
    FOREIGN KEY (bung_id) REFERENCES tb_bungs (bung_id),
    FOREIGN KEY (hashtag_id) REFERENCES tb_hashtags (hashtag_id)
);

