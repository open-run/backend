-- Create ENUM type for provider if not exists
DO
'BEGIN
  IF (NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = ''provider_enum''))
  THEN
    CREATE TYPE provider_enum AS ENUM (''GOOGLE'', ''NAVER'', ''KAKAO'');
  END IF;
END;
';

-- Create table TB_USERS if not exists
CREATE TABLE IF NOT EXISTS tb_users (
                                      user_id BIGINT PRIMARY KEY NOT NULL,
                                      withdraw BOOLEAN NOT NULL DEFAULT FALSE,
                                      nickname VARCHAR(16) NOT NULL,
                                      email VARCHAR(255) NOT NULL CHECK (email ~ '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$'),
                                      identity_authenticated BOOLEAN NOT NULL DEFAULT FALSE,
                                      provider provider_enum NOT NULL,
                                      blacklisted BOOLEAN NOT NULL DEFAULT FALSE,
                                      created_date TIMESTAMP NOT NULL,
                                      last_login_date TIMESTAMP NOT NULL,
                                      blockchain_address VARCHAR(42) NOT NULL
);

-- Create table TB_WITHDRAWS if not exists
CREATE TABLE IF NOT EXISTS tb_withdraws (
                                          user_id BIGINT PRIMARY KEY NOT NULL,
                                          deferment_period INTEGER CHECK (deferment_period IN (15, 30, 60)),
                                          created_date TIMESTAMP NOT NULL,
                                          CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES tb_users(user_id)
);

-- Create table TB_BUNG if not exists
CREATE TABLE IF NOT EXISTS tb_bung (
                                     bung_id UUID DEFAULT gen_random_uuid() PRIMARY KEY NOT NULL,
                                     location VARCHAR(128) NOT NULL DEFAULT '',
                                     datetime TIMESTAMP DEFAULT '1970-01-01 00:00:00' NOT NULL,
                                     bung_name VARCHAR(192) NOT NULL DEFAULT '',
                                     start_time TIMESTAMP DEFAULT '1970-01-01 00:00:00' NOT NULL,
                                     end_time TIMESTAMP DEFAULT '1970-01-01 00:00:00' NOT NULL,
                                     distance SMALLINT NOT NULL DEFAULT 0,
                                     pace VARCHAR(8) NOT NULL,
                                     participant_number SMALLINT NOT NULL,
                                     has_after_run BOOLEAN NOT NULL DEFAULT FALSE,
                                     note VARCHAR(4096) DEFAULT '' NULL
);

-- Create table TB_USERS_BUNG if not exists
CREATE TABLE IF NOT EXISTS tb_users_bung (
                                           user_bung_id SERIAL PRIMARY KEY NOT NULL,
                                           bung_id UUID NOT NULL,
                                           user_id BIGINT NOT NULL,
                                           FOREIGN KEY (bung_id) REFERENCES tb_bung(bung_id),
                                           FOREIGN KEY (user_id) REFERENCES tb_users(user_id),
                                           CONSTRAINT fk_bung_id FOREIGN KEY (bung_id) REFERENCES tb_bung(bung_id),
                                           CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES tb_users(user_id)
);
