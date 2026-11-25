package com.fridgebot.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

/** 컨트롤러 인자에 붙이면 액세스 토큰(JWT)의 subject를 사용자 ID(Long)로 받는다. */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "T(java.lang.Long).valueOf(subject)")
public @interface CurrentUserId {

}
