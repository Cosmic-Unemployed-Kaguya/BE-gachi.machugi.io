package kaguya.user.domain.user.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import kaguya.grpc.user.*;
import kaguya.user.global.grpc.interceptor.GrpcContextKeys;
import kaguya.user.domain.user.model.dto.request.ResetPasswordReq;
import kaguya.user.domain.user.model.dto.request.UpdatePasswordReq;
import kaguya.user.domain.user.model.dto.response.MyPageRes;
import kaguya.user.domain.user.model.dto.response.ProfileRes;
import kaguya.user.domain.user.model.enums.Role;
import kaguya.user.domain.user.service.UserService;
import kaguya.user.global.exception.BusinessException;
import kaguya.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.regex.Pattern;

@GrpcService
@RequiredArgsConstructor
public class UserGrpcServer extends UserServiceGrpc.UserServiceImplBase {

    // 정규식 패턴
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$");

    private final UserService userService;

    @Override
    public void getMyPage(
            Empty request,
            StreamObserver<MyPageResponse> responseObserver
    ) {

        Long idx = checkUser();

        MyPageRes resData = userService.getMyPage(idx);

        MyPageResponse response = MyPageResponse.newBuilder()
//                .setUsername(resData.username())
                .setEmail(resData.email())
                .setNickname(resData.nickname())
                .setJoinDate(resData.joinDate().toString())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getProfile(
            Empty request,
            StreamObserver<ProfileResponse> responseObserver
    ) {

        Long idx = checkUser();

        ProfileRes resData = userService.getProfile(idx);

        ProfileResponse response = ProfileResponse.newBuilder()
                .setName(resData.name())
                .setBirth(resData.birth().toString())
                .setPhone(resData.phone())
                .setGender(resData.gender())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updatePassword(
            UpdatePasswordRequest request,
            StreamObserver<Empty> responseObserver
    ) {

        Long idx = checkUser();

        String newPassword = request.getNewPassword();
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        UpdatePasswordReq reqData = new UpdatePasswordReq(
                request.getCurrentPassword(),
                request.getNewPassword()
        );

        userService.updatePassword(idx, reqData);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void updateNickname(
            UpdateNicknameRequest request,
            StreamObserver<Empty> responseObserver
    ) {

        Long idx = checkUser();

        userService.updateNickname(idx, request.getNickname());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void withdraw(
            Empty request,
            StreamObserver<Empty> responseObserver
    ) {

        Long idx = checkUser();

        userService.withdraw(idx);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

/*
    @Override
    public void findUsername (
            FindUsernameRequest request,
            StreamObserver<FindUsernameResponse> responseObserver
    ) {

        String oneTimeAuthCode = request.getOneTimeAuthCode();

        FindUsernameRes resData = userService.findUsername(oneTimeAuthCode);
        FindUsernameResponse response = FindUsernameResponse.newBuilder()
                .setMaskedUsername(resData.maskedUsername())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
 */

    @Override
    public void resetPassword(
            ResetPasswordRequest request,
            StreamObserver<Empty> responseObserver
    ) {

        ResetPasswordReq reqData = new ResetPasswordReq(
                request.getOneTimeAuthCode(),
                request.getNewPassword()
        );

        if (!PASSWORD_PATTERN.matcher(reqData.newPassword()).matches()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        userService.resetPassword(reqData);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }


    /**
     * 헬퍼 메서드
     */
    // 로그인한 회원(USER, ADMIN)인지 확인. 필요한 경우 회원의 idx 반환
    private Long checkUser() {

        Long idx = GrpcContextKeys.USER_IDX_CTX_KEY.get();
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();

        if (role == null || Role.GUEST.name().equals(role) || idx == null) {
            // 예외를 던지면 GlobalGrpcExceptionHandler가 가로채어 표준 gRPC 에러 응답으로 변환
            throw new BusinessException(ErrorCode.DENIED_PERMISSION);
        }

        return idx;
    }
}