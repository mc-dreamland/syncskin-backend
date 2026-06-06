package net.mcbjd.domain;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Table(name = "tb_skin_player")
@Data
@EqualsAndHashCode(callSuper = false)
public class SkinPlayer extends PanacheEntityBase {
    @Id
    private String uuid;
    @OneToOne
    @JoinColumn(name = "skin_id")
    private SkinData skinData;
}
