-- tb_users 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_users (
                                        user_id VARCHAR(36) DEFAULT (UUID()) PRIMARY KEY NOT NULL,
                                        withdraw BOOLEAN DEFAULT FALSE,
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

-- tb_withdraws 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_withdraws (
                                            user_id VARCHAR(36) PRIMARY KEY NOT NULL,
                                            deferment_period ENUM('15', '30', '60') NOT NULL DEFAULT '15',
                                            created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            FOREIGN KEY (user_id) REFERENCES tb_users(user_id)
);

-- tb_bung 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_bungs (
                                       bung_id VARCHAR(36) DEFAULT (UUID()) PRIMARY KEY NOT NULL,
                                       name VARCHAR(192) NOT NULL DEFAULT '',
                                       description VARCHAR(255) Not NULL,
                                       location VARCHAR(128) NOT NULL DEFAULT '',
                                       start_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                       end_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                       distance SMALLINT NOT NULL DEFAULT 0,
                                       pace VARCHAR(8) NOT NULL,
                                       member_number SMALLINT NOT NULL,
                                       has_after_run BOOLEAN DEFAULT FALSE,
                                       after_run_description VARCHAR(4096) Not NULL
);

-- tb_users_bung 테이블 생성 (존재하지 않을 경우에만)
CREATE TABLE IF NOT EXISTS tb_users_bungs (
                                             user_bung_id BIGINT(20) AUTO_INCREMENT PRIMARY KEY NOT NULL,
                                             bung_id VARCHAR(36) DEFAULT (UUID()) NOT NULL,
                                             user_id VARCHAR(36) DEFAULT (UUID()) NOT NULL,
                                             FOREIGN KEY (bung_id) REFERENCES tb_bungs(bung_id),
                                             FOREIGN KEY (user_id) REFERENCES tb_users(user_id)
);
