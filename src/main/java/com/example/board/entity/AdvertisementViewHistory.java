package com.example.board.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "ad_view_history")
@Getter
@Setter
@NoArgsConstructor
public class AdvertisementViewHistory {

    @Id
    private String id;

    private Long advertisementId;

    private String username;

    private String ip;

    private Boolean isTrueView = false;

    private LocalDateTime createdDate = LocalDateTime.now();
}
