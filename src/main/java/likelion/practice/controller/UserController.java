//UserController
package likelion.practice.controller;

import likelion.practice.dto.UserDTO;
import likelion.practice.entity.User;
import likelion.practice.security.JwtTokenProvider;
import likelion.practice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    // 회원가입 API
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserDTO userDTO) {
        User registeredUser = userService.registerUser(userDTO);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    // 로그인 API
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody UserDTO userDTO) {
        try {
            // 사용자 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDTO.getUserId(), userDTO.getPassword())
            );

            // 인증 성공 시 JWT 토큰 생성
            String jwtToken = jwtTokenProvider.createToken(authentication.getName());

            // 응답으로 토큰을 Map 형태로 반환
            Map<String, String> response = new HashMap<>();
            response.put("token", jwtToken);

            return ResponseEntity.ok(response);

        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }
    }

    // 아이디 중복 조회 API
    @GetMapping("/check-id")
    public ResponseEntity<String> checkUserIdDuplicate(@RequestParam String userId) {
        boolean isDuplicate = userService.checkUserIdDuplicate(userId);
        if (isDuplicate) {
            return ResponseEntity.ok("해당 아이디가 이미 존재합니다.");
        } else {
            return ResponseEntity.ok("사용 가능한 아이디입니다.");
        }
    }

    // 마이 페이지 조회 API
    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(Authentication authentication) {
        String userId = authentication.getName();
        User user = userService.getUserProfile(userId);
        return ResponseEntity.ok(user);
    }

    // 마이 페이지 수정 API
    @PutMapping("/profile")
    public ResponseEntity<User> updateUserProfile(Authentication authentication, @RequestBody UserDTO updatedUser) {
        String userId = authentication.getName();
        User user = userService.updateUserProfile(userId, updatedUser);
        return ResponseEntity.ok(user);
    }
}
