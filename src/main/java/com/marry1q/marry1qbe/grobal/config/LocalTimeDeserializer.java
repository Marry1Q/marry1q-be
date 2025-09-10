package com.marry1q.marry1qbe.grobal.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.getCurrentToken();
        
        // 문자열 형태로 온 경우 (예: "14:00")
        if (token == JsonToken.VALUE_STRING) {
            String timeStr = p.getValueAsString();
            try {
                return LocalTime.parse(timeStr, TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                // ISO 형식도 시도
                return LocalTime.parse(timeStr);
            }
        }
        
        // JSON 객체 형태로 온 경우 (예: {"hour": 14, "minute": 0})
        if (token == JsonToken.START_OBJECT) {
            JsonNode node = p.getCodec().readTree(p);
            
            int hour = node.has("hour") ? node.get("hour").asInt() : 0;
            int minute = node.has("minute") ? node.get("minute").asInt() : 0;
            int second = node.has("second") ? node.get("second").asInt() : 0;
            int nano = node.has("nano") ? node.get("nano").asInt() : 0;
            
            return LocalTime.of(hour, minute, second, nano);
        }
        
        throw new IOException("LocalTime 역직렬화 실패: 지원하지 않는 형식입니다.");
    }
}
