package net.mcbjd.service.impl;

import net.mcbjd.pojo.SkinCheck;
import net.mcbjd.pojo.SkinUploadData;
import net.mcbjd.service.SkinCheckService;
import net.mcbjd.websocket.GeyserWebSocket;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.SneakyThrows;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.*;

@ApplicationScoped
@JBossLog
public class GeyserWebSocketService {
    public static final ObjectMapper JSON_MAPPER = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private static final Logger LOG = Logger.getLogger(GeyserWebSocketService.class);
    private static final Set<String> BLACKLISTED_HASH = Set.of("29a905f1a6c1e7ac2d79e5412c65e427", "15ef3edf39d4501a5bfe214b0f3b1151");
    @Inject
    CachedService cachedService;

    @Inject
    @RestClient
    SkinCheckService checkService;

    @Inject
    GeyserWebSocket geyserWebSocket;

    @SneakyThrows
    @NonBlocking
    public SkinUploadData convertBedrockClientData(String json) {
        SkinUploadData skinUploadData = JSON_MAPPER.convertValue(JSON_MAPPER.readTree(json), SkinUploadData.class);
        String geometryName = skinUploadData.getGeometryName();
        if (!StringUtil.isNullOrEmpty(geometryName)) {
            skinUploadData.setSlim(new String(Base64.getDecoder().decode(geometryName)).contains("customSlim"));
        }
        if (Objects.nonNull(skinUploadData.getHash()) && !cachedService.hasSkinHash(skinUploadData.getHash())) {
            // PC
            if (!skinUploadData.getUuid().startsWith("00000000")) {
                saveSkin(skinUploadData);
                LOG.debugf("save pc skin: %s(%s)", skinUploadData.getUsername(), skinUploadData.getUuid());
            } else {
                if (skinUploadData.getHash() == null) {
                    LOG.debugf("illegal skin#1: %s", json);
                    return skinUploadData;
                }
                // PE
                checkService.check(new SkinCheck(skinUploadData.getHash().trim())).onFailure().recoverWithNull().subscribe().with(check -> {
                    if (check == null) return;
                    // 判断是否为合法 hash
                    LOG.debugf("%s hash: %s ,SkinCheckPojo: %s %s, %s", skinUploadData.getUuid(), skinUploadData.getHash(),
                            check,
                            check.getEntities() == null, check.getCode() == 13);
                    if (check.getCode() == 13 || check.getEntities() == null) {
                        LOG.debugf("illegal skin#2: %s", json);
                        return;
                    }
                    if (check.getEntities().length > 0 && !BLACKLISTED_HASH.contains(skinUploadData.getHash())) {
                        LOG.debugf("uuid: %s hash: %s result: %s", skinUploadData.getUuid(), skinUploadData.getHash(), check.getEntities()[0]);
                        // 保存合法的皮肤 hash
                        saveSkin(skinUploadData);
                    } else {
                        LOG.debugf("illegal skin#3: %s", json);
                    }
                });
            }
            return skinUploadData;
        }
        this.saveSkinCache(skinUploadData);
        return skinUploadData;
    }

    private void saveSkin(SkinUploadData skinUploadData) {
        this.cachedService.loadSkinHashCache(skinUploadData.getHash(), skinUploadData.getSkinData())
//                .call(s -> saveMysqlSkin(skinUploadData))
                .subscribe().with(s -> this.saveSkinCache(skinUploadData));
    }

//    @Inject
//    Mutiny.SessionFactory sf;
//
//    private Uni<Void> saveMysqlSkin(SkinUploadData skinUploadData) {
//        SkinData skinData = new SkinData();
//        skinData.setHash(skinUploadData.getHash());
//        skinData.setData(skinUploadData.getSkinData());
//        return sf.withTransaction(session -> session.persist(skinData)).call(__ -> {
//            SkinPlayer skinPlayer = new SkinPlayer();
//            skinPlayer.setUuid(skinUploadData.getUuid());
//            skinPlayer.setSkinData(skinData);
//            return sf.withTransaction(session -> session.persist(skinPlayer));
//        }).onFailure().recoverWithNull().call(__ -> sf.withSession(session -> session.createQuery("from SkinData where :hash = hash", SkinData.class).setParameter("hash", skinUploadData.getHash()).getSingleResult().call(data -> session.createQuery("update SkinPlayer player set player.skinData.id = :id where player.uuid = :uuid").setParameter("id", data.getId()).setParameter("uuid", skinUploadData.getUuid()).executeUpdate())));
//    }

