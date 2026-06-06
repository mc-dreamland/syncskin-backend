package net.mcbjd;

import net.mcbjd.domain.SkinPlayer;
import io.quarkus.test.junit.QuarkusTest;
import lombok.SneakyThrows;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class MysqlTest {
    @Inject
    Mutiny.SessionFactory sf;

    @Test
    @SneakyThrows
    public void skinTest() {
        sf.withSession(session -> session.createQuery("from SkinPlayer ", SkinPlayer.class).getResultList()).await().indefinitely().forEach(s->{
            System.out.println(s.getSkinData().getData());
        });
        Thread.sleep(5000L);
    }
}
