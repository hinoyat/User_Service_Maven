package com.c202.userservice.domain.user.service;

import com.c202.userservice.domain.user.entity.User;
import com.c202.userservice.domain.user.model.request.UpdateUserRequestDto;
import com.c202.userservice.domain.user.model.response.UserResponseDto;
import com.c202.userservice.domain.user.repository.UserRepository;
import com.c202.userservice.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 날짜 포맷터
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");


    // 사용자 정보 조회
    @Override
    public UserResponseDto getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException.ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        if ("Y".equals(user.getIsDeleted())) {
            throw new ServiceException.ResourceNotFoundException("탈퇴한 계정입니다.");
        }
        return UserResponseDto.toDto(user);
    }

    // 사용자 정보 수정
    @Override
    @Transactional
    public UserResponseDto updateUser(String username, UpdateUserRequestDto request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException.ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        if ("Y".equals(user.getIsDeleted())) {
            throw new ServiceException.ResourceNotFoundException("탈퇴한 계정입니다.");
        }

        // 닉네임 변경 시 중복 체크
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new ServiceException.UsernameAlreadyExistsException("이미 사용 중인 닉네임입니다.");
            }
            user.updateNickname(request.getNickname());
        }

        // 비밀번호 변경
        if (request.getPassword() != null) {
            user.updatePassword(passwordEncoder.encode(request.getPassword()));
        }

        // 생일 변경
        if (request.getBirthDate() != null) {
            user.updateBirthDate(request.getBirthDate());
        }

        // 수정 시간 업데이트
        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        user.updateInfo(now);

        return UserResponseDto.toDto(user);
    }


    // 회원 탈퇴
    @Override
    @Transactional
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException.ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        if ("Y".equals(user.getIsDeleted())) {
            throw new ServiceException.ResourceNotFoundException("이미 탈퇴한 계정입니다.");
        }
        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        user.setDeletedAt(now);
        // 물리적 삭제가 아닌 논리적 삭제 처리
        user.deleteUser();
    }


}