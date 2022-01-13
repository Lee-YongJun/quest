package com.example.quest.security.jwt;

import com.example.quest.security.service.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
//2.
//OncePerRequestFilter로부터 상속받은객체
//로그인할때 객체생성 Security의 내장객체(UserDetails)가지고 인증수행.
public class TokenFilter extends OncePerRequestFilter {
    //  Autowired: 필요한 의존 객체의 “타입"에 해당하는 bean을 찾아 주입한다.
    @Autowired
    //Token검사
    private JwtUtils jwtUtils;

    @Autowired
    //인증정보를 검색하고 존재하지않으면 exception 존재하면 객체 반환.
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(TokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            //token검사
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String userId = jwtUtils.getUserIdFromJwtToken(jwt);
                //security 내장객체 사용(UserDetails)
                UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
                //실제 인증 작업을 수행해서 이상유무를 판단 후, 이상이 없으면 Authentication을 생성합니다
                //이부분도 security 내장객체에서 수행.
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("사용자인증을 설정할 수 없습니다.: {}", e);
        }

        filterChain.doFilter(request, response);
    }
    //로그인 정보에 대한 Token을 “Bearer”라는 접두사를 달았고, 접두사를 제외한 나머지 정보를 가져온다.
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7, headerAuth.length());
        }

        return null;
    }
}
