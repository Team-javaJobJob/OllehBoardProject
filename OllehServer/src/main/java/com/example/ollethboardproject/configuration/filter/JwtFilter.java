package com.example.ollethboardproject.configuration.filter;

import com.example.ollethboardproject.domain.entity.Member;
import com.example.ollethboardproject.service.MemberService;
import com.example.ollethboardproject.utils.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    public static final String BEARER = "Bearer ";
    private final MemberService memberService;
    private final String secretKey;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.info("authorization : {}", authorization);

        // 토큰 유효성 검증
        if (authorization == null || !authorization.startsWith(BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }
        // 토큰 추출
        String accessToken = authorization.split(" ")[1];


        // 토큰 만료 기간 검증
        try {
            if (JwtTokenUtil.isExpired(accessToken, secretKey)) {
                log.error("token is invalid");
                filterChain.doFilter(request, response);
                return;
            }

            String userName = JwtTokenUtil.getUserName(accessToken, secretKey);
            log.info("username : {}", userName);

            // MemberEntity 객체 생성
            Member member = (Member) memberService.loadUserByUsername(userName);

            JwtFilter.saveAuthentication(request, member);


        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT Token", e);
            filterChain.doFilter(request, response);
            return;
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT Token", e);
            filterChain.doFilter(request, response);
            return;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT Token", e);
            filterChain.doFilter(request, response);
            return;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty.", e);
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);

    }

    public static void saveAuthentication(HttpServletRequest request, Member member) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(member,
                null, member.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

}
