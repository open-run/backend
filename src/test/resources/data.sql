-- h2 data sql sheet for test implementation, ** must be executed in order of foreign key constraints **
INSERT INTO tb_users (user_id, withdraw, nickname, email, identity_authenticated, provider, blacklisted, created_date, last_login_date, blockchain_address, running_pace, running_frequency)
VALUES
    ('1', FALSE, 'test', 'test1@test.com', FALSE, 'naver', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0x', NULL, NULL),
    ('2', FALSE, 'test2', 'test2@test.com', FALSE, 'naver', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0x', NULL, NULL),
    ('3', FALSE, 'test3', 'test3@test.com', FALSE, 'naver', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '0x', NULL, NULL);
;
