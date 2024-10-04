package com.example.board.service;

import com.example.board.dto.PostDeviceDto;
import com.example.board.entity.Device;
import com.example.board.entity.User;
import com.example.board.repository.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceService {

    private final UserRepository userRepository;

    public DeviceService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<Device> getDeviceList(String username) throws BadRequestException {
        User user = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found."));
        return user.getDeviceList();
    }

    public Device postDevice(String username, PostDeviceDto dto) throws BadRequestException {
        User user = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found."));

        Device device = new Device();
        device.setDeviceName(dto.getDeviceName());
        device.setToken(dto.getToken());

        user.getDeviceList().add(device);

        this.userRepository.save(user);

        return device;
    }
}
