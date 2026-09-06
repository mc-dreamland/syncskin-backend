package net.mcbjd.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.smallrye.mutiny.Uni;
import net.mcbjd.pojo.SkinCheck;
import net.mcbjd.service.SkinCheckService;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SkinValidationService {
    @Inject @RestClient
    SkinCheckService checkService;

    // Only a successful NetEase response may populate this cache.
    private final Cache<String, Boolean> valid = Caffeine.newBuilder()
            .maximumSize(5000).expireAfterAccess(Duration.ofHours(1)).build();
    private final ConcurrentHashMap<String, CompletableFuture<Boolean>> pending = new ConcurrentHashMap<>();

    public Uni<Boolean> validate(String hash) {
        if (hash == null || !hash.matches("[0-9a-fA-F]{32}")) return Uni.createFrom().item(false);
        String key = hash.toLowerCase(Locale.ROOT);
        if (valid.getIfPresent(key) != null) return Uni.createFrom().item(true);
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        CompletableFuture<Boolean> existing = pending.putIfAbsent(key, result);
        if (existing != null) return Uni.createFrom().completionStage(existing);
        try {
            checkService.check(new SkinCheck(key))
                    .ifNoItem().after(Duration.ofSeconds(10)).fail()
                    .map(SkinValidationService::accepted)
                    .onFailure().recoverWithItem(false)
                    .subscribe().with(accepted -> finish(key, result, accepted), failure -> finish(key, result, false));
        } catch (RuntimeException e) {
            finish(key, result, false);
        }
        return Uni.createFrom().completionStage(result);
    }

    private void finish(String key, CompletableFuture<Boolean> result, boolean accepted) {
        if (accepted) valid.put(key, true);
        pending.remove(key, result);
        result.complete(accepted);
    }

    static boolean accepted(Response response) {
        if (response == null) return false;
        try (response) {
            if (response.getStatus() != 200) return false;
            SkinCheck.SkinCheckModel model = response.readEntity(SkinCheck.SkinCheckModel.class);
            return model != null && Integer.valueOf(0).equals(model.getCode())
                    && model.getEntities() != null && model.getEntities().length == 1
                    && Boolean.TRUE.equals(model.getEntities()[0]);
        }
    }
}
