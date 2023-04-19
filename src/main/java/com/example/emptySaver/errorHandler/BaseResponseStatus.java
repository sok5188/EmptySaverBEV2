package com.example.emptySaver.errorHandler;


import lombok.Getter;

@Getter
public enum BaseResponseStatus {

    /**
     * 1000 : 요청 성공
     */
    SUCCESS(true,1000,"요청에 성공하였습니다."),

    /**
     * 2000 : Request 오류
     */
    EMPTY_JWT(false, 2001, "JWT를 입력해주세요."),
    INVALID_JWT(false, 2002, "유효하지 않은 JWT입니다."),
    INVALID_USER_JWT(false,2003,"권한이 없는 유저의 접근입니다."),

    POST_USERS_EXISTS_EMAIL(false, 2021, "이미 가입된 이메일입니다."),
    INVALID_SINGER_ID(false,2220,"유효하지 않은 가수 id입니다."),
    INVALID_USER_ID(false,2221,"유효하지 않은 회원 id입니다."),
    INVALID_ALBUM_ID(false,2222,"유효하지 않은 앨범 id입니다."),
    INVALID_MUSIC_ID(false,2223,"유효하지 않은 음악 id입니다."),
    INVALID_PLAYLIST_ID(false,2224,"유효하지 않은 플레이리스트 id입니다."),
    INVALID_SAVE_ATTEMPT(false,2225,"유효하지 않은 저장 시도입니다.(이미 저장되어 있습니다)"),
    INVALID_DELETE_ATTEMPT(false,2225,"유효하지 않은 삭제 시도입니다."),

    /**
     * 3000 : Response 오류
     */
    // Common
    RESPONSE_ERROR(false, 3000, "값을 불러오는데 실패하였습니다."),


    POST_USERS_EMPTY_EMAIL(false, 2015, "이메일을 입력해주세요."),

    FAILED_TO_LOGIN(false, 3014, "가입된 유저가 아닙니다."),

    /**
     * 4000 : Database, Server 오류
     */
    DATABASE_ERROR(false,4000,"데이터베이스 연결에 실패하였습니다."),
    SERVER_ERROR(false,4001,"서버와의 연결에 실패하였습니다.");


    private final boolean isSuccess;

    private final int code;

    private final String message;


    private BaseResponseStatus(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}
