package com.dc.bale.component;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class JsonConverter {
    private ObjectMapper mapper;

    public JsonConverter() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public String toString(Object jsonObject) throws JsonProcessingException {
        return mapper.writeValueAsString(jsonObject);
    }

    public <T> List<T> toList(String json, Class<T> type) throws IOException {
        CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class, type);
        return mapper.readValue(json, collectionType);
    }
}
