-- h2 data sql sheet for test implementation, ** must be executed in order of foreign key constraints **
INSERT INTO tb_users (user_id, nickname, identity_authenticated, provider, blacklisted,
                      created_date, last_login_date, blockchain_address, running_pace,
                      running_frequency)
VALUES ('9e1bfc60-f76a-47dc-9147-803653707192', 'test', FALSE, 'smart_wallet', FALSE, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '0x1234567890123456789012345678901234567890', NULL, NULL),
       ('91b4928f-8288-44dc-a04d-640911f0b2be', 'test2', FALSE, 'smart_wallet', FALSE, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '0x1234567890123456789012345678901234567891', NULL, NULL),
       ('5d22bd65-f1ed-4e7b-bc7b-0a59580d3176', 'test3', FALSE, 'smart_wallet', FALSE, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '0x1234567890123456789012345678901234567892', NULL, NULL)
;

INSERT INTO tb_bungs (bung_id, name, location, description, start_datetime, end_datetime, distance, pace, member_number,
                      has_after_run, after_run_description, is_completed, main_image)
VALUES ('c0477004-1632-455f-acc9-04584b55921f', 'test1_bung', 'Seoul', 'temp_description',
        CURRENT_TIMESTAMP + 1, CURRENT_TIMESTAMP + 2, 3, '5"55', 2, false, null, false, 'image1.png'),
       ('90477004-1422-4551-acce-04584b34612e', 'test2_bung', 'Bangkok', 'second_bung_data',
        CURRENT_TIMESTAMP + 3, CURRENT_TIMESTAMP + 4, 1, '6"30', 2, true, 'chicken & beer', false, 'image2.png'),
       ('a1234567-89ab-cdef-0123-456789abcdef', 'past_bung_incompleted', 'New York', 'past_bung_description',
        CURRENT_TIMESTAMP - 2, CURRENT_TIMESTAMP - 1, 5, '7"00', 3, false, null, false, 'image3.png'),
       ('a1234567-89ab-cdef-0123-1982ey1kbjas', 'past_bung_completed', 'New York', 'past_bung_description',
        CURRENT_TIMESTAMP - 4, CURRENT_TIMESTAMP - 3, 5, '7"00', 3, false, null, true, 'image4.png')
;

INSERT INTO tb_users_bungs (user_bung_id, bung_id, user_id, participation_status, modified_at,
                            is_owner)
VALUES (1, 'c0477004-1632-455f-acc9-04584b55921f', '9e1bfc60-f76a-47dc-9147-803653707192', false,
        CURRENT_TIMESTAMP(), true),
       (2, 'c0477004-1632-455f-acc9-04584b55921f', '91b4928f-8288-44dc-a04d-640911f0b2be', false,
        CURRENT_TIMESTAMP(), false),
       (3, '90477004-1422-4551-acce-04584b34612e', '5d22bd65-f1ed-4e7b-bc7b-0a59580d3176', false,
        CURRENT_TIMESTAMP(), true),
       (4, 'a1234567-89ab-cdef-0123-456789abcdef', '9e1bfc60-f76a-47dc-9147-803653707192', false,
        CURRENT_TIMESTAMP(), false),
       (5, 'a1234567-89ab-cdef-0123-456789abcdef', '91b4928f-8288-44dc-a04d-640911f0b2be', false,
        CURRENT_TIMESTAMP(), true),
       (6, 'a1234567-89ab-cdef-0123-1982ey1kbjas', '5d22bd65-f1ed-4e7b-bc7b-0a59580d3176', false,
        CURRENT_TIMESTAMP(), true)
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
       (3, 'c0477004-1632-455f-acc9-04584b55921f', 3),
       (4, '90477004-1422-4551-acce-04584b34612e', 4),
       (5, '90477004-1422-4551-acce-04584b34612e', 5),
       (6, 'a1234567-89ab-cdef-0123-456789abcdef', 1),
       (7, 'a1234567-89ab-cdef-0123-456789abcdef', 2);
;

INSERT INTO tb_challenges (challenge_id, name, description, challenge_type,
                           reward_type, reward_percentage,completed_type,
                           condition_date, condition_text
)
VALUES
    (1, 'test_challenge', 'test_challenge_description', 'normal', 'face', 50, 'count', null, null),
    (2, 'test_challenge2', 'test_challenge2_description', 'repetitive', 'face', 50, 'count', null, null)
;

INSERT INTO tb_challenge_stages (stage_id, challenge_id, stage_number, condition_count)
VALUES
    (1, 1, 1, 1),
    (2, 2, 1, 1),
    (3, 2, 2, 3),
    (4, 2, 3, 5)
;

INSERT INTO tb_users_challenges (user_challenge_id, challenge_stage_id, user_id, completed_date, current_count, current_progress, nft_completed)
VALUES (1, 1, '9e1bfc60-f76a-47dc-9147-803653707192', CURRENT_TIMESTAMP, 1, 100.0, false),
       (2, 3, '9e1bfc60-f76a-47dc-9147-803653707192', CURRENT_TIMESTAMP, 2, 66.6, false);
