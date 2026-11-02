package kaguya.user.domain.verification.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import kaguya.grpc.verification.*;
import kaguya.user.domain.verification.model.dto.request.CheckVerificationCodeReq;
import kaguya.user.domain.verification.model.dto.request.SendVerificationCodeReq;
import kaguya.user.domain.verification.model.dto.response.CheckVerificationCodeRes;
import kaguya.user.domain.verification.model.enums.VerificationType;
import kaguya.user.domain.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class VerificationGrpcServer extends VerificationServiceGrpc.VerificationServiceImplBase {

    private final VerificationService verificationService;

    @Override
    public void sendVerificationCode(
            SendVerificationCodeRequest request,
            StreamObserver<Empty> responseObserver
    ) {

        VerificationType verificationType = toVerificationType(request.getVerificationType());

        SendVerificationCodeReq reqData = new SendVerificationCodeReq(
                verificationType,
                request.getEmail()
        );

        verificationService.sendVerificationCode(reqData);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void checkVerificationCode(
            CheckVerificationCodeRequest request,
            StreamObserver<CheckVerificationCodeResponse> responseObserver
    ) {

        VerificationType verificationType = toVerificationType(request.getVerificationType());

        CheckVerificationCodeReq reqData = new CheckVerificationCodeReq(
                verificationType,
                request.getVerificationCode(),
                request.getEmail()
        );

        CheckVerificationCodeRes resData = verificationService.checkVerificationCode(reqData);

        CheckVerificationCodeResponse response = CheckVerificationCodeResponse.newBuilder()
                .setOneTimeAuthCode(resData.oneTimeAuthCode())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    /**
     * 헬퍼 메서드
     */
    // GrpcVerificationType -> VerificationType
    private VerificationType toVerificationType(GrpcVerificationType type) {
        return switch (type) {
            case REGISTER -> VerificationType.REGISTER;
            case RESET_PASSWORD -> VerificationType.RESET_PASSWORD;
            case REACTIVATE_ACCOUNT -> VerificationType.REACTIVATE_ACCOUNT;
            case VERIFICATION_TYPE_UNSPECIFIED, UNRECOGNIZED
                    -> throw new IllegalStateException("검증되지 않은 인증 타입: " + type);
        };
    }
}