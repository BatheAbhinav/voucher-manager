package abhinav.projects.vouchermanager.config;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.sql.SQLException;
import java.util.Map;

@WritingConverter
class MapToJsonbConverter implements Converter<Map<String, Object>, PGobject> {

    private final ObjectMapper objectMapper;

    MapToJsonbConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public PGobject convert(Map<String, Object> source) {
        try {
            PGobject jsonb = new PGobject();
            jsonb.setType("jsonb");
            jsonb.setValue(objectMapper.writeValueAsString(source));
            return jsonb;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize voucher attributes to JSONB", e);
        }
    }
}

@ReadingConverter
class JsonbToMapConverter implements Converter<PGobject, Map<String, Object>> {

    private final ObjectMapper objectMapper;

    JsonbToMapConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> convert(PGobject source) {
        try {
            return objectMapper.readValue(source.getValue(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize voucher attributes from JSONB", e);
        }
    }
}
