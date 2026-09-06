package net.mcbjd.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"sign", "md5_list"})
@Getter
@Setter
public class SkinCheck implements Serializable {
    @JsonProperty(value = "sign", defaultValue = "62bd6025d29b17ff")
    private String sign;
    @JsonProperty("md5_list")
    private String[] md5_list;

    public SkinCheck(String sign, String[] md5_list) {
        this.sign = Objects.requireNonNullElse(sign, "62bd6025d29b17ff");
        this.md5_list = md5_list;
    }

    public SkinCheck(String... md5_list) {
        this.sign = "62bd6025d29b17ff";
        this.md5_list = md5_list;
    }

    @Data
    @ToString
    public static class SkinCheckModel {
        private Integer code;
        private String message;
        private String details;
        private Boolean[] entities;
    }
}
