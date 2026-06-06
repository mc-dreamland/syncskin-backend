package net.mcbjd;

import net.mcbjd.pojo.SkinCheck;
import net.mcbjd.service.SkinCheckService;
import net.mcbjd.service.impl.CachedService;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import lombok.SneakyThrows;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class SkinRestClientTest {
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private URI uri = URI.create("https://g79mclobt.nie.netease.com/pe/web/skin-md5-valid-inner");

    Client restClientBuilder = ClientBuilder.newBuilder()
            .executorService(executorService)
            .build();
    @Test
    @SneakyThrows
    public void skinTest(){
        System.setProperty("https.protocols", "SSL");
        CompletionStage<Response> post = restClientBuilder.target(uri)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .rx()
                .post(Entity.json(new SkinCheck()));
        post.whenComplete(((response, throwable) -> {
            if (throwable != null) throwable.printStackTrace();
            System.out.println(response.getEntity());
        }));
    }

    @Inject
    @RestClient
    SkinCheckService skinCheckService;

    @Inject
    private CachedService cachedService;


    @Test
    public void checkTest(){
        Uni<SkinCheck.SkinCheckModel> check = skinCheckService.check(new SkinCheck("f1e31ca4adf0727d12b5080929d32e70"));

        check.map(s->{
            if (s.getEntities().length > 0 && s.getEntities()[0]){
                return cachedService.loadSkinHashCache("1","test");
            }
            return Uni.createFrom().nothing();
        })
                .await().indefinitely()
                .subscribe().with(b->{
            System.out.println("b" +b);
        });
    }
}
