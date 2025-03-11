package com.c202.userservice.global.exception;

// 기본 예외 클래스
public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    // 리소스를 찾을 수 없을 때 사용하는 예외
    public static class ResourceNotFoundException extends ServiceException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    // 사용자명, 닉네임 등이 이미 존재할 때 사용하는 예외
    public static class UsernameAlreadyExistsException extends ServiceException {
        public UsernameAlreadyExistsException(String message) {
            super(message);
        }
    }

    // 인증 관련 예외
    public static class AuthenticationException extends ServiceException {
        public AuthenticationException(String message) {
            super(message);
        }
    }

    // 권한 관련 예외
    public static class AuthorizationException extends ServiceException {
        public AuthorizationException(String message) {
            super(message);
        }
    }

    // 유효성 검사 실패 예외
    public static class ValidationException extends ServiceException {
        public ValidationException(String message) {
            super(message);
        }
    }
}