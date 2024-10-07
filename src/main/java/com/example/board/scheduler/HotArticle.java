package com.example.board.scheduler;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class HotArticle implements Serializable {
    private Long id;
    private String title;
    private String contents;
    private String authorName;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Long viewCount;
}