package com.hermes.multitenancy.event;

import com.hermes.multitenancy.util.TenantUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 테넌트 관련 이벤트
 * 모든 서비스에서 공통으로 사용하는 테넌트 이벤트
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantEvent {
    
    public enum EventType {
        TENANT_CREATED,
        TENANT_UPDATED, 
        TENANT_DELETED,
        TENANT_STATUS_CHANGED
    }
    
    private EventType eventType;
    private String tenantId;
    private String tenantName;
    private String status;
    private String adminEmail;
    private LocalDateTime timestamp;
    
    public static TenantEvent created(String tenantId, String tenantName, String adminEmail) {
        return new TenantEvent(
            EventType.TENANT_CREATED,
            tenantId,
            tenantName,
            "ACTIVE",
            adminEmail,
            LocalDateTime.now()
        );
    }
    
    public static TenantEvent deleted(String tenantId) {
        return new TenantEvent(
            EventType.TENANT_DELETED,
            tenantId,
            null,
            null,
            null,
            LocalDateTime.now()
        );
    }

    /**
     * 스키마명을 동적으로 생성하여 반환
     */
    public String getSchemaName() {
        return TenantUtils.generateSchemaName(this.tenantId);
    }
}
