package kaguya.user.global.grpc;

import kaguya.user.global.exception.BusinessException;
import kaguya.user.global.exception.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * gRPC 요청의 page / size / order_by -> Spring Pageable 변환
 * - 컨트롤러의 @PageableDefault, max-page-size 역할
 * - 허용 필드와 기본 정렬은 API마다 넘겨받음 (어떤 gRPC 서비스에서도 재사용 가능)
 */
public final class GrpcPageableConverter {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    // 상태 없는 유틸 클래스 -> 인스턴스 생성 막음
    private GrpcPageableConverter() {
    }

    // page + size + order_by -> Pageable. 안 보낸 값은 기본값, size는 상한 적용
    public static Pageable toPageable(
            int page, int size,
            String orderBy,
            Map<String, String> sortableFields,
            List<Sort.Order> defaultOrders
    ) {

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        Sort sort = toSort(orderBy, sortableFields, defaultOrders);

        return PageRequest.of(safePage, safeSize, sort);
    }

    // order_by가 비어 있으면 기본 정렬, 있으면 파싱
    private static Sort toSort(
            String orderBy,
            Map<String, String> sortableFields,
            List<Sort.Order> defaultOrders
    ) {

        List<Sort.Order> orders = orderBy.isBlank() ? defaultOrders : parseOrderBy(orderBy, sortableFields);

        return Sort.by(orders);
    }

    // order_by 파싱 + 정렬 항목 검사(화이트리스트, 중복) + API 이름 -> 엔티티 필드명 변환
    // ex. "join_date desc, last_login_date" -> [joinDate DESC, lastLoginDate ASC]
    private static List<Sort.Order> parseOrderBy(
            String orderBy,
            Map<String, String> sortableFields
    ) {

        List<Sort.Order> orders = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String clause : orderBy.split(",")) {
            String[] tokens = clause.trim().split("\\s+");
            String field = tokens[0];
            boolean desc = tokens.length > 1 && tokens[1].equals("desc");

            String property = sortableFields.get(field);

            if (property == null) {
                throw new BusinessException(ErrorCode.INVALID_SORT_FIELD);
            }
            if (!seen.add(field)) {
                throw new BusinessException(ErrorCode.DUPLICATE_SORT_FIELD);
            }

            Sort.Direction direction = desc ? Sort.Direction.DESC : Sort.Direction.ASC;
            orders.add(new Sort.Order(direction, property, Sort.NullHandling.NULLS_LAST));
        }

        return orders;
    }
}