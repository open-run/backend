-- h2 data sql sheet for test implementation, ** must be executed in order of foreign key constraints **
INSERT INTO tb_users (user_id, withdraw, nickname, email, identity_authenticated, provider, blacklisted, created_date, last_login_date, blockchain_address, running_pace, running_frequency)
VALUES
('1', FALSE, 'test', 'test1@test.com', FALSE, 'naver', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0x', NULL, NULL),
('2', FALSE, 'test2', 'test2@test.com', FALSE, 'naver', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0x', NULL, NULL),
('3', FALSE, 'test3', 'test3@test.com', FALSE, 'naver', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0x', NULL, NULL)
;

INSERT INTO tb_withdraws (user_id, deferment_period, created_date)
VALUES
    ( '1', '15', CURRENT_TIMESTAMP )
;

INSERT INTO tb_bung (bung_id, location, datetime, bung_name, start_time, end_time, distance, pace, participant_number, has_after_run, note)
VALUES
    ('1', 'seoul', CURRENT_TIMESTAMP, 'temp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, '5', 2, false, 'lalalala')
;

INSERT INTO tb_users_bung (user_bung_id, bung_id, user_id)
VALUES
    ( 1, '1', '1' )
;