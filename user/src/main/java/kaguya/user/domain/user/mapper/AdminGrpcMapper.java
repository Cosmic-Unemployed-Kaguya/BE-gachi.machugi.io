package kaguya.user.domain.user.mapper;

import kaguya.grpc.admin.*;
import kaguya.user.global.grpc.GrpcPageableConverter;
import kaguya.user.domain.user.model.dto.request.BlockReq;
import kaguya.user.domain.user.model.dto.request.BlockSearchReq;
import kaguya.user.domain.user.model.dto.request.UserSearchReq;
import kaguya.user.domain.user.model.dto.response.GetBlockDetailsRes;
import kaguya.user.domain.user.model.dto.response.GetBlocksInfoRes;
import kaguya.user.domain.user.model.dto.response.GetUserDetailsRes;
import kaguya.user.domain.user.model.dto.response.GetUsersInfoRes;
import kaguya.user.domain.user.model.enums.ManagementType;
import kaguya.user.domain.user.model.enums.Role;
import kaguya.user.domain.user.model.enums.SearchType;
import kaguya.user.domain.user.model.enums.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AdminService gRPC 변환 전담
 * - 요청: proto message -> 서비스 DTO / Pageable
 * - 응답: 서비스 DTO -> proto message
 */
@Component
public class AdminGrpcMapper {

    // 정렬 허용 필드 (API 이름 -> 엔티티 필드명)
    private static final Map<String, String> USER_SORTABLE_FIELDS = Map.of(
            "join_date", "joinDate",
            "last_login_date", "lastLoginDate"
    );
    private static final Map<String, String> BLOCK_SORTABLE_FIELDS = Map.of(
            "start_date", "startDate",
            "end_date", "endDate"
    );

    // order_by를 보내지 않았을 때 쓰는 기본 정렬
    private static final List<Sort.Order> USER_DEFAULT_ORDERS = List.of(Sort.Order.desc("joinDate"));
    private static final List<Sort.Order> BLOCK_DEFAULT_ORDERS = List.of(Sort.Order.desc("startDate"));


    /**
     * 요청 변환 (proto -> 서비스)
     */
    // UserSearchRequest -> UserSearchReq
    public UserSearchReq toUserSearchReq(UserSearchRequest c) {

        SearchType searchType = switch (c.getSearchType()) {
            case EMAIL -> SearchType.EMAIL;
            case NICKNAME -> SearchType.NICKNAME;
            case SEARCH_TYPE_UNSPECIFIED, UNRECOGNIZED -> null;
        };

        Role role = switch (c.getRole()) {
            case USER -> Role.USER;
            case ADMIN -> Role.ADMIN;
            case ROLE_UNSPECIFIED, UNRECOGNIZED -> null;
        };

        Status status = switch (c.getStatus()) {
            case ACTIVE -> Status.ACTIVE;
            case DORMANT -> Status.DORMANT;
            case SUSPENDED -> Status.SUSPENDED;
            case BANNED -> Status.BANNED;
            case WITHDRAWAL -> Status.WITHDRAWAL;
            case STATUS_UNSPECIFIED, UNRECOGNIZED -> null;
        };

        String keyword = c.getKeyword().isBlank() ? null : c.getKeyword().trim();

        return new UserSearchReq(searchType, keyword, role, status);
    }

    // BlockSearchRequest -> BlockSearchReq
    public BlockSearchReq toBlockSearchReq(BlockSearchRequest c) {

        SearchType searchType = switch (c.getSearchType()) {
            case EMAIL -> SearchType.EMAIL;
            case NICKNAME -> SearchType.NICKNAME;
            case SEARCH_TYPE_UNSPECIFIED, UNRECOGNIZED -> null;
        };

        ManagementType managementType = switch (c.getManagementType()) {
            case WARNING -> ManagementType.WARNING;
            case TEMP_BAN -> ManagementType.TEMP_BAN;
            case PERM_BAN -> ManagementType.PERM_BAN;
            case MANAGEMENT_TYPE_UNSPECIFIED, UNRECOGNIZED -> null;
        };

        String keyword = c.getKeyword().isBlank() ? null : c.getKeyword().trim();

        return new BlockSearchReq(searchType, keyword, managementType);
    }

