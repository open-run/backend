-- h2 data sql sheet for test implementation, ** must be executed in order of foreign key constraints **
INSERT INTO tb_users (user_id, nickname, identity_authenticated, provider, blacklisted,
                      created_date, last_login_date, blockchain_address, running_pace,
                      profile_image_storage_key, running_frequency, feedback)
VALUES ('9e1bfc60-f76a-47dc-9147-803653707192', 'test', FALSE, 'smart_wallet', FALSE, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '0x1234567890123456789012345678901234567890', NULL, NULL, NULL, 0),
       ('91b4928f-8288-44dc-a04d-640911f0b2be', 'test2', FALSE, 'smart_wallet', FALSE, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '0x1234567890123456789012345678901234567891', NULL, NULL, NULL, 0),
       ('5d22bd65-f1ed-4e7b-bc7b-0a59580d3176', 'test3', FALSE, 'smart_wallet', FALSE, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '0x1234567890123456789012345678901234567892', NULL,
        'profile-images/users/5d22bd65-f1ed-4e7b-bc7b-0a59580d3176/profile.png', NULL, 0)
;

INSERT INTO tb_bungs (bung_id, name, description, location, latitude, longitude, start_datetime, end_datetime, distance, pace, member_number, current_member_number,
                      has_after_run, after_run_description, is_completed, is_faded, main_image)
