package net.mcbjd.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.smallrye.common.annotation.NonBlocking;
import lombok.SneakyThrows;
import net.mcbjd.pojo.SkinUploadData;
import net.mcbjd.websocket.GeyserWebSocket;
import org.jboss.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@ApplicationScoped
public class GeyserWebSocketService {
    public static final ObjectMapper JSON_MAPPER = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private static final Logger LOG = Logger.getLogger(GeyserWebSocketService.class);
    @Inject CachedService cachedService;
    @Inject SkinValidationService validation;
    @Inject GeyserWebSocket geyserWebSocket;

    // Identity tokens distinguish A -> B -> A and requests returning out of order.
    private final Cache<String, Object> latest = Caffeine.newBuilder()
            .maximumSize(5000).expireAfterAccess(Duration.ofHours(1)).build();

    @SneakyThrows
    @NonBlocking
    public SkinUploadData convertBedrockClientData(String json) {
        SkinUploadData data = JSON_MAPPER.readValue(json, SkinUploadData.class);
        data.setUuid(UUID.fromString(data.getUuid()).toString());
        Object token = begin(data.getUuid());
        try {
            // Recompute from the transported image, also supporting older uploaders.
            data.setHash(SkinHashes.fromUpload(data.getSkinData()));
            if (data.getGeometryName() != null && !data.getGeometryName().isEmpty()) {
                data.setSlim(new String(Base64.getDecoder().decode(data.getGeometryName()), StandardCharsets.UTF_8).contains("customSlim"));
            }
        } catch (IllegalArgumentException e) {
            LOG.debugf("Invalid skin upload for %s: %s", data.getUuid(), e.getMessage());
            return data;
        }
        if (SkinHashes.exempt(data.isPersonaSkin(), data.getSkinId())) {
            complete(data, token, true);
        } else {
            validation.validate(data.getHash()).subscribe().with(valid -> complete(data, token, valid),
                    failure -> LOG.warn("Skin validation failed", failure));
        }
        return data;
    }

    private synchronized Object begin(String uuid) {
        Object token = new Object();
        latest.put(uuid, token);
        return token;
    }

    private synchronized void complete(SkinUploadData data, Object token, boolean valid) {
        if (latest.getIfPresent(data.getUuid()) != token || !valid) return;
        cachedService.putImage(data.getHash(), data.getSkinData());
        SkinUploadData previous = cachedService.getPlayer(data.getUuid());
        boolean sameImage = previous != null && Objects.equals(data.getHash(), previous.getHash());
        boolean restoreFashion = previous != null && sameImage && data.getFashionName() == null
                && data.getFashionDataName() == null && !data.isWearFashion() && previous.isWearFashion();
        boolean differentFashion = previous != null && data.getFashionName() != null && data.getFashionDataName() != null
                && !data.getFashionName().equals(previous.getFashionName());
        // Preserve the existing custom-skin override and fashion restoration behavior.
        if (previous == null || restoreFashion || (!sameImage && !previous.isCustom()) || differentFashion) {
            cachedService.putPlayer(data);
            if (restoreFashion) restoreSkin(data);
        }
    }

    public synchronized void setCustomSkin(String uuid, String encodedSkin) {
        SkinUploadData previous = cachedService.getPlayer(uuid);
        if (previous == null) throw new NotFoundException("Player skin not found");
        SkinUploadData data = JSON_MAPPER.convertValue(previous, SkinUploadData.class);
        try {
            data.setHash(SkinHashes.fromUpload(encodedSkin));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
        data.setSkinData(encodedSkin);
        data.setCustom(true);
        data.setPersonaSkin(false);
        data.setSkinId(uuid + (data.isSlim() ? ".NonsyncCustomSlim" : ".NonsyncCustom"));
        begin(uuid);
        // Custom uploads are an explicit document exemption, not a positive MD5 verdict.
        cachedService.putImage(data.getHash(), data.getSkinData());
        cachedService.putPlayer(data);
        updateSkin(data);
    }

    public void restoreSkin(SkinUploadData data) {
        Map<String, Object> event = new HashMap<>();
        event.put("event_id", 8);
        event.put("uuid", data.getUuid());
        event.put("entity_id", data.getEntityId());
        event.put("username", data.getUsername());
        event.put("player_entitys", data.getPlayerEntitys());
        broadcast(event);
    }

    public void updateSkin(SkinUploadData data) {
        broadcast(Map.of("event_id", 6, "uuid", data.getUuid(), "skin_data", data.getSkinData(), "skin_hash", data.getHash()));
    }

    @SneakyThrows
    private void broadcast(Map<String, ?> event) {
        String json = JSON_MAPPER.writeValueAsString(event);
        var sessions = geyserWebSocket.getSessions();
        synchronized (sessions) {
            for (var session : sessions) {
                if (session.isOpen()) {
                    try { session.getAsyncRemote().sendText(json); }
                    catch (RuntimeException e) { LOG.debug("Skin notification could not be sent", e); }
                }
            }
        }
    }
}
