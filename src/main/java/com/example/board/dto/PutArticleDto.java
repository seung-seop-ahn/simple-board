package com.example.board.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PutArticleDto {
    String title;
    String contents;
}
