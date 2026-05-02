package com.javainternshiporderservice.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUtilsTest {

    private SecurityUtils securityUtils;

    @BeforeEach
    void setUp() {
        securityUtils = new SecurityUtils();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isCurrentUserAdmin_shouldReturnTrue_whenUserHasAdminRole() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "1", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(securityUtils.isCurrentUserAdmin()).isTrue();
    }

    @Test
    void isCurrentUserAdmin_shouldReturnFalse_whenUserHasOnlyUserRole() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "1", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(securityUtils.isCurrentUserAdmin()).isFalse();
    }

    @Test
    void isCurrentUserAdmin_shouldReturnFalse_whenNoAuthentication() {
        SecurityContextHolder.clearContext();

        assertThat(securityUtils.isCurrentUserAdmin()).isFalse();
    }

    @Test
    void isCurrentUserAdmin_shouldReturnFalse_whenAuthenticationIsNull() {
        SecurityContextHolder.getContext().setAuthentication(null);

        assertThat(securityUtils.isCurrentUserAdmin()).isFalse();
    }

    @Test
    void getCurrentUserId_shouldReturnUserId_whenPrincipalIsNumeric() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "123", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Long result = securityUtils.getCurrentUserId();

        assertThat(result).isEqualTo(123L);
    }

    @Test
    void getCurrentUserId_shouldReturnNull_whenPrincipalIsNotNumeric() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user@example.com", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Long result = securityUtils.getCurrentUserId();

        assertThat(result).isNull();
    }

    @Test
    void getCurrentUserId_shouldReturnNull_whenNoAuthentication() {
        SecurityContextHolder.clearContext();

        Long result = securityUtils.getCurrentUserId();

        assertThat(result).isNull();
    }

    @Test
    void getCurrentUserId_shouldReturnNull_whenAuthenticationIsNull() {
        SecurityContextHolder.getContext().setAuthentication(null);

        Long result = securityUtils.getCurrentUserId();

        assertThat(result).isNull();
    }

    @Test
    void isOwnerOrAdmin_shouldReturnTrue_whenUserIsAdmin() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "999", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(securityUtils.isOwnerOrAdmin(1L)).isTrue();
        assertThat(securityUtils.isOwnerOrAdmin(999L)).isTrue();
        assertThat(securityUtils.isOwnerOrAdmin(123L)).isTrue();
    }

    @Test
    void isOwnerOrAdmin_shouldReturnTrue_whenUserIsOwner() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "5", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(securityUtils.isOwnerOrAdmin(5L)).isTrue();
    }

    @Test
    void isOwnerOrAdmin_shouldReturnFalse_whenUserIsNotOwnerAndNotAdmin() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "5", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(securityUtils.isOwnerOrAdmin(10L)).isFalse();
    }

    @Test
    void isOwnerOrAdmin_shouldReturnFalse_whenNoAuthentication() {
        SecurityContextHolder.clearContext();

        assertThat(securityUtils.isOwnerOrAdmin(1L)).isFalse();
    }

    @Test
    void isOwnerOrAdmin_shouldReturnFalse_whenUserIdIsNull() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "not-a-number", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(securityUtils.isOwnerOrAdmin(1L)).isFalse();
    }

    @Test
    void isOwnerOrAdmin_shouldHandleZeroUserId() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "0", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(securityUtils.isOwnerOrAdmin(0L)).isTrue();
    }

    @Test
    void isOwnerOrAdmin_shouldHandleNegativeUserId() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "-1", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(securityUtils.isOwnerOrAdmin(-1L)).isTrue();
    }
}
