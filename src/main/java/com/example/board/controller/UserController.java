package com.example.board.controller;

import com.example.board.entity.User;
import com.example.board.config.security.CustomUserDetailsService;
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

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Autowired
    public UserController(AuthenticationManager authenticationManager, CustomUserDetailsService customUserDetailsService, JwtUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
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
    public ResponseEntity<String> signOut(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
//        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("validate/token")
    public ResponseEntity<String> validateToken(@RequestBody() ValidateTokenDto dto) {
        Boolean valid = jwtUtil.validateToken(dto.getToken());
        return valid ? ResponseEntity.ok("ok") : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        this.userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
