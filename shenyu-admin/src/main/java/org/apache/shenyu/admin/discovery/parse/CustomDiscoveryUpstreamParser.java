package org.apache.shenyu.admin.discovery.parse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.apache.shenyu.admin.discovery.DiscoveryDataChangedEventSyncListener;
import org.apache.shenyu.admin.mapper.PluginMapper;
import org.apache.shenyu.admin.mapper.ProxySelectorMapper;
import org.apache.shenyu.admin.model.dto.DiscoveryUpstreamDTO;
import org.apache.shenyu.admin.model.entity.ProxySelectorDO;
import org.apache.shenyu.common.dto.DiscoveryUpstreamData;
import org.apache.shenyu.common.dto.ProxySelectorData;
import org.apache.shenyu.common.utils.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * DiscoveryUpstreamParser.
 * <p>
 * You can define a custom map mapper if your custom upstream doesn't fit
 * <p/>
 */
public class CustomDiscoveryUpstreamParser implements JsonDeserializer<DiscoveryUpstreamData>, keyValueParser {

    private static final Logger LOG = LoggerFactory.getLogger(CustomDiscoveryUpstreamParser.class);

    private final Map<String, String> conversion;

    private final ProxySelectorMapper proxySelectorMapper;

    public CustomDiscoveryUpstreamParser(final Map<String, String> conversion, final ProxySelectorMapper proxySelectorMapper) {
        this.conversion = conversion;
        this.proxySelectorMapper = proxySelectorMapper;
    }

    @Override
    public DiscoveryUpstreamData deserialize(final JsonElement jsonElement,
                                             final Type type,
                                             final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject asJsonObject = jsonElement.getAsJsonObject();
        JsonObject afterJson = new JsonObject();
        for (Map.Entry<String, JsonElement> elementEntry : asJsonObject.entrySet()) {
            String key = elementEntry.getKey();
            if (conversion.containsKey(key)) {
                String transferKey = conversion.get(key);
                afterJson.add(transferKey, elementEntry.getValue());
            } else {
                afterJson.add(key, elementEntry.getValue());
            }
        }
        return GsonUtils.getInstance().fromJson(afterJson, DiscoveryUpstreamData.class);
    }

    @Override
    public List<DiscoveryUpstreamData> parseValue(final String jsonString) {
        GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(DiscoveryUpstreamData.class, this);
        Gson gson = gsonBuilder.create();
        return gson.fromJson(jsonString, new TypeToken<List<DiscoveryUpstreamData>>() {
        }.getType());
    }

    /**
     * /shenyu/discovery/{pluginName}/{selectorId}/{upstream_suq}
     *
     * @param key
     * @return
     */
    @Override
    public ProxySelectorData parseKey(String key) {
        String[] subArray = key.split("/");
        String proxySelectorId = subArray[3];
        ProxySelectorData proxySelectorData = new ProxySelectorData();
        ProxySelectorDO proxySelectorDO = proxySelectorMapper.selectById(proxySelectorId);
        BeanUtils.copyProperties(proxySelectorDO, proxySelectorData);
        LOG.info("shenyu parseKey pluginName={}|proxySelectorName={}|type={}|forwardPort={}", proxySelectorData.getPluginName(), proxySelectorData.getName(), proxySelectorData.getType(), proxySelectorData.getForwardPort());
        return proxySelectorData;
    }

}
