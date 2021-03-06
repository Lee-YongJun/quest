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

//??????
@Slf4j
//??? ???????????? ????????? ????????? ?????? ??????????????? ????????? ??????????????? ?????? ???????????????
@CrossOrigin(origins = "*", maxAge = 3600)
//Restuful ??????????????? ???????????? Response Body??????
@RestController
//????????? ????????? ?????? ???????????? ???????????? ?????? ??????
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

    //refresh ?????? ????????? ??????
    @Autowired
    RefreshTokenService refreshTokenService;

    /**
     * ?????????
     *
     * @param loginRequest
     * @return
     */

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(),
                userDetails.getUsername(), userDetails.getName(), userDetails.getEmail(), userDetails.getPhone(),
                userDetails.getPostCode(), userDetails.getAddress(), userDetails.getDetailAddress(), roles));
    }

    //refreshToken??????.
    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {

        //?????? ??????????????? ???????????? ????????? ?????????.
        String requestRefreshToken = request.getRefreshToken();

        //???????????? refreshToken????????? ????????????.
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "REFRESH TOKEN??? ?????????????????? ?????? ????????????!"));
    }

    /**
     * ???????????????
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

    /**
     * ????????????
     *
     * @param signUpRequest
     * @return
     */

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("??????: ?????????????????? ??????????????????."));
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: ?????????????????? ??????????????????!"));
        }
        // ????????? ????????? ????????????
        User user = new User(signUpRequest.getUsername(), signUpRequest.getName(),
                encoder.encode(signUpRequest.getPassword()), signUpRequest.getEmail(), signUpRequest.getPhone(), signUpRequest.getPostCode(),
                signUpRequest.getAddress(), signUpRequest.getDetailAddress());

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("??????: ????????? ?????? ??? ????????????."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("??????: ????????? ?????? ??? ????????????."));
                        roles.add(adminRole);
                        break;

                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("??????: ????????? ?????? ??? ????????????."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("????????? ??????????????? ?????????????????????."));
    }

    /**
     * ????????????
     */
    @PutMapping("/modify")
    public Header<MemberResponse> update(@RequestBody MemberRequest request) {

        Header<MemberRequest> result = new Header<MemberRequest>();
        request.setPassword(encoder.encode(request.getPassword()));
        result.setData(request);
        return memberService.update(result);
    }

    /**
     * ????????????
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
