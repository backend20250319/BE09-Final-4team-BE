package com.hermes.tenantservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 테넌트 생성 요청 DTO
 */
@Data
@Schema(description = "테넌트 생성 요청")
public class CreateTenantRequest {

    @Schema(description = "테넌트 ID", example = "company1")
    @NotBlank(message = "테넌트 ID는 필수입니다")
    @Size(min = 2, max = 50, message = "테넌트 ID는 2-50자 사이여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "테넌트 ID는 영숫자, 하이픈, 언더스코어만 허용됩니다")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "테넌트명", example = "Sample Company")
    @NotBlank(message = "테넌트명은 필수입니다")
    @Size(min = 1, max = 100, message = "테넌트명은 1-100자 사이여야 합니다")
    @JsonProperty("name")
    private String name;

    @Schema(description = "테넌트 설명", example = "샘플 회사 테넌트")
    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    @JsonProperty("description")
    private String description;

    @Schema(description = "관리자 이메일", example = "admin@company.com")
    @NotBlank(message = "관리자 이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @JsonProperty("adminEmail")
    private String adminEmail;

    @Schema(description = "커스텀 스키마명 (선택사항)", example = "custom_schema")
    @Size(max = 50, message = "스키마명은 50자 이하여야 합니다")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "스키마명은 소문자로 시작하고 소문자, 숫자, 언더스코어만 허용됩니다")
    @JsonProperty("customSchemaName")
    private String customSchemaName;

    @Schema(description = "초기 스키마 생성 여부", example = "true")
    @JsonProperty("createInitialSchema")
    private boolean createInitialSchema = true;
}
