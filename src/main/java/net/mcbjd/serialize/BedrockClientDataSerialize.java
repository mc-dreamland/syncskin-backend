package net.mcbjd.serialize;

import net.mcbjd.pojo.BedrockClientData;
import net.mcbjd.service.impl.GeyserWebSocketService;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class BedrockClientDataSerialize extends JsonSerializer<BedrockClientData> {
    @Override
    public void serialize(BedrockClientData value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

    }

    public static byte[] gZipBytes(byte[] data) {
        byte[] gZipByte = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(data);
            gzip.finish();
            gzip.close();
            gZipByte = bos.toByteArray();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gZipByte;
    }
}
