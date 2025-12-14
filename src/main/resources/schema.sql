-- tb_users 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_users
(
    user_id                VARCHAR(36)                      DEFAULT (UUID()) PRIMARY KEY NOT NULL,
    nickname               VARCHAR(16)                      DEFAULT NULL,
    identity_authenticated BOOLEAN                          DEFAULT FALSE,
    provider               ENUM ('smart_wallet')            NOT NULL,
    blacklisted            BOOLEAN                          DEFAULT FALSE,
    created_date           TIMESTAMP                        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_date        TIMESTAMP                        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    blockchain_address     VARCHAR(42)                      NOT NULL DEFAULT '0x',
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
    latitude              DECIMAL(10, 6)                             NOT NULL,
    longitude             DECIMAL(11, 6)                             NOT NULL,
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
    participation_status BOOLEAN     DEFAULT FALSE             NOT NULL,
    modified_at          TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_owner             BOOLEAN     DEFAULT FALSE             NOT NULL,
    CONSTRAINT tb_users_bungs_tb_bungs_bung_id_fk
        FOREIGN KEY (bung_id) REFERENCES tb_bungs (bung_id),
    CONSTRAINT tb_users_bungs_tb_users_user_id_fk
        FOREIGN KEY (user_id) REFERENCES tb_users (user_id)
);

-- tb_challenges 테이블 생성
CREATE TABLE IF NOT EXISTS tb_challenges
(
    challenge_id      BIGINT AUTO_INCREMENT
        PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    description       VARCHAR(255) NOT NULL,
    challenge_type    VARCHAR(30)  NOT NULL,
    reward_type       VARCHAR(30)  NOT NULL,
    reward_percentage FLOAT        NOT NULL,
    completed_type    VARCHAR(30)  NOT NULL,
    condition_date    DATETIME     NULL,
    condition_text    VARCHAR(100) NULL
);

-- tb_challenge_stages 테이블 생성 (✅ 복수형으로 수정)
CREATE TABLE IF NOT EXISTS tb_challenge_stages
(
    stage_id        BIGINT AUTO_INCREMENT
        PRIMARY KEY,
    stage_number    INT           NOT NULL,
    condition_count INT DEFAULT 1 NOT NULL,
    challenge_id    BIGINT        NOT NULL,
    CONSTRAINT tb_challenge_stages_challenge_id_fk
        FOREIGN KEY (challenge_id) REFERENCES tb_challenges (challenge_id)
);

-- tb_users_challenges 테이블 생성
CREATE TABLE IF NOT EXISTS tb_users_challenges
(
    user_challenge_id  BIGINT AUTO_INCREMENT
        PRIMARY KEY,
    user_id            VARCHAR(36) DEFAULT (UUID()) NOT NULL,
    challenge_stage_id BIGINT                       NOT NULL,
    current_count      BIGINT      DEFAULT 0        NOT NULL,
    current_progress   FLOAT       DEFAULT 0.0      NOT NULL,
    nft_completed      TINYINT(1)  DEFAULT 0        NOT NULL,
    completed_date     TIMESTAMP                    NULL,
    CONSTRAINT tb_users_challenges_tb_challenge_stages_stage_id_fk
        FOREIGN KEY (challenge_stage_id) REFERENCES tb_challenge_stages (stage_id),
    CONSTRAINT tb_users_challenges_tb_users_user_id_fk
        FOREIGN KEY (user_id) REFERENCES tb_users (user_id)
);

-- tb_hashtags 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_hashtags
(
    hashtag_id  BIGINT(36) AUTO_INCREMENT PRIMARY KEY NOT NULL,
    hashtag_str VARCHAR(36) UNIQUE                    NOT NULL
);

-- tb_bungs_hashtags 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_bungs_hashtags
(
    bung_hashtag_id BIGINT(20) AUTO_INCREMENT PRIMARY KEY NOT NULL,
    bung_id         VARCHAR(36) DEFAULT (UUID())          NOT NULL,
    hashtag_id      BIGINT(36)                            NOT NULL,
    CONSTRAINT tb_bungs_hashtags_tb_bungs_bung_id_fk
        FOREIGN KEY (bung_id) REFERENCES tb_bungs (bung_id),
    CONSTRAINT tb_bungs_hashtags_tb_hashtags_hashtag_id_fk
        FOREIGN KEY (hashtag_id) REFERENCES tb_hashtags (hashtag_id)
);
