package com.hermes.multitenancy.util;

import com.hermes.multitenancy.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 데이터베이스 스키마 관리 유틸리티
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaUtils {

    private final DataSource dataSource;

    /**
     * 스키마 존재 여부 확인
     */
    public boolean schemaExists(String schemaName) {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            String sql = "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, schemaName);
            
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Error checking if schema exists: {}", schemaName, e);
            return false;
        }
    }

    /**
     * 새로운 스키마 생성
     */
    public boolean createSchema(String schemaName) {
        if (!TenantUtils.isValidSchemaName(schemaName)) {
            log.error("Invalid schema name: {}", schemaName);
            return false;
        }

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // 스키마가 이미 존재하는지 확인
            if (schemaExists(schemaName)) {
                log.info("Schema already exists: {}", schemaName);
                return true;
            }

            // 스키마 생성
            String createSql = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
            jdbcTemplate.execute(createSql);
            
            log.info("Schema created successfully: {}", schemaName);
            return true;
            
        } catch (Exception e) {
            log.error("Error creating schema: {}", schemaName, e);
            return false;
        }
    }

    /**
     * 스키마 삭제
     */
    public boolean dropSchema(String schemaName) {
        if (TenantContext.DEFAULT_SCHEMA_NAME.equals(schemaName)) {
            log.error("Cannot drop default schema: {}", schemaName);
            return false;
        }

        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // 스키마 존재 확인
            if (!schemaExists(schemaName)) {
                log.info("Schema does not exist: {}", schemaName);
                return true;
            }

            // 스키마 삭제 (CASCADE 옵션으로 모든 객체 포함 삭제)
            String dropSql = "DROP SCHEMA " + schemaName + " CASCADE";
            jdbcTemplate.execute(dropSql);
            
            log.info("Schema dropped successfully: {}", schemaName);
            return true;
            
        } catch (Exception e) {
            log.error("Error dropping schema: {}", schemaName, e);
            return false;
        }
    }

    /**
     * 스키마의 모든 테이블 삭제 (스키마는 유지)
     */
    public boolean clearSchema(String schemaName) {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // 스키마의 모든 테이블 조회
            String tablesSql = "SELECT table_name FROM information_schema.tables " +
                             "WHERE table_schema = ? AND table_type = 'BASE TABLE'";
            
            java.util.List<String> tables = jdbcTemplate.queryForList(tablesSql, String.class, schemaName);
            
            // 각 테이블 삭제
            for (String tableName : tables) {
                String dropTableSql = "DROP TABLE IF EXISTS " + schemaName + "." + tableName + " CASCADE";
                jdbcTemplate.execute(dropTableSql);
                log.debug("Dropped table: {}.{}", schemaName, tableName);
            }
            
            log.info("Schema cleared successfully: {}", schemaName);
            return true;
            
        } catch (Exception e) {
            log.error("Error clearing schema: {}", schemaName, e);
            return false;
        }
    }

    /**
     * 스키마의 테이블 목록 조회
     */
    public java.util.List<String> getSchemaTableNames(String schemaName) {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            String sql = "SELECT table_name FROM information_schema.tables " +
                        "WHERE table_schema = ? AND table_type = 'BASE TABLE' ORDER BY table_name";
            
            return jdbcTemplate.queryForList(sql, String.class, schemaName);
            
        } catch (Exception e) {
            log.error("Error getting table names for schema: {}", schemaName, e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 스키마의 테이블 수 조회
     */
    public int getSchemaTableCount(String schemaName) {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                        "WHERE table_schema = ? AND table_type = 'BASE TABLE'";
            
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, schemaName);
            return count != null ? count : 0;
            
        } catch (Exception e) {
            log.error("Error getting table count for schema: {}", schemaName, e);
            return 0;
        }
    }

    /**
     * 현재 연결의 search_path 설정
     */
    public void setSearchPath(String schemaName) {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            String sql = "SET search_path TO " + schemaName;
            jdbcTemplate.execute(sql);
            
            log.debug("Search path set to: {}", schemaName);
            
        } catch (Exception e) {
            log.error("Error setting search path to: {}", schemaName, e);
        }
    }

    /**
     * 현재 search_path 조회
     */
    public String getCurrentSearchPath() {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            String sql = "SHOW search_path";
            
            return jdbcTemplate.queryForObject(sql, String.class);
            
        } catch (Exception e) {
            log.error("Error getting current search path", e);
            return null;
        }
    }
}
