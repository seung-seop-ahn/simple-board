package com.example.board.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidateTokenDto {
    @NotBlank(message = "Token is mandatory")
    private String token;
}