    private void saveSkinCache(SkinUploadData skinUploadData) {
        cachedService.loadPlayerSkinCache(skinUploadData.getUuid(), skinUploadData).subscribe().with(a -> {
            if (a == null
                    // skinuploadata 默认 wear 为 true
                    // 没有穿时装 handleBedrockSkin
                    // 但实际上缓存是穿了的
                    // 默认进服都是没穿时装
                    // 只有拿到之后...
                    || (equalsHash(skinUploadData.getSkinData(), a.getSkinData()) && Objects.isNull(skinUploadData.getFashionName()) && Objects.isNull(skinUploadData.getFashionDataName()) && !skinUploadData.isWearFashion() && a.isWearFashion()) || (!equalsHash(skinUploadData.getSkinData(), a.getSkinData()) && !a.isCustom()) || (skinUploadData.getFashionName() != null && skinUploadData.getFashionDataName() != null && !skinUploadData.getFashionName().equals(a.getFashionName()))) {
                // 刷新 skinCache
                cachedService.invalidateSkin(skinUploadData.getUuid());
                cachedService.loadPlayerSkinCache(skinUploadData.getUuid(), skinUploadData).subscribe().with(b -> {
//                    if (Objects.nonNull(b.getFashionName()) && Objects.nonNull(b.getFashionDataName())) {
//                        Map<String, Object> event_id = Map.of(
//                                "event_id", 7,
//                                "uuid", b.getUuid(),
//                                "fashion_name", b.getFashionName(),
//                                "fashion_data_name", b.getFashionDataName(),
//                                "player_entitys", b.getPlayerEntitys(),
//                                "entity_id", b.getEntityId(),
//                                "xuid", b.getXuid(),
//                                "username", b.getUsername()
//                        );
//                        this.geyserWebSocket.getSessions().forEach(session -> session.getAsyncRemote().sendObject(Unchecked.function(JSON_MAPPER::writeValueAsString).apply(event_id)));
//                    } else
                    if (a != null && b != null && equalsHash(skinUploadData.getSkinData(), a.getSkinData()) && Objects.isNull(skinUploadData.getFashionName()) && Objects.isNull(skinUploadData.getFashionDataName()) && !skinUploadData.isWearFashion() && a.isWearFashion()) {
                        this.restoreSkin(b);
                        LOG.debug("refresh cached! " + b.getUuid());
                    }
                });
            }
        });
    }

    public void restoreSkin(SkinUploadData skinUploadData) {
        log.debugf("skinUploadData: %s", skinUploadData);
        Map<String, Object> restore_skin = Map.of("event_id", 8, "uuid", skinUploadData.getUuid(), "entity_id", skinUploadData.getEntityId(), "username", skinUploadData.getUsername(), "player_entitys", skinUploadData.getPlayerEntitys());
        this.geyserWebSocket.getSessions().forEach(session -> session.getAsyncRemote().sendObject(Unchecked.function(JSON_MAPPER::writeValueAsString).apply(restore_skin)));
    }

    public void updateSkin(SkinUploadData skinUploadData) {
        Map<String, Object> updateSkin = Map.of("event_id", 6, "uuid", skinUploadData.getUuid(), "skin_data", skinUploadData.getSkinData(), "skin_hash", skinUploadData.getHash());
        this.geyserWebSocket.getSessions().forEach(session -> session.getAsyncRemote().sendObject(Unchecked.function(JSON_MAPPER::writeValueAsString).apply(updateSkin)));
    }

    private boolean equalsHash(String a, String b) {
        return hash(Optional.ofNullable(a).orElse("")).equalsIgnoreCase(hash(Optional.ofNullable(b).orElse("")));
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (Exception e) {
            return "";
        }
    }
}
