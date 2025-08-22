package com.hermes.multitenancy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 테넌트 정보 DTO
 * 런타임에서 테넌트 정보를 전달하기 위한 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantInfo {

    private String tenantId;
    private String name;
    private String schemaName;
    private String status;

    public static TenantInfo of(String tenantId, String schemaName) {
        return new TenantInfo(tenantId, null, schemaName, "ACTIVE");
    }
}
