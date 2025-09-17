package com.hermes.multitenancy.hibernate;

import com.hermes.multitenancy.util.TenantUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Schema-per-tenant 방식의 Connection Provider
 * 단일 DataSource를 사용하되 tenant별로 search_path를 동적 변경
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaBasedConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    @Override
    public Connection getAnyConnection() throws SQLException {
        log.debug("Getting any connection (no schema set)");
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        log.debug("Releasing any connection");
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Override
    public Connection getConnection(String tenantId) throws SQLException {
        log.debug("Getting connection for tenant: {}", tenantId);

        Connection connection = dataSource.getConnection();

        try {
            if (tenantId != null) {
                String schemaName = TenantUtils.generateSchemaName(tenantId);
                setSchema(connection, schemaName);
            }

            log.debug("Successfully configured connection for tenant: {} (schema: {})",
                tenantId, tenantId != null ? TenantUtils.generateSchemaName(tenantId) : "none");

            return connection;

        } catch (SQLException e) {
            log.error("Failed to configure connection for tenant: {}", tenantId, e);
            try {
                connection.close();
            } catch (SQLException closeException) {
                log.warn("Failed to close connection after error", closeException);
            }
            throw e;
        }
    }

    @Override
    public void releaseConnection(String tenantId, Connection connection) throws SQLException {
        log.debug("Releasing connection for tenant: {}", tenantId);

        if (connection != null && !connection.isClosed()) {
            try {
                // Connection을 반납하기 전에 search_path를 기본값으로 재설정
                // 이는 Connection Pool에서 재사용될 때 안전성을 보장
                resetSchema(connection);
            } catch (SQLException e) {
                log.warn("Failed to reset schema before releasing connection for tenant: {}", tenantId, e);
            } finally {
                connection.close();
            }
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        // Connection의 적극적 해제 지원
        // 트랜잭션이 완료되면 즉시 Connection을 Pool에 반납
        return true;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnsupportedOperationException("Unwrapping is not supported by this implementation");
    }

    /**
     * Connection의 search_path 설정
     */
    private void setSchema(Connection connection, String schemaName) throws SQLException {
        String sql = "SET search_path TO " + schemaName;
        log.debug("Executing: {}", sql);

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    /**
     * Connection의 search_path를 기본값으로 재설정
     */
    private void resetSchema(Connection connection) throws SQLException {
        String sql = "RESET search_path";
        log.debug("Executing: {}", sql);

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}