package io.openur.domain.challenge.enums;

// 기본적으로 모든 도전과제는 횟수 식이다, 최소 한번 부터 시작하니까.
public enum CompletedType {
    // 날짜를 입력 받자
    date,
    // 장소 주소..?
    place,
    // 착용한 옷
    wearing,
    // 달성 페이스
    pace,
    
    count
//    || 모든 도전과제가 기본적으로 횟수라는 제한을 가지고, 떄문에 기본적인 사항일뿐, 따로 여길 필요는 없다.
}
