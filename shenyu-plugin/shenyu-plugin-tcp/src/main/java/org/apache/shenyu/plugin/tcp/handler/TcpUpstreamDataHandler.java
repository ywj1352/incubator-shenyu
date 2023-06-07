/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shenyu.plugin.tcp.handler;

import org.apache.shenyu.common.dto.DiscoveryUpstreamData;
import org.apache.shenyu.common.dto.ProxySelectorData;
import org.apache.shenyu.common.enums.PluginEnum;
import org.apache.shenyu.plugin.base.handler.ProxySelectorDataHandler;
import org.apache.shenyu.protocol.tcp.BootstrapServer;
import org.apache.shenyu.protocol.tcp.TcpServerConfiguration;
import org.apache.shenyu.protocol.tcp.UpstreamProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * upstreamList data change.
 */
public class TcpUpstreamDataHandler implements ProxySelectorDataHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TcpUpstreamDataHandler.class);

    private final Map<String, BootstrapServer> cache = new ConcurrentHashMap<>();

    @Override
    public synchronized void createProxySelector(final ProxySelectorData proxySelectorData) {
        String name = proxySelectorData.getName();
        if (!cache.containsKey(name)) {
            Integer forwardPort = proxySelectorData.getForwardPort();
            TcpServerConfiguration tcpServerConfiguration = new TcpServerConfiguration();
            tcpServerConfiguration.setPort(forwardPort);
            tcpServerConfiguration.setProps(proxySelectorData.getProps());
            tcpServerConfiguration.setPluginSelectorName(name);
            //   UpstreamProvider.getSingleton().createUpstreams(name, upstreamDataList);
            BootstrapServer bootstrapServer = TcpBootstrapFactory.getSingleton().createBootstrapServer(tcpServerConfiguration);
            cache.put(name, bootstrapServer);
            LOG.info("shenyu create TcpBootstrapServer success port is {}", forwardPort);
        }
    }

    @Override
    public void addUpstreamDataList(final String proxySelectorName, final List<DiscoveryUpstreamData> upstreamDataList) {
        UpstreamProvider.getSingleton().provide(proxySelectorName).addAll(upstreamDataList);
    }

    @Override
    public void removeUpstreamDataList(final String proxySelectorName, final List<DiscoveryUpstreamData> upstreamDataList) {
        UpstreamProvider.getSingleton().remove(proxySelectorName, upstreamDataList);
        BootstrapServer bootstrapServer = cache.get(proxySelectorName);
        bootstrapServer.removeCommonUpstream(upstreamDataList);
        LOG.info("shenyu update TcpBootstrapServer success remove is {}", upstreamDataList);
    }

    @Override
    public void removeProxySelector(final String proxySelectorName) {
        if (cache.containsKey(proxySelectorName)) {
            cache.remove(proxySelectorName).shutdown();
            LOG.info("shenyu shutdown {}", proxySelectorName);
        }
    }

    @Override
    public String pluginName() {
        return PluginEnum.TCP.getName();
    }

}
