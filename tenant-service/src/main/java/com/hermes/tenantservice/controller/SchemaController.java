package com.hermes.tenantservice.controller;

import com.hermes.tenantservice.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 스키마 상태 조회 컨트롤러
 * 
 * 주의: 각 서비스가 독립적인 DB를 사용하므로 
 * 실제 스키마 생성/삭제는 각 서비스가 이벤트를 받아서 처리합니다.
 * 
 * 이 컨트롤러는 테넌트 메타데이터 조회 용도로만 사용됩니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/schemas")
@RequiredArgsConstructor
@Tag(name = "Schema Information", description = "스키마 정보 조회 API (읽기 전용)")
public class SchemaController {

    /**
     * 테넌트 스키마 정보 조회
     * 
     * 주의: 이는 테넌트 메타데이터 기반 정보이며, 
     * 실제 각 서비스의 스키마 상태와 다를 수 있습니다.
     */
    @GetMapping("/{tenantId}")
    @Operation(
        summary = "테넌트 스키마 메타데이터 조회", 
        description = "테넌트의 스키마 메타데이터를 조회합니다. 실제 서비스별 스키마 상태는 각 서비스에 직접 확인해야 합니다."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSchemaInfo(
            @Parameter(description = "테넌트 ID") 
            @PathVariable String tenantId) {
        
        log.debug("스키마 메타데이터 조회 요청: tenantId={}", tenantId);
        
        // TODO: 실제로는 각 서비스의 스키마 상태를 조회하는 API를 호출하거나
        // 이벤트 기반으로 수집된 상태 정보를 반환해야 함
        
        Map<String, Object> schemaInfo = Map.of(
            "tenantId", tenantId,
            "expectedSchemaName", "tenant_" + tenantId.toLowerCase(),
            "note", "실제 스키마 상태는 각 서비스(user-service, news-service 등)에서 개별적으로 관리됩니다.",
            "recommendation", "각 서비스의 actuator/health 엔드포인트를 통해 스키마 상태를 확인하세요."
        );
        
        return ResponseEntity.ok(
                ApiResponse.success("스키마 메타데이터 조회 완료", schemaInfo));
    }

    /**
     * 서비스별 스키마 상태 요약 (향후 구현 예정)
     */
    @GetMapping("/{tenantId}/services")
    @Operation(
        summary = "서비스별 스키마 상태 요약", 
        description = "각 서비스의 스키마 상태를 요약해서 보여줍니다 (향후 구현 예정)"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> getServiceSchemaStatus(
            @Parameter(description = "테넌트 ID") 
            @PathVariable String tenantId) {
        
        log.debug("서비스별 스키마 상태 요약 요청: tenantId={}", tenantId);
        
        Map<String, Object> serviceStatus = Map.of(
            "tenantId", tenantId,
            "services", Map.of(
                "user-service", "각 서비스의 /actuator/health 엔드포인트를 확인하세요",
                "news-service", "각 서비스의 /actuator/health 엔드포인트를 확인하세요"
            ),
            "status", "NOT_IMPLEMENTED",
            "message", "향후 각 서비스로부터 스키마 상태를 수집하여 표시할 예정입니다."
        );
        
        return ResponseEntity.ok(
                ApiResponse.success("서비스별 스키마 상태 요약", serviceStatus));
    }
}