    // 회원 목록 PageParam + order_by -> Pageable
    public Pageable toUserPageable(PageParam p, String orderBy) {
        return GrpcPageableConverter.toPageable(
                p.getPage(), p.getSize(),
                orderBy,
                USER_SORTABLE_FIELDS,
                USER_DEFAULT_ORDERS
        );
    }

    // 제재 목록 PageParam + order_by -> Pageable
    public Pageable toBlockPageable(PageParam p, String orderBy) {
        return GrpcPageableConverter.toPageable(
                p.getPage(), p.getSize(),
                orderBy,
                BLOCK_SORTABLE_FIELDS,
                BLOCK_DEFAULT_ORDERS
        );
    }

    // GrpcRole -> Role
    public Role toRole(GrpcRole grpcRole) {
        return Role.valueOf(grpcRole.name());
    }

    // BlockRequest -> BlockReq
    public BlockReq toBlockReq(BlockRequest request) {

        // endDate가 비어있다면 null로 처리 (영구 정지는 종료일 불필요)
        LocalDateTime endDate = request.getEndDate().isBlank() ? null : LocalDateTime.parse(request.getEndDate());
        // Proto Enum -> Domain Enum 변환
        ManagementType managementType = ManagementType.valueOf(request.getManagementType().name());

        return new BlockReq(managementType, request.getReason(), endDate);
    }


    /**
     * 응답 변환 (서비스 -> proto)
     */
    // GetUsersInfoRes -> GetUsersInfoResponse (목록 + 페이징 정보)
    public GetUsersInfoResponse toGetUsersInfoResponse(GetUsersInfoRes res) {
        return GetUsersInfoResponse.newBuilder()
                .addAllUserList(res.userList().stream()
                        .map(this::toUserDetailsResponse)
                        .toList())
                .setPage(res.page())
                .setSize(res.size())
                .setTotalElements(res.totalElements())
                .setTotalPages(res.totalPages())
                .setHasNext(res.hasNext())
                .build();
    }

    // GetUserDetailsRes -> GetUserDetailsResponse
    public GetUserDetailsResponse toUserDetailsResponse(GetUserDetailsRes dto) {
        return GetUserDetailsResponse.newBuilder()
                .setIdx(dto.idx())
                .setEmail(dto.email())
                .setNickname(dto.nickname())
                .setPoint(dto.point())
                .setRole(dto.role())
                .setStatus(dto.status())
                .setJoinDate(dto.joinDate().toString())
                .setLastLoginDate(dto.lastLoginDate() != null ? dto.lastLoginDate().toString() : "")  // null -> 빈 문자열
                .setWithdrawalDate(dto.withdrawalDate() != null ? dto.withdrawalDate().toString() : "")
                .build();
    }

    // GetBlocksInfoRes -> GetBlocksInfoResponse (목록 + 페이징 정보)
    public GetBlocksInfoResponse toGetBlocksInfoResponse(GetBlocksInfoRes res) {
        return GetBlocksInfoResponse.newBuilder()
                .addAllBlockList(res.blockList().stream()
                        .map(this::toBlockDetailsResponse)
                        .toList())
                .setPage(res.page())
                .setSize(res.size())
                .setTotalElements(res.totalElements())
                .setTotalPages(res.totalPages())
                .setHasNext(res.hasNext())
                .build();
    }

    // GetBlockDetailsRes -> GetBlockDetailsResponse
    private GetBlockDetailsResponse toBlockDetailsResponse(GetBlockDetailsRes dto) {
        return GetBlockDetailsResponse.newBuilder()
                .setIdx(dto.idx())
                .setUserIdx(dto.userIdx())
                .setUserEmail(dto.userEmail())
                .setUserNickname(dto.userNickname())
                .setManagementType(dto.managementType())
                .setReason(dto.reason())
                .setStartDate(dto.startDate().toString())
                .setEndDate(dto.endDate() != null ? dto.endDate().toString() : "")  // null(영구 정지) -> 빈 문자열
                .setAdminNickname(dto.adminNickname())
                .build();
    }
}