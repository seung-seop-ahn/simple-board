package com.example.board.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PostArticleDto {
    @NotBlank(message = "Title is mandatory")
    String title;

    @NotBlank(message = "Contents is mandatory")
    String contents;
}
