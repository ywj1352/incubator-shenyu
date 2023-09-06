package org.apache.shenyu.plugin.websocket.handler;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.shenyu.common.dto.DiscoverySyncData;
import org.apache.shenyu.common.dto.DiscoveryUpstreamData;
import org.apache.shenyu.common.enums.PluginEnum;
import org.apache.shenyu.loadbalancer.cache.UpstreamCacheManager;
import org.apache.shenyu.loadbalancer.entity.Upstream;
import org.apache.shenyu.plugin.base.handler.DiscoveryUpstreamDataHandler;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class WebSocketUpstreamDataHandler implements DiscoveryUpstreamDataHandler {

    @Override
    public void handlerDiscoveryUpstreamData(final DiscoverySyncData discoverySyncData) {
        List<DiscoveryUpstreamData> upstreamDataList = discoverySyncData.getUpstreamDataList();
        if (CollectionUtils.isNotEmpty(upstreamDataList)) {
            UpstreamCacheManager.getInstance().submit(discoverySyncData.getSelectorId(), convertUpstreamList(upstreamDataList));
        }
    }

    private List<Upstream> convertUpstreamList(final List<DiscoveryUpstreamData> upstreamList) {
        return upstreamList.stream()
                .map(u -> Upstream.builder()
                        .protocol(u.getProtocol())
                        .url(u.getUrl())
                        .weight(u.getWeight())
                        .status(Objects.equals(u.getStatus(), 0))
                        .timestamp(Optional.ofNullable(u.getDateCreated()).map(Timestamp::getTime).orElse(System.currentTimeMillis()))
                        .warmup(u.getWarmup())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public String pluginName() {
        return PluginEnum.WEB_SOCKET.getName();
    }

}
