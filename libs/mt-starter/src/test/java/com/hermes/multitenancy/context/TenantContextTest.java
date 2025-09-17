package com.hermes.multitenancy.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void 테넌트_ID_설정_및_조회() {
        // given
        String tenantId = "company1";

        // when
        TenantContext.setTenantId(tenantId);

        // then
        assertEquals(tenantId, TenantContext.getCurrentTenantId());
        assertTrue(TenantContext.hasTenantContext());
    }


    @Test
    void 컨텍스트_미설정시_테넌트ID_조회시_예외_발생() {
        // when & then
        assertThrows(IllegalStateException.class, () -> {
            TenantContext.getCurrentTenantId();
        });
    }

    @Test
    void executeWithTenant_테스트() {
        // given
        String tenantId = "company1";
        String result = "test_result";

        // when
        String actualResult = TenantContext.executeWithTenant(tenantId, () -> {
            assertEquals(tenantId, TenantContext.getCurrentTenantId());
            return result;
        });

        // then
        assertEquals(result, actualResult);
    }


    @Test
    void 컨텍스트_정리_테스트() {
        // given
        TenantContext.setTenantId("company1");

        // when
        TenantContext.clear();

        // then
        assertFalse(TenantContext.hasTenantContext());
    }

    @Test
    void null_또는_빈_테넌트ID_설정시_무시() {
        // when & then
        TenantContext.setTenantId(null);
        assertFalse(TenantContext.hasTenantContext());

        TenantContext.setTenantId("");
        assertFalse(TenantContext.hasTenantContext());

        TenantContext.setTenantId("   ");
        assertFalse(TenantContext.hasTenantContext());
    }
}