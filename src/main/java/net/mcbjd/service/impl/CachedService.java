package net.mcbjd.service.impl;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CaffeineCache;
import io.smallrye.mutiny.Uni;
import net.mcbjd.pojo.SkinUploadData;
import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class CachedService {
    @CacheName("playerSkin")
    Cache playerSkin;
    @CacheName("skinCache")
    Cache skinHashCache;

    public Uni<SkinUploadData> loadPlayerSkinCache(String uuid, SkinUploadData data) {
        // Reads must never insert null/negative cache entries.
        return data == null ? Uni.createFrom().item(() -> getPlayer(uuid)) : playerSkin.get(uuid, key -> data);
    }

    public SkinUploadData getPlayer(String uuid) {
        CompletableFuture<SkinUploadData> value = playerSkin.as(CaffeineCache.class).getIfPresent(uuid);
        return value == null ? null : value.getNow(null);
    }

    public void putPlayer(SkinUploadData data) {
        playerSkin.as(CaffeineCache.class).put(data.getUuid(), CompletableFuture.completedFuture(data));
    }

    public Uni<String> loadSkinHashCache(String hash, String data) {
        return data == null ? Uni.createFrom().item(() -> getImage(hash)) : skinHashCache.get(hash, key -> data);
    }

    public void putImage(String hash, String data) {
        skinHashCache.as(CaffeineCache.class).put(hash, CompletableFuture.completedFuture(data));
    }

    public String getImage(String hash) {
        CompletableFuture<String> value = skinHashCache.as(CaffeineCache.class).getIfPresent(hash);
        return value == null ? null : value.getNow(null);
    }

    public void invalidateSkin(String uuid) { playerSkin.invalidate(uuid).subscribe().with(ignored -> { }); }
    public void invalidateHash(String hash) { skinHashCache.invalidate(hash).subscribe().with(ignored -> { }); }

    // Data presence is not a NetEase validation result. See SkinValidationService.
    public boolean hasSkinHash(String hash) { return getImage(hash) != null; }
}
