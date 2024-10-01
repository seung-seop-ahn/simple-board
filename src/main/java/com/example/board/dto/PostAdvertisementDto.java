package com.example.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostAdvertisementDto {
    @NotBlank(message = "Title is mandatory")
    String title;

    @NotBlank(message = "Contents is mandatory")
    String contents;

    private Boolean isVisible;

    @NotNull(message = "StartDate is mandatory")
    private LocalDateTime startDate;

    @NotNull(message = "EndDate is mandatory")
    private LocalDateTime endDate;
}
