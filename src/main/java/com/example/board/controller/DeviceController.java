package com.example.board.controller;

import com.example.board.dto.PostDeviceDto;
import com.example.board.entity.Device;
import com.example.board.service.DeviceService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping()
    public ResponseEntity<List<Device>> getDeviceList() throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        List<Device> list = this.deviceService.getDeviceList(userDetails.getUsername());
        return ResponseEntity.ok(list);
    }

    @PostMapping()
    public ResponseEntity<Device> postDevice(@RequestBody PostDeviceDto dto) throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Device device = this.deviceService.postDevice(userDetails.getUsername(), dto);
        return ResponseEntity.ok(device);
    }

}
