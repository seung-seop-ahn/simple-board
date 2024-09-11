package com.example.board.controller;

import com.example.board.config.security.CustomUserDetailsService;
import com.example.board.dto.SignInDto;
import com.example.board.dto.SignUpDto;
import com.example.board.dto.ValidateTokenDto;
import com.example.board.entity.User;
import com.example.board.service.TokenBlacklistService;
import com.example.board.service.UserService;
import com.example.board.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;

    @Autowired
    public UserController(AuthenticationManager authenticationManager, CustomUserDetailsService customUserDetailsService, JwtUtil jwtUtil, UserService userService, TokenBlacklistService tokenBlacklistService) {
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @GetMapping("")
    public ResponseEntity<List<User>> getUserList() {
        List<User> list = userService.getUserList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("sign-up")
    public ResponseEntity<User> signUp(@Valid @RequestBody SignUpDto dto) {
        User user = userService.signUp(dto);
        return ResponseEntity.ok(user);
    }

    @PostMapping("sign-in")
    public ResponseEntity<String> signIn(@Valid @RequestBody SignInDto dto, HttpServletResponse response) throws AuthenticationException {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(dto.getUsername());

        String token = jwtUtil.generateToken(userDetails.getUsername());

        Cookie cookie = new Cookie("token", token);
//        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // 1 hour

        response.addCookie(cookie);

        return ResponseEntity.ok(token);
    }

    @PostMapping("sign-out")
    public ResponseEntity<String> signOut(
            @RequestBody(required = false) ValidateTokenDto requestTokenDto,
            @CookieValue("token") String cookieToken,
            HttpServletResponse response
    ) {
        String token = cookieToken != null ? cookieToken : requestTokenDto.getToken();
        LocalDateTime expireDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        String username = jwtUtil.getUsernameFromToken(token);
        this.tokenBlacklistService.addBlacklist(token, expireDate, username);

        Cookie cookie = new Cookie("token", null);
//        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("validate/token")
    public ResponseEntity<String> validateToken(@Valid @RequestBody() ValidateTokenDto dto) {
        Boolean valid = jwtUtil.validateToken(dto.getToken());
        return valid ? ResponseEntity.ok("ok") : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        this.userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
