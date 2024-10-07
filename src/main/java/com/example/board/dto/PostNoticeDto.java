package com.example.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PostNoticeDto {
    @NotBlank(message = "Title is mandatory")
    String title;

    @NotBlank(message = "Contents is mandatory")
    String contents;
}
