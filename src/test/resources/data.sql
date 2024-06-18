-- h2 data sql sheet for test implementation, ** must be executed in order of foreign key constraints **
INSERT INTO tb_users (user_id, nickname, email, identity_authenticated, provider, blacklisted,
                      created_date, last_login_date, blockchain_address, running_pace,
                      running_frequency)
VALUES ('1', 'test', 'test1@test.com', FALSE, 'naver', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        '0x', NULL, NULL),
       ('2', 'test2', 'test2@test.com', FALSE, 'naver', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        '0x', NULL, NULL),
       ('3', 'test3', 'test3@test.com', FALSE, 'naver', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        '0x', NULL, NULL)
;

INSERT INTO tb_bungs (bung_id, location, datetime, bung_name, start_time, end_time, distance, pace, participant_number, has_after_run, note)
VALUES
    ('1', 'seoul', CURRENT_TIMESTAMP, 'temp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, '5', 2, false, 'lalalala')
;

INSERT INTO tb_users_bungs (user_bung_id, bung_id, user_id)
VALUES
    ( 1, '1', '1' )
;
