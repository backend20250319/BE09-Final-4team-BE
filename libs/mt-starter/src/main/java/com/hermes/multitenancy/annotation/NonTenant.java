package com.hermes.multitenancy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 테넌트 격리를 적용하지 않는 API임을 나타내는 어노테이션
 * 이 어노테이션이 적용된 Controller 메소드나 클래스는 데이터베이스 기본 설정을 사용합니다.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NonTenant {
}