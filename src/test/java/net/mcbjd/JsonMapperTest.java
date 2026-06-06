package net.mcbjd;

import net.mcbjd.pojo.SkinUploadData;
import net.mcbjd.serialize.SkinUploadDataDeSerialize;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class JsonMapperTest {
    @Test
    @SneakyThrows
    public void json() {
        ObjectMapper JSON_MAPPER = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        JSON_MAPPER.registerModule(new SimpleModule().addDeserializer(SkinUploadData.class,new SkinUploadDataDeSerialize()));

        String json = """
                {"skin_data":"H4sIAAAAAAAA/+3YQQqDMBBA0dx/4VG8V8+QhdKFG5G2djomNu9DUFAkL9GNpbyu1rpExpvHdx8/Pz8/P/8/+qO+u68PP/+H8zw858/zP6Z52caZaxn+Fu//3rjd/zy28pcf7v3Z9z/ju+l5/1sUnW/2emSvJz//SP7ofHsf/Pz8/Pz8/Pz8/PySJEmSJEnfl/0/5gpDJH7+kfyj/9/k5+fn5+fn5+fnv5N/BeO3wr4AQAAA","hash":"34fde760b3ea33c5d713058936427758","geometry_data":"H4sIAAAAAAAA/+2XS4/aMBDHv0vOIcoLmnLrsVIPlfaIVsgkE/A2iZHtsN0ivnvHQNg8nGDIqlRVhUCJ7Zn5+T9+DHsrZTwncrkDLigrrLnlOZ7vuJZt5bSAmJNUztfAcpD8zZov9taKFSCOTwXJAQ1WLHnD4VvCoZD4/kqokKqB7hi+L1zHtf0Qf/Dh+WBf7HTjVOjLuLhcnSMxTtcU4RaTKfZH+A1wjG0J+gs9LTw0nNkeNpQ75chGe3w5x4nJFup8FW8b7+SSM0nkUYkTUORWRNiZgIg53Z769xZN0CNNKXB0W4nknONJ+ClLDssN0PUGAwX+e9srTeTGms/Cg5pnR1HOWFsY94ouYSXexK8rE2GjHVbCKJFmdWlGp64y1BBfTWVYOVdPdWT89KRyAyQxSWUL12gFHPPfZKVFmhGJHlxn6to3omO+m+ykoVg1lT50jWI9OQ4bOcawYVSPm0Eqv/B8YP5qT/n6bNUU8BsSGLGEkYblKQPYNdJRI+wjqtl/lZAPW88Um3LhDay+yGgCbmvDcLWXh9Wc3COnMU7gd3C6gtYpe6kyFhPJuFAHWYaLcblhWaJG4iExxcP00IzSll0X42Kpl9x1vNNBNjRHPKO6a+YbrAdOG8/53Hvc9CtuRONqYL6TQor2CjwTGhC9EwRq1HUCzQoc1mNynyCGOJoV2FGkDjkOyvRea1K9kPgHSOObwvRm35Q5KRhNnLgUkuWaS34Wai5529pRQVcZLFesLBJxGe13eliaCpCntNtqvq3+s0/vrrrhoy/vP1BvPKhu+GtLgP9nxvh7JaecM9zXKckE/PvXzHGnemr1NgmCDykbj45vrRuv0owrHBtMt1WOthcq4/4a5pMZ/9114616mgONqxwbXEOl40W/e6pHrfgPLg5u/Rs5CUaUFE8ZfWxZ8Xz4DYJSQicTEgAA","geometry_name":"ewogICAiZ2VvbWV0cnkiIDogewogICAgICAiZGVmYXVsdCIgOiAiZ2VvbWV0cnkuaHVtYW5vaWQuY3VzdG9tIgogICB9Cn0K","skin_id":"c18e65aa-7b21-4637-9b63-8ad63622ef01.Custom94ea493aaa384fd7a8aa9bfa0163be5e","uuid":"00000000-0000-4000-8000-00003242b4ad","xuid":"3242b4ad"}
                """;

        SkinUploadData skinUploadData = JSON_MAPPER.convertValue(json, SkinUploadData.class);
        System.out.println(skinUploadData);
    }

    @Test
    public void uid(){
        System.out.println(initUid("00000000-0000-4000-8000-00003242b4ad"));
    }

    public int initUid(String uuid) {
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
}
