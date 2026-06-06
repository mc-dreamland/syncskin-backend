package net.mcbjd.serialize;

import net.mcbjd.pojo.BedrockClientData;
import net.mcbjd.service.impl.GeyserWebSocketService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class BedrockClientDataDeSerialize extends JsonDeserializer<BedrockClientData> {
    @Override
    public BedrockClientData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String s = new String(unGZipBytes(p.getBinaryValue()), StandardCharsets.UTF_8);
        return GeyserWebSocketService.JSON_MAPPER.convertValue(GeyserWebSocketService.JSON_MAPPER.readTree(s),BedrockClientData.class);
    }

    private byte[] unGZipBytes(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            GZIPInputStream gzip = new GZIPInputStream(bis);
            byte[] buf = new byte[1024];
            int num = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, num);
            }
            b = baos.toByteArray();
            baos.flush();
            baos.close();
            gzip.close();
            bis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }
}
