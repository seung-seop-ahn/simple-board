package com.example.board.controller;

import com.example.board.entity.User;
import com.example.board.config.security.CustomUserDetailsService;
import com.example.board.service.UserService;
import com.example.board.util.JwtUtil;
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
    public ResponseEntity<String> signIn(@Valid @RequestBody SignInDto dto) throws AuthenticationException {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(dto.getUsername());
        return ResponseEntity.ok(jwtUtil.generateToken(userDetails.getUsername()));
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