VALUES ('c0477004-1632-455f-acc9-04584b55921f', 'test1_bung', 'temp_description', 'Seoul', 37.5665, 126.9780,
        CURRENT_TIMESTAMP + 1, CURRENT_TIMESTAMP + 2, 3, '5"55', 2, 2, false, null, false, false, 'image1.png'),
       ('90477004-1422-4551-acce-04584b34612e', 'test2_bung', 'second_bung_data', 'Bangkok', 13.7563, 100.5018,
        CURRENT_TIMESTAMP + 3, CURRENT_TIMESTAMP + 4, 1, '6"30', 2, 1, true, 'chicken & beer', false, false, 'image2.png'),
       ('a1234567-89ab-cdef-0123-456789abcdef', 'past_bung_incompleted', 'past_bung_description', 'New York', 40.7128, -74.0060,
        CURRENT_TIMESTAMP - 2, CURRENT_TIMESTAMP - 1, 5, '7"00', 3, 2, false, null, false, false, 'image3.png'),
       ('a1234567-89ab-cdef-0123-1982ey1kbjas', 'past_bung_completed', 'past_bung_description', 'New York', 40.7128, -74.0060,
        CURRENT_TIMESTAMP - 4, CURRENT_TIMESTAMP - 3, 5, '7"00', 3, 3, false, null, true, false, 'image4.png'),
       -- 벙 완료 도전과제 트리거 테스트용: 시작됐지만 미완료, user2(벙주, 인증됨) / user3(미인증)
       ('b1234567-89ab-cdef-0123-456789abcdef', 'trigger_complete_bung', 'challenge_trigger_bung', 'Busan', 35.1796, 129.0756,
        CURRENT_TIMESTAMP - 2, CURRENT_TIMESTAMP - 1, 5, '7"00', 3, 2, false, null, false, false, 'image5.png')
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
        CURRENT_TIMESTAMP(), true),
       (7, 'b1234567-89ab-cdef-0123-456789abcdef', '91b4928f-8288-44dc-a04d-640911f0b2be', true,
        CURRENT_TIMESTAMP(), true),
       (8, 'b1234567-89ab-cdef-0123-456789abcdef', '5d22bd65-f1ed-4e7b-bc7b-0a59580d3176', false,
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
       (3, 'c0477004-1632-455f-acc9-04584b55921f', 3),
       (4, '90477004-1422-4551-acce-04584b34612e', 4),
       (5, '90477004-1422-4551-acce-04584b34612e', 5),
       (6, 'a1234567-89ab-cdef-0123-456789abcdef', 1),
       (7, 'a1234567-89ab-cdef-0123-456789abcdef', 2);
;

INSERT INTO tb_challenges (challenge_id, name, description, challenge_type,
                           reward_type, completed_type,
                           condition_date, condition_text
)
VALUES
    (1, 'test_challenge', 'test_challenge_description', 'normal', 'footwear', 'count', null, null),
    (2, 'test_challenge2', 'test_challenge2_description', 'repetitive', 'footwear', 'count', null, null),
    -- 활동 트리거 매핑(ChallengeActivityType)과 맞춘 시드:
    -- BUNG_CREATE→[2,6], BUNG_JOIN→[3,5], BUNG_CERTIFY→[4], BUNG_COMPLETE→[19,7]
    (3, '벙 참여하기', '벙 참여하기 반복 도전과제', 'repetitive', 'footwear', 'count', null, null),
    (4, '벙 참여 인증하기', '벙 참여 인증하기 반복 도전과제', 'repetitive', 'footwear', 'count', null, null),
    (5, '첫 걸음', '첫 벙 참여 도전과제', 'normal', 'footwear', 'count', null, null),
    (6, '러너의 시작', '첫 벙 생성 도전과제', 'normal', 'footwear', 'count', null, null),
    (7, '완주왕', '첫 벙 완주 도전과제', 'normal', 'footwear', 'count', null, null),
    (19, '벙 완료하기', '벙 완료하기 반복 도전과제', 'repetitive', 'footwear', 'count', null, null)
;

INSERT INTO tb_challenge_stages (stage_id, stage_number, condition_count, challenge_id, weight_common, weight_rare, weight_epic)
VALUES
    (1, 1, 1, 1, 100, 0, 0),
    (2, 1, 1, 2, 100, 0, 0),
    (3, 2, 3, 2, 100, 0, 0),
    (4, 3, 5, 2, 100, 0, 0),
    -- stage_id 10+ : 기존 1~4와 충돌 방지
    (10, 1, 1, 3, 100, 0, 0),
    (11, 2, 2, 3, 100, 0, 0),
    (12, 3, 3, 3, 100, 0, 0),
    (13, 1, 1, 4, 100, 0, 0),
    (14, 2, 2, 4, 100, 0, 0),
    (15, 1, 1, 5, 100, 0, 0),
    (16, 1, 1, 6, 100, 0, 0),
    (17, 1, 1, 7, 100, 0, 0),
    (18, 1, 1, 19, 100, 0, 0),
    (19, 2, 2, 19, 100, 0, 0)
;

INSERT INTO tb_users_challenges (user_challenge_id, user_id, challenge_stage_id, current_count, current_progress, nft_completed, completed_date)
VALUES (1, '9e1bfc60-f76a-47dc-9147-803653707192', 1, 1, 100.0, false, CURRENT_TIMESTAMP),
       (2, '9e1bfc60-f76a-47dc-9147-803653707192', 3, 3, 100.0, false, CURRENT_TIMESTAMP);

-- Swarm-backed catalog (tb_nfts / tb_nft_tokens), owned by openrun-nft-tools.
-- *_ref values are bare 64-char hex Swarm references. nft_id ordering drives the
-- avatar-token candidate list [100,200,300,500,600]; nfts 3/5/8/9 have no avatar token.
INSERT INTO tb_nfts (nft_id, name, category, rarity, thumbnail_ref, avatar_ref, avatar2_ref)
VALUES (1, '테스트 상의', 'top', 'common',
        '01a0efefefefefefefefefefefefefefefefefefefefefefefefefefefefefef',
        '01b0efefefefefefefefefefefefefefefefefefefefefefefefefefefefefef', NULL),
       (2, '테스트 헤어', 'hair', 'rare',
        '02a0efefefefefefefefefefefefefefefefefefefefefefefefefefefefefef',
        '02b0efefefefefefefefefefefefefefefefefefefefefefefefefefefefefef',
        '02c0efefefefefefefefefefefefefefefefefefefefefefefefefefefefefef'),
       (3, '토큰 없는 아이템', 'face', 'common',
        '03a0efefefefefefefefefefefefefefefefefefefefefefefefefefefefefef',
        '03b0efefefefefefefefefefefefefefefefefefefefefefefefefefefefefef', NULL),
       (4, 'shoes1', 'shoes', 'common',
        '04a0efefefefefefefefefefefefefefefefefefefefefefefefefefefefefef',
        '04b0efefefefefefefefefefefefefefefefefefefefefefefefefefefefefef', NULL),
       (5, '비활성 하의', 'pants', 'common',
        '05a0efefefefefefefefefefefefefefefefefefefefefefefefefefefefefef',
        '05b0efefefefefefefefefefefefefefefefefefefefefefefefefefefefefef', NULL),
       (6, '동글이안경', 'head_acc', 'common',
        '06a0efefefefefefefefefefefefefefefefefefefefefefefefefefefefefef',
        '06b0efefefefefefefefefefefefefefefefefefefefefefefefefefefefefef', NULL),
       (7, '반짝 대머리', 'hair', 'common',
        '07a0efefefefefefefefefefefefefefefefefefefefefefefefefefefefefef',
        '07b0efefefefefefefefefefefefefefefefefefefefefefefefefefefefefef', NULL),
       (8, '장착 이미지 없는 아이템', 'body_acc', 'common',
        '08a0efefefefefefefefefefefefefefefefefefefefefefefefefefefefefef', NULL, NULL),
       (9, '장착 이미지 없는 헤어', 'hair', 'common',
        '09a0efefefefefefefefefefefefefefefefefefefefefefefefefefefefefef', NULL, NULL);

INSERT INTO tb_nft_tokens (token_id, nft_id, image_role)
VALUES ('100', 1, 'avatar'),
       ('200', 2, 'avatar'),
       ('300', 4, 'avatar'),
       ('500', 6, 'avatar'),
       ('600', 7, 'avatar'),
       ('1001', 1, 'thumbnail'),
       ('1002', 2, 'thumbnail'),
       ('1004', 4, 'thumbnail'),
       ('1006', 6, 'thumbnail'),
       ('1007', 7, 'thumbnail');
