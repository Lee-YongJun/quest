package com.example.quest.controller;

import com.example.quest.exception.TokenRefreshException;
import com.example.quest.model.entity.RefreshToken;
import com.example.quest.model.enums.ERole;
import com.example.quest.model.entity.Role;
import com.example.quest.model.entity.User;
import com.example.quest.model.network.Header;
import com.example.quest.model.network.request.MemberRequest;
import com.example.quest.model.network.response.MemberResponse;
import com.example.quest.payload.request.LoginRequest;
import com.example.quest.payload.request.SignupRequest;
import com.example.quest.payload.request.TokenRefreshRequest;
import com.example.quest.payload.response.JwtResponse;
import com.example.quest.payload.response.MessageResponse;
import com.example.quest.payload.response.TokenRefreshResponse;
import com.example.quest.repository.RoleRepository;
import com.example.quest.repository.UserRepository;
import com.example.quest.security.jwt.JwtUtils;
import com.example.quest.security.service.RefreshTokenService;
import com.example.quest.security.service.UserDetailsImpl;
import com.example.quest.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//로그
@Slf4j
//웹 페이지의 제한된 자원을 외부 도메인에서 접근을 허용하도록 하는 어노테이션
@CrossOrigin(origins = "*", maxAge = 3600)
//Restuful 웹서비스의 컨트롤러 Response Body생성
@RestController
//들어온 요청을 특정 메서드와 매핑하기 위해 사용
@RequestMapping("/quest/auth")
public class AuthController extends BaseController<MemberRequest, MemberResponse, User> {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    MemberService memberService;

    //refresh 토큰 서비스 추가
    @Autowired
    RefreshTokenService refreshTokenService;

    /**
     * 로그인
     *
     * @param loginRequest
     * @return
     */

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        //jwt refresh토큰 넣기전에 사용.
        //        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(),
                userDetails.getUsername(), userDetails.getName(), userDetails.getEmail(), userDetails.getPhone(),
                userDetails.getPostCode(), userDetails.getAddress(), userDetails.getDetailAddress(), roles));

        //refresh Token 하기전에 jwt 토큰적용.
        //        return ResponseEntity.ok(new JwtResponse(jwt,
        //                userDetails.getId(),
        //                userDetails.getUsername(),
        //                userDetails.getName(),
        //                userDetails.getEmail(),
        //                userDetails.getPhone(),
        //                userDetails.getPostCode(),
        //                userDetails.getAddress(),
        //                userDetails.getDetailAddress(),
        //                roles));

    }

    //refreshToken추가.
    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {

        //요청 데이터에서 새로고침 토큰을 얻는다.
        String requestRefreshToken = request.getRefreshToken();

        //토큰에서 refreshToken개체를 가져온다.
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "REFRESH TOKEN은 데이터베이스 안에 없습니다!"));
    }

    /**
     * 회원리스트
     *
     * @param pageable
     * @return
     */
    @GetMapping("/paging") // http://localhost:8080/quest/auth/paging?page=0
    @PreAuthorize("hasRole('ADMIN')")
    public Header<List<MemberResponse>> pagingRead(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 10) Pageable pageable) {
        log.info("{}", pageable);
        return baseService.pagingRead(pageable);
    }

    public String templateTest(String str, int num, boolean flag){
        return new String("str");
    }

    /**
     * 회원가입
     *
     * @param signUpRequest
     * @return
     */

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("에러: 이미존재하는 아이디입니다."));
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: 이미존재하는 이메일입니다!"));
        }
        // 새로운 유저의 계정생성
        User user = new User(signUpRequest.getUsername(), signUpRequest.getName(),
                encoder.encode(signUpRequest.getPassword()), signUpRequest.getEmail(), signUpRequest.getPhone(), signUpRequest.getPostCode(),
                signUpRequest.getAddress(), signUpRequest.getDetailAddress());

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("에러: 권한을 찾을 수 없습니다."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("에러: 권한을 찾을 수 없습니다."));
                        roles.add(adminRole);
                        break;

                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("에러: 권한을 찾을 수 없습니다."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("가입이 성공적으로 진행되었습니다."));
    }

    /**
     * 회원수정
     */
    @PutMapping("/modify")
    public Header<MemberResponse> update(@RequestBody MemberRequest request) {

        Header<MemberRequest> result = new Header<MemberRequest>();
        request.setPassword(encoder.encode(request.getPassword()));
        result.setData(request);
        return memberService.update(result);
    }

    /**
     * 회원삭제
     *
     * @param id
     * @return
     */
    @Override
    @DeleteMapping("{id}")
    public Header delete(@PathVariable Long id) {
        return baseService.delete(id);
    }
}
