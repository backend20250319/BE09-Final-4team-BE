package com.hermes.multitenancy.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class NonTenantTest {

    @Test
    void 클래스_레벨_NonTenant_어노테이션_확인() throws NoSuchMethodException {
        // given
        Class<?> controllerClass = TestNonTenantController.class;

        // when
        NonTenant annotation = controllerClass.getAnnotation(NonTenant.class);

        // then
        assertNotNull(annotation);
    }

    @Test
    void 메소드_레벨_NonTenant_어노테이션_확인() throws NoSuchMethodException {
        // given
        Method method = TestController.class.getMethod("nonTenantMethod");

        // when
        NonTenant annotation = method.getAnnotation(NonTenant.class);

        // then
        assertNotNull(annotation);
    }

    @Test
    void NonTenant_어노테이션이_없는_메소드_확인() throws NoSuchMethodException {
        // given
        Method method = TestController.class.getMethod("tenantMethod");

        // when
        NonTenant annotation = method.getAnnotation(NonTenant.class);

        // then
        assertNull(annotation);
    }

    @NonTenant
    @RestController
    static class TestNonTenantController {
        @GetMapping("/health")
        public String health() {
            return "OK";
        }
    }

    @RestController
    static class TestController {
        @NonTenant
        @GetMapping("/public")
        public String nonTenantMethod() {
            return "Public";
        }

        @GetMapping("/private")
        public String tenantMethod() {
            return "Private";
        }
    }
}