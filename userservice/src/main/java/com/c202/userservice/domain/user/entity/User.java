package com.c202.userservice.domain.user.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long userSeq;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private String birthDate;

    @Column(nullable = true, length = 100)
    private String introduction;

    @Column(nullable = false, length = 15)
    private String createdAt;

    @Column(nullable = false, length = 15)
    private String updatedAt;

    @Column(nullable = true, length = 15)
    private String deletedAt;

    @Column(nullable = false, length = 1)
    private String isDeleted;

    // 엔티티 수정 메소드 추가
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateBirthDate(String birthDate) {this.birthDate = birthDate;}

    public void deleteUser() {
        this.isDeleted = "Y";
    }

    public void updateInfo(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void updateIntroduction(String introduction) {this.introduction = introduction;}

}