package com.hermes.tenantservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 스키마 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "스키마 정보")
public class SchemaInfo {

    @Schema(description = "스키마명", example = "tenant_company1")
    @JsonProperty("schemaName")
    private String schemaName;

    @Schema(description = "스키마 존재 여부", example = "true")
    @JsonProperty("exists")
    private boolean exists;

    @Schema(description = "테이블 개수", example = "5")
    @JsonProperty("tableCount")
    private int tableCount;

    @Schema(description = "테이블 목록")
    @JsonProperty("tables")
    private List<String> tables;

    @Schema(description = "스키마 생성 상태", example = "CREATED")
    @JsonProperty("status")
    private String status;

    @Schema(description = "상태 메시지", example = "스키마가 성공적으로 생성되었습니다")
    @JsonProperty("message")
    private String message;

    public static SchemaInfo created(String schemaName, int tableCount, List<String> tables) {
        return new SchemaInfo(schemaName, true, tableCount, tables, "CREATED", "스키마가 성공적으로 생성되었습니다");
    }

    public static SchemaInfo exists(String schemaName, int tableCount, List<String> tables) {
        return new SchemaInfo(schemaName, true, tableCount, tables, "EXISTS", "스키마가 이미 존재합니다");
    }

    public static SchemaInfo notExists(String schemaName) {
        return new SchemaInfo(schemaName, false, 0, List.of(), "NOT_EXISTS", "스키마가 존재하지 않습니다");
    }

    public static SchemaInfo error(String schemaName, String errorMessage) {
        return new SchemaInfo(schemaName, false, 0, List.of(), "ERROR", errorMessage);
    }
}
