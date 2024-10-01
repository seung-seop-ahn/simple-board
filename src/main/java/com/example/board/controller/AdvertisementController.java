package com.example.board.controller;

import com.example.board.dto.PostAdvertisementDto;
import com.example.board.entity.Advertisement;
import com.example.board.service.AdvertisementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advertisements")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @Autowired
    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @PostMapping()
    public ResponseEntity<Advertisement> postAdvertisement(@Valid @RequestBody PostAdvertisementDto dto) {
        // todo: authentication
        Advertisement advertisement = this.advertisementService.postAdvertisement(dto);
        return ResponseEntity.ok(advertisement);
    }

    @GetMapping()
    public ResponseEntity<List<Advertisement>> getAdvertisementList() {
        List<Advertisement> list = this.advertisementService.getAdvertisementList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Advertisement> getAdvertisement(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam(required = false) Boolean isTrueView
    ) throws BadRequestException {
        String ip = this.getClientIp(request);
        String username = "";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principle = authentication.getPrincipal();
        if(!principle.equals("anonymousUser")) {
            UserDetails userDetails = (UserDetails) principle;
            username = userDetails.getUsername();
        }

        Advertisement advertisement = this.advertisementService.getAdvertisement(id, username, ip, isTrueView);
        return ResponseEntity.ok(advertisement);
    }

    @GetMapping("/{id}/click")
    public ResponseEntity<Void> clickAdvertisement(HttpServletRequest request, @PathVariable Long id) {
        String ip = this.getClientIp(request);
        String username = "";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principle = authentication.getPrincipal();
        if(!principle.equals("anonymousUser")) {
            UserDetails userDetails = (UserDetails) principle;
            username = userDetails.getUsername();
        }

        this.advertisementService.clickAdvertisement(id, username, ip);
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
