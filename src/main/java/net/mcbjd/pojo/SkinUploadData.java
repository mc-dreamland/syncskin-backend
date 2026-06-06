package net.mcbjd.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.jbosslog.JBossLog;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
@Data
@NoArgsConstructor
@JBossLog
public class SkinUploadData {
    private String xuid;
    private String uuid;
    private String username;
    private boolean slim;
//    @JsonProperty(value = "client_data")
//    @JsonDeserialize(using = BedrockClientDataDeSerialize.class)
//    private BedrockClientData clientData;
    @JsonProperty("skin_data")
    private String skinData;
    @JsonProperty("geometry_data")
    private String geometryData;
    @JsonProperty("geometry_name")
    private String geometryName;
    @JsonProperty("skin_id")
    private String skinId;
    @JsonProperty("fashion_name")
    private String fashionName;
    @JsonProperty("fashion_data_name")
    private String fashionDataName;
    @JsonProperty("player_entitys")
    private List<UUID> playerEntitys;
    @JsonProperty("wear_fashion")
    private boolean wearFashion = true;
    @JsonProperty("entity_id")
    private int entityId;
    @JsonProperty("uid")
    private long uid;
    private String hash;
    @JsonIgnore
    private boolean custom;

    public int initUid() {
        try {
            int i = uuid.replace("-", "").hashCode();
            if (i < 0) {
                i = -i;
            }
            return i;
        } catch (Exception ignored) {
        }
        return -1;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
