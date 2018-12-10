package com.dc.bale.component;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonConverter {
    private ObjectMapper mapper;

    public JsonConverter() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    public String toString(Object jsonObject) throws JsonProcessingException {
        return mapper.writeValueAsString(jsonObject);
    }

    public <T> T toObject(String json) throws IOException {
        return mapper.readValue(json, new TypeReference<T>() {
        });
    }

    public <T> T toObject(String json, Class<T> type) throws IOException {
        return mapper.readValue(json, type);
    }
}
