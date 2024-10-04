package com.example.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PostDeviceDto {
    @NotBlank(message = "Device name is mandatory")
    private String deviceName;

    @NotBlank(message = "Device token is mandatory")
    private String token;
}
