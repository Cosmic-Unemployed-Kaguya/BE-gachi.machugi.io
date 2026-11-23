package kaguya.user.domain.verification.model.enums;

import lombok.Getter;

@Getter
public enum VerificationType {

    REGISTER("회원 가입"),
//    FIND_ID("아이디 찾기"),
    RESET_PASSWORD("비밀번호 초기화"),
    REACTIVATE_ACCOUNT("휴면상태 해제");

    private final String label;

    VerificationType(String label) {
        this.label = label;
    }
}