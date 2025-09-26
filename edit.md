### 수정 계획 요약 (2025-09-26)

1. **예약 상세 (예약 관리) 개선**
   - 백엔드 `ReservationRepository`의 상세 조회 쿼리를 Projection 기반으로 정리하여 필드 매핑 오류 제거
   - `AdminReservationService`에서 Object[] 기반 가공 로직을 Projection DTO 매핑으로 단순화, 예외 처리 보강
   - 프론트 `ReservationMonitoring.vue`에서 예약/결제 상태 매핑 및 상세 모달 데이터 병합 로직을 단일 스펙으로 정리
   - `ReservationDetailDto` 사용 라인 검토 및 필요 시 필드 보완

2. **결제 목록 (결제 관리) 정상화**
   - 백엔드 `PaymentRepository`에 결제·예약·호텔·사용자 정보가 포함된 검색 Projection 쿼리 추가
   - `AdminPaymentService`가 새로운 Projection을 `PaymentSummaryDto`로 변환하도록 수정
   - 프론트 `PaymentManagement.vue`에서 목록 렌더링 및 필터/정렬 파라미터 매핑을 새로운 DTO 구조에 맞게 업데이트, 오류 알림 정비

3. **리뷰 관리 고도화**
   - 리뷰 목록/상세에 필요한 호텔명·작성자·평점 등 정보를 제공하는 백엔드 전용 DTO 및 쿼리 구현
   - 신고/숨김 상태 필드 정합성 검토 후 관리 API 및 서비스 로직 보강
   - 프론트 `ReviewManagement.vue`를 axios 기반으로 전환하고, 목록/상세/필터/상태 변경 동작을 새 응답 구조에 맞춰 수정

4. **쿠폰 관리 정합성 확보**
   - 백엔드 쿠폰 생성/수정/조회 API 파라미터와 응답 구조를 `db.md` 스키마에 맞춰 재정비 (필수 필드, 기본값, 타입 일치)
   - 통계 API가 활성/비활성/만료 수치를 정확히 반환하도록 점검
   - 프론트 `CouponManagement.vue`의 폼 입력 검증, 목록/통계 호출, 필터/페이지네이션 로직을 API 변경 사항과 동기화

> 위 순서(1→2→3→4)로 단계별 수정 및 검증 진행 예정

