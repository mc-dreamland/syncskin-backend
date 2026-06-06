package net.mcbjd.domain;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Table(name = "tb_skin_data")
@Data
@EqualsAndHashCode(callSuper = false)
public class SkinData extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    private String hash;
    private String data;
}
