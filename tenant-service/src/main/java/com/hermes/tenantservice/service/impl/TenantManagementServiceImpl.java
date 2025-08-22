package com.hermes.tenantservice.service.impl;

import com.hermes.tenantservice.entity.Tenant;
import com.hermes.tenantservice.dto.*;
import com.hermes.tenantservice.repository.TenantRepository;
import com.hermes.tenantservice.messaging.TenantEventPublisher;
import com.hermes.tenantservice.service.TenantManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 테넌트 관리 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantManagementServiceImpl implements TenantManagementService {

    private final TenantRepository tenantRepository;
    private final TenantEventPublisher tenantEventPublisher;

    @Override
    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        log.info("테넌트 생성 시작: tenantId={}", request.getTenantId());

        // 테넌트 ID 유효성 검증
        if (!isValidTenantId(request.getTenantId())) {
            throw new IllegalArgumentException("올바르지 않은 테넌트 ID 형식입니다: " + request.getTenantId());
        }

        // 중복 확인
        if (tenantRepository.existsByTenantId(request.getTenantId())) {
            throw new IllegalArgumentException("이미 존재하는 테넌트 ID입니다: " + request.getTenantId());
        }

        // 테넌트 엔티티 생성 (스키마명은 tenantId에서 자동 생성)
        Tenant tenant = new Tenant(
            request.getTenantId(),
            request.getName()
        );
        tenant.setDescription(request.getDescription());
        tenant.setAdminEmail(request.getAdminEmail());

        // 테넌트 저장
        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("테넌트 메타데이터 저장 완료: tenantId={}", request.getTenantId());

        // 테넌트 생성 이벤트 발행 (각 서비스가 자신의 스키마를 생성)
        if (request.isCreateInitialSchema()) {
            tenantEventPublisher.publishTenantCreated(
                savedTenant.getTenantId(),
                savedTenant.getName(),
                savedTenant.getAdminEmail()
            );
            log.info("테넌트 생성 이벤트 발행 완료: tenantId={}", request.getTenantId());
        }

        return TenantResponse.from(savedTenant);
    }

    @Override
    public PagedResponse<TenantResponse> getTenants(Pageable pageable) {
        log.debug("테넌트 목록 조회: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Tenant> tenantPage = tenantRepository.findAll(pageable);
        Page<TenantResponse> responsePage = tenantPage.map(TenantResponse::from);
        return PagedResponse.from(responsePage);
    }

    @Override
    public TenantResponse getTenant(String tenantId) {
        log.debug("테넌트 조회: tenantId={}", tenantId);

        Tenant tenant = tenantRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("테넌트를 찾을 수 없습니다: " + tenantId));
        
        return TenantResponse.from(tenant);
    }

    @Override
    @Transactional
    public TenantResponse updateTenant(String tenantId, UpdateTenantRequest request) {
        log.info("테넌트 수정: tenantId={}", tenantId);

        Tenant tenant = tenantRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("테넌트를 찾을 수 없습니다: " + tenantId));

        // 선택적 필드 업데이트
        if (StringUtils.hasText(request.getName())) {
            tenant.setName(request.getName());
        }
        if (request.getDescription() != null) {
            tenant.setDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getAdminEmail())) {
            tenant.setAdminEmail(request.getAdminEmail());
        }
        if (StringUtils.hasText(request.getStatus())) {
            try {
                Tenant.TenantStatus status = Tenant.TenantStatus.valueOf(request.getStatus().toUpperCase());
                tenant.setStatus(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("올바르지 않은 테넌트 상태입니다: " + request.getStatus());
            }
        }

        Tenant updatedTenant = tenantRepository.save(tenant);
        log.info("테넌트 수정 완료: tenantId={}", tenantId);

        return TenantResponse.from(updatedTenant);
    }

    @Override
    @Transactional
    public void deleteTenant(String tenantId, boolean deleteSchema) {
        log.warn("테넌트 삭제 요청: tenantId={}, deleteSchema={}", tenantId, deleteSchema);

        Tenant tenant = tenantRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("테넌트를 찾을 수 없습니다: " + tenantId));

        // 테넌트 삭제 이벤트 발행 (각 서비스가 자신의 스키마를 삭제)
        if (deleteSchema) {
            tenantEventPublisher.publishTenantDeleted(tenantId);
            log.info("테넌트 삭제 이벤트 발행 완료: tenantId={}", tenantId);
        }

        // 테넌트 메타데이터 삭제
        tenantRepository.delete(tenant);
        log.warn("테넌트 삭제 완료: tenantId={}", tenantId);
    }

    @Override
    @Transactional
    public TenantResponse updateTenantStatus(String tenantId, String status) {
        log.info("테넌트 상태 변경: tenantId={}, status={}", tenantId, status);

        Tenant tenant = tenantRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("테넌트를 찾을 수 없습니다: " + tenantId));

        try {
            Tenant.TenantStatus tenantStatus = Tenant.TenantStatus.valueOf(status.toUpperCase());
            tenant.setStatus(tenantStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("올바르지 않은 테넌트 상태입니다: " + status);
        }

        Tenant updatedTenant = tenantRepository.save(tenant);
        log.info("테넌트 상태 변경 완료: tenantId={}, status={}", tenantId, status);

        return TenantResponse.from(updatedTenant);
    }

    @Override
    public boolean existsTenant(String tenantId) {
        return tenantRepository.existsByTenantId(tenantId);
    }

    /**
     * 테넌트 ID 유효성 검사
     * TenantUtils.isValidTenantId() 로직을 인라인화
     */
    private boolean isValidTenantId(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            return false;
        }
        
        // 소문자로 시작, 소문자+숫자+언더스코어만 허용 (a-z0-9_)
        String pattern = "^[a-z][a-z0-9_]*$";
        int maxLength = 50;
        
        return tenantId.matches(pattern) && tenantId.length() <= maxLength;
    }
}