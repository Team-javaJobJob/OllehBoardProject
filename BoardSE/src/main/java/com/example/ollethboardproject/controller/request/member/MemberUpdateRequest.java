package com.example.ollethboardproject.controller.request.member;

import com.example.ollethboardproject.domain.Gender;
import com.example.ollethboardproject.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberUpdateRequest {
    private String userName;
    private String password;
    private String nickName;
    private Gender gender;
    private Role role;

}