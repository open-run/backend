-- h2 data sql sheet for test implementation, ** must be executed in order of foreign key constraints **
INSERT INTO tb_users (user_id, nickname, email, identity_authenticated, provider, blacklisted,
                      created_date, last_login_date, blockchain_address, running_pace,
                      running_frequency)
VALUES ('9e1bfc60-f76a-47dc-9147-803653707192', 'test', 'test1@test.com', FALSE, 'naver', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        '0x', NULL, NULL),
       ('91b4928f-8288-44dc-a04d-640911f0b2be', 'test2', 'test2@test.com', FALSE, 'naver', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        '0x', NULL, NULL),
       ('5d22bd65-f1ed-4e7b-bc7b-0a59580d3176', 'test3', 'test3@test.com', FALSE, 'naver', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        '0x', NULL, NULL)
;

INSERT INTO tb_bungs (bung_id, name, location, description, start_datetime, end_datetime, distance, pace, member_number, has_after_run, after_run_description)
VALUES
    ('c0477004-1632-455f-acc9-04584b55921f', 'test1_bung', 'seoul', 'temp_description', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, '5"55', 2, false, 'lalalala')
;

INSERT INTO tb_users_bungs (user_bung_id, bung_id, user_id, participation_status, modified_at,
                            is_owner)
VALUES (1, 'c0477004-1632-455f-acc9-04584b55921f', '9e1bfc60-f76a-47dc-9147-803653707192', true,
        CURRENT_TIMESTAMP(), true),
       (2, 'c0477004-1632-455f-acc9-04584b55921f', '91b4928f-8288-44dc-a04d-640911f0b2be', true,
        CURRENT_TIMESTAMP(), false)
;

INSERT INTO tb_hashtags (hashtag_id, hashtag_str)
VALUES (1, '펀런'),
       (2, '런린이'),
       (3, '밤산책'),
       (4, 'LSD'),
       (5, '고수만')
;

INSERT INTO tb_bungs_hashtags (bung_hashtag_id, bung_id, hashtag_id)
VALUES (1, 'c0477004-1632-455f-acc9-04584b55921f', 1),
       (2, 'c0477004-1632-455f-acc9-04584b55921f', 2),
       (3, 'c0477004-1632-455f-acc9-04584b55921f', 3)
;
