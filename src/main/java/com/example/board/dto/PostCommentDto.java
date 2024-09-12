package com.example.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PostCommentDto {
    @NotBlank(message = "Contents is mandatory")
    String contents;
}
