package dev.yerokha.smarttale.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Converter(autoApply = true)
@Component
public class MapToJsonConverter implements AttributeConverter<Map<String, String>, String> {

    private final ObjectMapper mapper;
    private final TypeReference<Map<String, String>> typeRef;

    public MapToJsonConverter(ObjectMapper mapper) {
        this.mapper = mapper;
        this.typeRef = new TypeReference<>() {
        };
    }

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not convert attribute to JSON", e);
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, typeRef);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not convert attribute to Map", e);
        }
    }
}
