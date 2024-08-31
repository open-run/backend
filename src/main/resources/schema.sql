-- tb_users 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_users (
                                        user_id VARCHAR(36) DEFAULT (UUID()) PRIMARY KEY NOT NULL,
                                        nickname VARCHAR(16) DEFAULT NULL,
                                        email VARCHAR(255) NOT NULL,
                                        identity_authenticated BOOLEAN DEFAULT FALSE,
                                        provider ENUM('naver', 'kakao') NOT NULL,
                                        blacklisted BOOLEAN DEFAULT FALSE,
                                        created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        last_login_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        blockchain_address VARCHAR(42) NOT NULL DEFAULT '0x',
                                        running_pace VARCHAR(8) DEFAULT NULL,
                                        running_frequency SMALLINT(4) DEFAULT NULL
);

-- tb_bungs 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_bungs (
                                       bung_id VARCHAR(36) DEFAULT (UUID()) PRIMARY KEY NOT NULL,
                                       name VARCHAR(192) NOT NULL DEFAULT '',
                                       description VARCHAR(255),
                                       location VARCHAR(128) NOT NULL DEFAULT '',
                                       start_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                       end_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                       distance SMALLINT NOT NULL DEFAULT 0,
                                       pace VARCHAR(8) NOT NULL,
                                       member_number SMALLINT NOT NULL,
                                       has_after_run BOOLEAN DEFAULT FALSE,
                                       after_run_description VARCHAR(4096) DEFAULT NULL
);

-- tb_users_bungs 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_users_bungs (
                                             user_bung_id BIGINT(20) AUTO_INCREMENT PRIMARY KEY NOT NULL,
                                             bung_id VARCHAR(36) DEFAULT (UUID()) NOT NULL,
                                             user_id VARCHAR(36) DEFAULT (UUID()) NOT NULL,
                                             FOREIGN KEY (bung_id) REFERENCES tb_bungs (bung_id), -- TODO: CASCADE 설정
                                             FOREIGN KEY (user_id) REFERENCES tb_users(user_id),
                                             participation_status BOOLEAN NOT NULL,
                                             modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                             is_owner    BOOLEAN   DEFAULT FALSE NOT NULL
);

-- tb_hashtags 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_hashtags(
                                            hashtag_id  BIGINT(36) AUTO_INCREMENT PRIMARY KEY NOT NULL,
                                            hashtag_str VARCHAR(36) UNIQUE NOT NULL
);

-- tb_hashtags 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_bungs_hashtags (
                                              bung_hashtag_id BIGINT(20) AUTO_INCREMENT PRIMARY KEY NOT NULL,
                                              bung_id VARCHAR(36) DEFAULT (UUID()) NOT NULL,
                                              hashtag_id BIGINT(36) NOT NULL,
                                              FOREIGN KEY (bung_id) REFERENCES tb_bungs(bung_id),
                                              FOREIGN KEY (hashtag_id) REFERENCES tb_hashtags(hashtag_id)
);

-- tb_nft_indexer 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_nft_index
(
    nft_index VARCHAR(64) PRIMARY KEY      NOT NULL,
    user_id   VARCHAR(36) DEFAULT (UUID()) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES tb_users (user_id),
    is_new    BOOLEAN     DEFAULT FALSE    NOT NULL,
    is_active BOOLEAN     DEFAULT FALSE    NOT NULL
);
