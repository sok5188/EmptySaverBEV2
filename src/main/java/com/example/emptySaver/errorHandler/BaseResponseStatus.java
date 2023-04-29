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
    POST_USERS_EXISTS_EMAIL(false, 2021, "이미 가입된 이메일입니다."),
    INVALID_ID(false, 2220, "존재하지 않는 유저ID입니다."),
    INVALID_EMAIL(false, 2221, "존재하지 않는 이메일입니다."),
    INVALID_PASSWORD(false, 2222, "유효하지 않은 비밀번호입니다."),
    INVALID_CHANGE_ATTEMPT_NAME(false, 2223, "유효하지 않은 변경 시도입니다.(이름이 올바르지 않습니다.)"),
    INVALID_SAVE_ATTEMPT(false,2225,"유효하지 않은 저장 시도입니다.(이미 저장되어 있습니다)"),
    INVALID_DELETE_ATTEMPT(false,2225,"유효하지 않은 삭제 시도입니다."),
    INVALID_FCMTOKEN(false,2226,"FCM 토큰이 유효하지 않습니다"),

    /**
     * 3000 : Response 오류
     */
    // Common
    RESPONSE_ERROR(false, 3000, "값을 불러오는데 실패하였습니다."),
    FAILED_TO_SEND_NOTIFICATION(false, 3010, "알림 전송에 실패했습니다."),
    FAILED_TO_LOGIN(false, 3014, "가입된 유저가 아닙니다."),
    FAILED_TO_SEND_MAIL(false,3111,"메일 전송에 실패했습니다.");


    private final boolean isSuccess;

    private final int code;

    private final String message;


    private BaseResponseStatus(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}
