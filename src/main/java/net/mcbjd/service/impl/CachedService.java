package net.mcbjd.service.impl;

import net.mcbjd.pojo.SkinUploadData;
import io.quarkus.cache.*;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CachedService {
    private static final Logger LOG = Logger.getLogger(CachedService.class);
    @CacheName("playerSkin")
    Cache playerSkin;
    @CacheName("skinCache")
    Cache skinHashCache;

    public Uni<SkinUploadData> loadPlayerSkinCache(@CacheKey String KEY, SkinUploadData clientData) {
        return playerSkin.get(KEY, k -> {
            LOG.debug("saved cache key: " + KEY);
            return clientData;
        });
    }

    /**
     * 存储皮肤缓存
     *
     * @param hash
     * @param skinData
     * @return
     */
    public Uni<String> loadSkinHashCache(@CacheKey String hash, String skinData) {
        return skinHashCache.get(hash, k -> {
            LOG.debugf("save skinHash key: " + hash);
            return skinData;
        });
    }


    public void invalidateSkin(String uuid) {
        playerSkin.as(CaffeineCache.class).invalidate(uuid).subscribe().with(s -> {
        });
    }

    public void invalidateHash(String hash) {
        skinHashCache.as(CaffeineCache.class).invalidate(hash).subscribe().with(s -> {
        });
    }

    public boolean hasSkinHash(String hash) {
        return skinHashCache.as(CaffeineCache.class).getIfPresent(hash) != null;
    }
}
