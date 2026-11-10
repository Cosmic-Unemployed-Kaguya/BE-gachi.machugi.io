package kaguya.user.domain.user.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import kaguya.grpc.admin.*;
import kaguya.user.global.grpc.interceptor.GrpcContextKeys;
import kaguya.user.domain.user.mapper.AdminGrpcMapper;
import kaguya.user.domain.user.model.dto.request.BlockReq;
import kaguya.user.domain.user.model.dto.request.BlockSearchReq;
import kaguya.user.domain.user.model.dto.request.UserSearchReq;
import kaguya.user.domain.user.model.dto.response.GetBlocksInfoRes;
import kaguya.user.domain.user.model.dto.response.GetUserDetailsRes;
import kaguya.user.domain.user.model.dto.response.GetUsersInfoRes;
import kaguya.user.domain.user.model.enums.Role;
import kaguya.user.domain.user.service.AdminService;
import kaguya.user.global.exception.BusinessException;
import kaguya.user.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.Pageable;

@GrpcService
@RequiredArgsConstructor
public class AdminGrpcServer extends AdminServiceGrpc.AdminServiceImplBase {

    private final AdminService adminService;
    private final AdminGrpcMapper mapper;

    @Override
    public void getUserList(GetUsersInfoRequest request, StreamObserver<GetUsersInfoResponse> responseObserver) {

        checkAdmin();

        UserSearchReq condition = mapper.toUserSearchReq(request.getCondition());
        Pageable pageable = mapper.toUserPageable(request.getPageParam(), request.getOrderBy());

        GetUsersInfoRes resData = adminService.getUserList(condition, pageable);
        GetUsersInfoResponse response = mapper.toGetUsersInfoResponse(resData);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUserDetails(GetUserDetailsRequest request, StreamObserver<GetUserDetailsResponse> responseObserver) {

        checkAdmin();

        GetUserDetailsRes resData = adminService.getUserDetails(request.getUserIdx());
        GetUserDetailsResponse response = mapper.toUserDetailsResponse(resData);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateNickname(UpdateNicknameRequest request, StreamObserver<Empty> responseObserver) {

        checkAdmin();

        adminService.updateNickname(request.getUserIdx(), request.getNickname());

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void updateRole(UpdateRoleRequest request, StreamObserver<Empty> responseObserver) {

        checkAdmin();

        Role role = mapper.toRole(request.getRole());
        adminService.updateRole(request.getUserIdx(), role);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void blockUser(BlockRequest request, StreamObserver<Empty> responseObserver) {

        Long adminIdx = checkAdmin();  // 제재 처리자 기록용으로 관리자 idx 사용

        BlockReq reqData = mapper.toBlockReq(request);
        adminService.blockUser(request.getUserIdx(), adminIdx, reqData);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void getBlockList(GetBlockListRequest request, StreamObserver<GetBlocksInfoResponse> responseObserver) {

        checkAdmin();

        BlockSearchReq condition = mapper.toBlockSearchReq(request.getCondition());
        Pageable pageable = mapper.toBlockPageable(request.getPageParam(), request.getOrderBy());

        GetBlocksInfoRes resData = adminService.getBlockList(condition, pageable);
        GetBlocksInfoResponse response = mapper.toGetBlocksInfoResponse(resData);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    /**
     * 헬퍼 메서드
     */
    // 관리자 권한 확인. 필요한 경우 관리자의 idx 반환
    private Long checkAdmin() {

        Long idx = GrpcContextKeys.USER_IDX_CTX_KEY.get();
        String role = GrpcContextKeys.USER_ROLE_CTX_KEY.get();

        if (!Role.ADMIN.name().equals(role) || idx == null) {
            // 예외를 던지면 GlobalGrpcExceptionHandler가 가로채어 표준 gRPC 에러 응답으로 변환
            throw new BusinessException(ErrorCode.DENIED_PERMISSION);
        }

        return idx;
    }
}