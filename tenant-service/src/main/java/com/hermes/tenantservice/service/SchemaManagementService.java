package com.hermes.tenantservice.service;

import com.hermes.tenantservice.dto.SchemaInfo;

/**
 * 스키마 관리 서비스 인터페이스
 */
public interface SchemaManagementService {

    /**
     * 테넌트 스키마 생성
     */
    SchemaInfo createSchema(String tenantId, boolean overwrite);

    /**
     * 테넌트 스키마 정보 조회
     */
    SchemaInfo getSchemaInfo(String tenantId);

    /**
     * 테넌트 스키마 삭제
     */
    void deleteSchema(String tenantId);

    /**
     * 테넌트 스키마 초기화
     */
    SchemaInfo initializeSchema(String tenantId, boolean clearData);

    /**
     * 스키마 백업 생성
     */
    String createBackup(String tenantId);

    /**
     * 스키마 유효성 검증
     */
    SchemaInfo validateSchema(String tenantId);

    /**
     * 스키마 존재 여부 확인
     */
    boolean schemaExists(String tenantId);
}
