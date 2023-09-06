package org.apache.shenyu.admin.discovery;

import org.apache.shenyu.admin.model.dto.DiscoveryHandlerDTO;
import org.apache.shenyu.admin.model.dto.DiscoveryUpstreamDTO;
import org.apache.shenyu.admin.model.dto.ProxySelectorDTO;
import org.apache.shenyu.admin.model.dto.SelectorDTO;
import org.apache.shenyu.admin.model.entity.DiscoveryDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShenyuClientDiscoveryProcessor implements DiscoveryProcessor, ApplicationEventPublisherAware {

    private static final Logger LOG = LoggerFactory.getLogger(LocalDiscoveryProcessor.class);

    private ApplicationEventPublisher applicationEventPublisher;


    @Override
    public void createDiscovery(DiscoveryDO discoveryDO) {

    }

    @Override
    public void createProxySelector(DiscoveryHandlerDTO discoveryHandlerDTO, ProxySelectorDTO proxySelectorDTO) {

    }

    @Override
    public void createSelectorWatcher(DiscoveryHandlerDTO discoveryHandlerDTO, SelectorDTO selectorDTO, String pluginName) {

    }

    @Override
    public void removeDiscovery(DiscoveryDO discoveryDO) {

    }

    @Override
    public void removeProxySelector(DiscoveryHandlerDTO discoveryHandlerDTO, ProxySelectorDTO proxySelectorDTO) {

    }

    @Override
    public void changeUpstream(ProxySelectorDTO proxySelectorDTO, List<DiscoveryUpstreamDTO> upstreamDTOS) {


        //todo 实现他
    }

    @Override
    public void fetchAll(String discoveryHandlerId) {


    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public DiscoveryMode mode() {
        return DiscoveryMode.SHENYU_CLIENT;
    }
}
