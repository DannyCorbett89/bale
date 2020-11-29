package com.dc.bale.component;

import com.dc.bale.exception.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonMapper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public <T> T toObject(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new JsonMappingException(e);
        }
    }
}
