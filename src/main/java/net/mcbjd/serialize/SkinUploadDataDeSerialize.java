package net.mcbjd.serialize;

import net.mcbjd.pojo.SkinUploadData;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.jbosslog.JBossLog;

import java.io.IOException;

@JBossLog
public class SkinUploadDataDeSerialize extends JsonDeserializer<SkinUploadData> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public SkinUploadData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        SkinUploadData skinUploadData = mapper.convertValue(mapper.readTree(p.getValueAsString()), SkinUploadData.class);
        skinUploadData.setUid(skinUploadData.initUid());
        return skinUploadData;
    }
}
