package com.example.quest.security;

import com.example.quest.security.jwt.AuthEntryPointJwt;
import com.example.quest.security.jwt.TokenFilter;
import com.example.quest.security.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//1개 이상 Bean을 등록하고 있음을 명시하는 어노테이션
@Configuration
//웹 보안을 활성화
@EnableWebSecurity
//EnableGlobalMethodSecurity
//MethodSecurity용 설정이 따로 필요할때 사용.
//1.securedEnabled
//Secured 애노테이션을 사용하여 인가 처리를 하고 싶을때 사용하는 옵션이다. 기본값은 false
//2.prePostEnabled
//PreAuthorize, PostAuthorize 애노테이션을 사용하여 인가 처리를 하고 싶을때 사용하는 옵션이다.기본값은 false
//3.jsr250Enabled
//RolesAllowed 애노테이션을 사용하여 인가 처리를 하고 싶을때 사용하는 옵션이다.기본값은 false
@EnableGlobalMethodSecurity(prePostEnabled = true)
//WebSecurityConfigurerAdapter : 스프링 시큐리티의 전반적인 보안 기능 초기화 및 설정 담당.
//WebSecurityConfig에서 실제 구현.
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    //  Autowired: 필요한 의존 객체의 “타입"에 해당하는 빈을 찾아 주입한다.
    //  생성자 (스프링 4.3부터는 생략 가능)
    //  Setter
    //  필드
    @Autowired
    //스프링시큐리티 인증시에 사용.
    //인증정보를 검색하고 존재하지않으면 exception 존재하면 객체 반환.
    UserDetailsServiceImpl userDetailsService;
    //인증인가 예외발생시 처리.
    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;
    //Bean생성해주는 역할
    //OncePerRequestFilter로부터 상속받은객체, 직접 선언
    //로그인할때 객체생성 Security의 내장객체(UserDetails)가지고 인증수행.
    @Bean
    public TokenFilter jwtTokenFilter() {
        return new TokenFilter();
    }
    //AuthenticationManagerBuilder를 통해 인증객체 만들어줌.
    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }
    //Bean생성해주는 역할
    //인증 수행을 위한 동작을 나타냄.
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    //Bean생성해주는 역할
    //Security 내장객체로 비밀번호 암호화 담당.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    //1. 접근하는 페이지가 어떤페이지인지 설정.
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //CORS는 도메인 또는 포트가 다른 서버의 자원을 요청하는 매커니즘
        //cors(): cors설정
        //csrf(): jwt토큰 사용하므로 csrf설정 disable()처리
        http.cors().and().csrf().disable()
                //예외처리 작동
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                //세션 정책 설정.
                //ALWAYS: 스프링 시큐리티가 항상 세션을 생성
                //IF_REQUIRED: 스프링 시큐리티가 필요시 생성
                //NEVER: 스프링 시큐리티가 생성하지 않지만 기존에 존재하면 사용.
                //STATELESS : JWT 토큰방식 쓸때 사용하는 설정.
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                //authorizeRequest:HttpServletRequest이용
                //antMatchers:특정경로 지정
                //permitAll:모든 사용자 접근 허용.
                //hasRole(): 시스템상 특정권한 가진 사람만 접근가능.
                .authorizeRequests().antMatchers("/quest/auth/**").permitAll()
                .antMatchers("/quest/test/**").permitAll()
                //모든 리소스를 의미하며 접근허용 리소스 및 인증후 특정 레벨의 권한을 가진 사용자만 접근가능한 리소스를 설정하고 그외 나머지 리소스들은 무조건 인증을 완료해야 접근이 가능하다.
                .anyRequest().authenticated();
        //UsernamePasswordAuthenticationFilter 전에 jwtTokenFilter 를 적용시킨다.
        http.addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
