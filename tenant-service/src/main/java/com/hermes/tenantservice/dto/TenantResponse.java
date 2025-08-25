package com.hermes.tenantservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hermes.tenantservice.entity.Tenant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 테넌트 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "테넌트 정보 응답")
public class TenantResponse {

    @Schema(description = "테넌트 ID", example = "1")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "테넌트 식별자", example = "company1")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "테넌트명", example = "Sample Company")
    @JsonProperty("name")
    private String name;

    @Schema(description = "테넌트 설명", example = "샘플 회사 테넌트")
    @JsonProperty("description")
    private String description;

    @Schema(description = "스키마명", example = "tenant_company1")
    @JsonProperty("schemaName")
    private String schemaName;

    @Schema(description = "테넌트 상태", example = "ACTIVE")
    @JsonProperty("status")
    private String status;

    @Schema(description = "관리자 이메일", example = "admin@company.com")
    @JsonProperty("adminEmail")
    private String adminEmail;

    @Schema(description = "생성일시", example = "2024-01-01T00:00:00")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-01T00:00:00")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    /**
     * Tenant 엔티티로부터 TenantResponse 생성
     */
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getTenantId(),
                tenant.getName(),
                tenant.getDescription(),
                tenant.getSchemaName(),
                tenant.getStatus().name(),
                tenant.getAdminEmail(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }
}
