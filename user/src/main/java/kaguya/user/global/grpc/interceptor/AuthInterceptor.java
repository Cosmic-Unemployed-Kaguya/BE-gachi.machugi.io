package kaguya.user.global.grpc.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

@Slf4j
@GrpcGlobalServerInterceptor  // 모든 gRPC 요청에 자동 적용
public class AuthInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next)
    {

        // HTTP의 Request Header와 같은 gRPC Metadata에서 유저 정보 추출
        String role = headers.get(GrpcContextKeys.USER_ROLE_META_KEY);
        String userIdx = headers.get(GrpcContextKeys.USER_IDX_META_KEY);
        String guestIdx = headers.get(GrpcContextKeys.GUEST_IDX_META_KEY);

        // [테스트용] 헤더 조작 확인 후 삭제
        log.info("[AuthInterceptor] role=[{}], userIdx=[{}], guestIdx=[{}]", role, userIdx, guestIdx);

        // gRPC Context(상태 저장소) 가져오기
        Context context = Context.current();

        if (role != null && !role.isBlank()) {
            context = context.withValue(GrpcContextKeys.USER_ROLE_CTX_KEY, role);
        }

        // 회원 식별자: 숫자(Long)일 때만 담음
        if (userIdx != null && !userIdx.isBlank()) {
            try {
                context = context.withValue(GrpcContextKeys.USER_IDX_CTX_KEY, Long.valueOf(userIdx));
            } catch (NumberFormatException e) {
                log.warn("잘못된 x-user-idx 형식: {}", userIdx);
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid data format"), new Metadata());
                return new ServerCall.Listener<ReqT>() {};
            }
        }

        // 게스트 식별자: UUID 문자열 그대로 담음
        if (guestIdx != null && !guestIdx.isBlank()) {
            context = context.withValue(GrpcContextKeys.GUEST_IDX_CTX_KEY, guestIdx);
        }

        // Context를 담아서 다음 Interceptor 또는 실제 gRPC 서비스 로직으로 요청 전달
        return Contexts.interceptCall(context, call, headers, next);
    }
}