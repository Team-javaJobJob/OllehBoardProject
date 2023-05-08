package com.example.ollethboardproject.service;

import com.example.ollethboardproject.controller.request.MemberUpdateRequest;
import com.example.ollethboardproject.controller.request.MemberJoinRequest;
import com.example.ollethboardproject.controller.request.MemberLoginRequest;
import com.example.ollethboardproject.controller.request.PwEncodeRequest;
import com.example.ollethboardproject.domain.dto.MemberDTO;
import com.example.ollethboardproject.domain.entity.Member;
import com.example.ollethboardproject.domain.entity.Post;
import com.example.ollethboardproject.exception.BoardException;
import com.example.ollethboardproject.exception.ErrorCode;
import com.example.ollethboardproject.repository.CommunityRepository;
import com.example.ollethboardproject.repository.MemberRepository;
import com.example.ollethboardproject.repository.PostRepository;
import com.example.ollethboardproject.repository.OllehRepository;
import com.example.ollethboardproject.utils.ClassUtil;
import com.example.ollethboardproject.utils.JwtTokenUtil;
import com.example.ollethboardproject.utils.TokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final BCryptPasswordEncoder encoder;
    private final OllehRepository ollehRepository;
    private final PostService postService;

    @Value("${jwt.token.secret}")
    private String key;
    @Value("${jwt.access-expired}")
    private Long accessExpiredTimeMs;
    @Value("${jwt.refresh-expired}")
    private Long refreshExpiredTimeMs;

    public MemberDTO join(MemberJoinRequest memberJoinRequest) {
        // 회원가입 중복 체크
        memberRepository.findByUserName(memberJoinRequest.getUserName())
                .ifPresent(member -> {
                    throw new BoardException(ErrorCode.DUPLICATED_USERNAME, String.format("%s is duplicated", memberJoinRequest.getUserName()));
                });
        //TODO: 비밀번호 제약조건 설정 여부
        //비밀번호 암호화를 위해 pwEncodeRequest 타입으로 변환
        PwEncodeRequest pwEncodeRequest = new PwEncodeRequest(
                memberJoinRequest.getUserName(), memberJoinRequest.getNickName(), memberJoinRequest.getPassword(),
                memberJoinRequest.getGender(), memberJoinRequest.getRoles());

        //비밀번호 암호화 후 member 타입으로 객체 생성
        Member member = Member.toPw(encodePassword(pwEncodeRequest));
        //member 엔티티 저장
        Member savedMember = memberRepository.save(member);
        //entity -> DTO 로 변환후 return
        return MemberDTO.fromEntity(savedMember);
    }
    @Transactional(readOnly = true)
    public TokenInfo login(MemberLoginRequest memberLoginRequest) {
        //아이디 체크
        Member member = memberRepository.findByUserName(memberLoginRequest.getUserName())
                .orElseThrow(() -> new BoardException(ErrorCode.USER_NOT_FOUND, String.format("%s is not found", memberLoginRequest.getUserName())));

        //패스워드 확인
        if (!encoder.matches(memberLoginRequest.getPassword(), member.getPassword())) {
            throw new BoardException(ErrorCode.INVALID_TOKEN, String.format("password is invalid"));
        }

        // 토큰 발급 (엑세스 , 리프레시)
        //TODO: 각 토큰들에 대한 세부 설정
        String accessToken = JwtTokenUtil.createAccessToken(member.getUsername(), key, accessExpiredTimeMs);
        String refreshToken = JwtTokenUtil.createRefreshToken(member.getUsername(), key, refreshExpiredTimeMs);
        return TokenInfo.generateTokens(accessToken, refreshToken);
    }

//    private MemberJoinRequest encodePassword(MemberJoinRequest memberJoinRequest) {
//        //비밀번호 암호화
//        String encodePassword = encoder.encode(memberJoinRequest.getPassword());
//        memberJoinRequest.encode(encodePassword);
//        return memberJoinRequest;
//    }

    private PwEncodeRequest encodePassword(PwEncodeRequest pwEncodeRequest) {
        //비밀번호 암호화
        String encodePassword = encoder.encode(pwEncodeRequest.getPassword());
        pwEncodeRequest.encode(encodePassword);
        return pwEncodeRequest;
    }

    public MemberDTO findMemberByPassword(String password, Authentication authentication) {
        //캐스팅에 의한 에러가 나지 않도록 ClassUtil 메서드 사용
        Member member = ClassUtil.castingInstance(authentication.getPrincipal(), Member.class).get();
        //비밀번호가 일치하는 회원 정보를 조회할 수 있다.
        passwordMatches(member, password);
        //entity -> DTO 로 변환후 return
        return MemberDTO.fromEntity(member);
    }

    public MemberDTO updateMember(String password, MemberUpdateRequest memberUpdateRequest, Authentication authentication) {
        //캐스팅에 의한 에러가 나지 않도록 ClassUtil 메서드 사용
        Member member = ClassUtil.castingInstance(authentication.getPrincipal(), Member.class).get();
        //수정된 회원 정보 중복 검증
        duplicationMatches(member.getUsername());
        //비밀번호가 일치하는 회원만 회원 정보를 수정할 수 있다.
        passwordMatches(member, password);
        //수정된 비밀번호 암호화
        PwEncodeRequest pwEncodeRequest = new PwEncodeRequest(
                memberUpdateRequest.getUserName(), memberUpdateRequest.getNickName(), memberUpdateRequest.getPassword(),
                memberUpdateRequest.getGender(), memberUpdateRequest.getRole());
        //암화화된 비밀번호가 포함된 정보를 member 타입으로 객체화
        Member updatedMember = Member.toPw(encodePassword(pwEncodeRequest));
        //회원 정보 수정 (Setter 를 사용하지 않기 위함)
        member.update(updatedMember);
        //수정된 회원 정보 저장
        memberRepository.save(member);
        return MemberDTO.fromEntity(updatedMember);
    }

    public void deleteMember(String password, Authentication authentication) {
        //캐스팅에 의한 에러가 나지 않도록 ClassUtil 메서드 사용
        Member member = ClassUtil.castingInstance(authentication.getPrincipal(), Member.class).get();
        //비밀번호가 일치하는 회원만 회원 정보를 삭제할 수 있다.
        passwordMatches(member, password);
        //회원 정보 삭제
        memberRepository.deleteById(member.getId());
    }

    //비밀번호 일치 검증 메서드
    private void passwordMatches(Member member, String password) {
        //비교할 password 암호화
        String encodePassword = encoder.encode(password);
        if (encoder.matches(encodePassword, member.getPassword())) {
            throw new BoardException(ErrorCode.HAS_NOT_PERMISSION_TO_ACCESS);
        }
    }

    //수정된 회원 정보 중복 검증 메서드
    private void duplicationMatches(String updaterName) {
        memberRepository.findByUserName(updaterName).orElseThrow(() ->
                new BoardException(ErrorCode.DUPLICATED_USERNAME, String.format("%s not found", updaterName)));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberRepository.findByUserName(username).orElseThrow(() ->
                new BoardException(ErrorCode.USER_NOT_FOUND, String.format("%s not found", username)));
    }

}