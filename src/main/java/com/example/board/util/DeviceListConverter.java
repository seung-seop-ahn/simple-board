package com.example.board.util;

import com.example.board.entity.Device;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class DeviceListConverter implements AttributeConverter<List<Device>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Device> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attributes);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not convert string list to json:", e);
        }
    }

    @Override
    public List<Device> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<Device>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not convert json to string list:", e);
        }
    }
}
