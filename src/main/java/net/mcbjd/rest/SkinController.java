package net.mcbjd.rest;

import net.mcbjd.pojo.SkinUploadData;
import net.mcbjd.service.impl.CachedService;
import net.mcbjd.service.impl.GeyserWebSocketService;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.Operation;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Optional;

@Path("/skin")
public class SkinController {

    @Inject
    CachedService cachedService;


    @Inject
    GeyserWebSocketService webSocketService;

    @GET
    @Path("/{uuid}")
    @Operation(summary = "获取玩家皮肤数据")
    public Uni<SkinUploadData> getSkinData(@PathParam("uuid") String uuid) {
        return cachedService.loadPlayerSkinCache(uuid, null);
    }

    @PUT
    @Path("/{uuid}")
    @Operation(summary = "修改玩家皮肤数据")
    public Uni<Boolean> updateSkin(@PathParam("uuid") String uuid, SkinUploadData uploadData) {
        return Uni.createFrom().item(() -> {
            webSocketService.setCustomSkin(uuid, uploadData.getSkinData());
            return true;
        });
    }

    @GET
    @Path("/hash/{hash}")
    @Operation(summary = "通过hash获取皮肤数据")
    public Uni<String> getHashSkinData(@PathParam("hash") String hash) {
        return Uni.createFrom().item(() -> Optional.ofNullable(cachedService.getImage(hash)).orElse(""));
    }
}
