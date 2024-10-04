package com.example.board.pojo;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

public abstract class Notification implements Serializable {
    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}