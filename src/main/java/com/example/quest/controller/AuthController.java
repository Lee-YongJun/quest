package com.example.quest.controller;

import com.example.quest.model.ERole;
import com.example.quest.model.Role;
import com.example.quest.model.User;
import com.example.quest.payload.request.LoginRequest;
import com.example.quest.payload.request.SignupRequest;
import com.example.quest.payload.response.JwtResponse;
import com.example.quest.payload.response.MessageResponse;
import com.example.quest.repository.RoleRepository;
import com.example.quest.repository.UserRepository;
import com.example.quest.security.jwt.JwtUtils;
import com.example.quest.security.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

//웹 페이지의 제한된 자원을 외부 도메인에서 접근을 허용하도록 하는 어노테이션
@CrossOrigin(origins = "*", maxAge = 3600)
//Restuful 웹서비스의 컨트롤러 Response Body생성
@RestController
//들어온 요청을 특정 메서드와 매핑하기 위해 사용
@RequestMapping("/quest/auth")
public class AuthController {

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
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                roles));
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
                    .body(new MessageResponse("에러: 아이디가 이미 있습니다 !"));
        }
        // 새로운 유저의 계정생성
        User user = new User(signUpRequest.getUsername(),
                encoder.encode(signUpRequest.getPassword()));

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
        //test
    }
}
