package net.mcbjd;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.websocket.*;
import java.net.URI;

@QuarkusTest
public class GeyserSocketTest {

    @TestHTTPResource("/geyser")
    URI uri;


    @ClientEndpoint
    public static class Client {

        @OnOpen
        public void open(Session session) {
            System.out.println("open");
        }

        @OnMessage
        void message(String msg) {
            System.out.println("message: "+msg);
        }

    }


    @Test
    public void testWebsocketChat() throws Exception {
        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri)) {
            session.getAsyncRemote().sendObject("{\"client_data\":\"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\",\"uuid\":\"e07f8272-204e-33dc-8405-3963209abeb5\",\"xuid\":\"\"}");
//            Assertions.assertEquals("CONNECT", MESSAGES.poll(10, TimeUnit.SECONDS));
//            Assertions.assertEquals("User stu joined", MESSAGES.poll(10, TimeUnit.SECONDS));
//            session.getAsyncRemote().sendText("hello world");
//            Assertions.assertEquals(">> stu: hello world", MESSAGES.poll(10, TimeUnit.SECONDS));
        }
    }
}
