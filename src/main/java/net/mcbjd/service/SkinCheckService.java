package net.mcbjd.service;

import net.mcbjd.pojo.SkinCheck;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/")
@ApplicationScoped
@RegisterRestClient(configKey = "skin-check")
public interface SkinCheckService {
    @POST
    Uni<SkinCheck.SkinCheckModel> check(SkinCheck skinCheck);
}
