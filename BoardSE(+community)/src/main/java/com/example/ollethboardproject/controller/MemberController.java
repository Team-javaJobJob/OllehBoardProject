package com.example.ollethboardproject.controller;

import com.example.ollethboardproject.controller.request.MemberUpdateRequest;
import com.example.ollethboardproject.controller.request.MemberJoinRequest;
import com.example.ollethboardproject.controller.request.MemberLoginRequest;
import com.example.ollethboardproject.controller.response.MemberJoinResponse;
import com.example.ollethboardproject.controller.response.MemberLoginResponse;
import com.example.ollethboardproject.controller.response.Response;
import com.example.ollethboardproject.domain.dto.MemberDTO;
import com.example.ollethboardproject.service.MemberService;
import com.example.ollethboardproject.utils.TokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    //회원가입
    @PostMapping("/join")
    public Response<MemberJoinResponse> join(@RequestBody MemberJoinRequest memberJoinRequest) {
        MemberDTO memberDTO = memberService.join(memberJoinRequest);
        return Response.success(MemberJoinResponse.fromUserDTO(memberDTO));
    }

    //로그인
    @PostMapping("/login")
    public Response<MemberLoginResponse> login(@RequestBody MemberLoginRequest memberLoginRequest) {
        TokenInfo tokens = memberService.login(memberLoginRequest);
        return Response.success(new MemberLoginResponse(tokens.getAccessToken(), tokens.getRefreshToken()));
    }

    //회원 정보 조회
    @GetMapping("{password}")
    public ResponseEntity<MemberDTO> findMemberById(@PathVariable String password, Authentication authentication) {
        log.info("GET /api/v1/members/{}", password);
        MemberDTO memberDTO = memberService.findMemberByPassword(password, authentication);
        return new ResponseEntity<>(memberDTO, HttpStatus.OK);
    }

    //회원 정보 수정
    @PutMapping("{password}")
    public ResponseEntity<MemberDTO> updateMember(@PathVariable String password, @RequestBody MemberUpdateRequest memberUpdateRequest , Authentication authentication) {
        log.info("PUT /api/v1/members/{}", password);
        MemberDTO updatedMemberDTO = memberService.updateMember(password, memberUpdateRequest, authentication);
        return new ResponseEntity<>(updatedMemberDTO, HttpStatus.OK);
    }

    //회원 정보 삭제
    @DeleteMapping("{password}")
    public ResponseEntity<Void> deleteMember(@PathVariable String password, Authentication authentication) {
        log.info("DELETE /api/v1/members/{}", password);
        memberService.deleteMember(password, authentication);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}