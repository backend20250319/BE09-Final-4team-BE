package com.hermes.tenantservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 테넌트 수정 요청 DTO
 */
@Data
@Schema(description = "테넌트 수정 요청")
public class UpdateTenantRequest {

    @Schema(description = "테넌트명", example = "Updated Company Name")
    @Size(min = 1, max = 100, message = "테넌트명은 1-100자 사이여야 합니다")
    @JsonProperty("name")
    private String name;

    @Schema(description = "테넌트 설명", example = "업데이트된 회사 설명")
    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    @JsonProperty("description")
    private String description;

    @Schema(description = "관리자 이메일", example = "new-admin@company.com")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @JsonProperty("adminEmail")
    private String adminEmail;

    @Schema(description = "테넌트 상태", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED"})
    @JsonProperty("status")
    private String status;
}
