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

package org.apache.shenyu.plugin.base.cache;

import org.apache.shenyu.common.dto.DiscoveryUpstreamData;
import org.apache.shenyu.common.dto.ProxySelectorData;
import org.apache.shenyu.plugin.base.handler.ProxySelectorDataHandler;
import org.apache.shenyu.sync.data.api.ProxySelectorDataSubscriber;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CommonProxySelectorDataSubscriber.
 */
public class CommonProxySelectorDataSubscriber implements ProxySelectorDataSubscriber {

    private final Map<String, ProxySelectorDataHandler> handlerMap;

    public CommonProxySelectorDataSubscriber(final List<ProxySelectorDataHandler> proxySelectorDataHandlerList) {
        this.handlerMap = proxySelectorDataHandlerList.stream().collect(Collectors.toConcurrentMap(ProxySelectorDataHandler::pluginName, e -> e));
    }


    @Override
    public void onSubscribeProxySelector(final ProxySelectorData proxySelectorData) {
        Optional.ofNullable(handlerMap.get(proxySelectorData.getPluginName()))
                .ifPresent(handler -> handler.createProxySelector(proxySelectorData));
    }

    @Override
    public void unSubscribeProxySelector(final ProxySelectorData proxySelectorData) {
        Optional.ofNullable(handlerMap.get(proxySelectorData.getPluginName()))
                .ifPresent(handler -> handler.removeProxySelector(proxySelectorData.getName()));
    }

    @Override
    public void onSubscribeDiscoveryUpstreamData(final ProxySelectorData proxySelectorData, final List<DiscoveryUpstreamData> upstreamDataList) {
        Optional.ofNullable(handlerMap.get(proxySelectorData.getPluginName()))
                .ifPresent(handler -> handler.addUpstreamDataList(proxySelectorData.getName(), upstreamDataList));
    }

    @Override
    public void unSubscribeDiscoveryUpstreamData(final ProxySelectorData proxySelectorData, final List<DiscoveryUpstreamData> upstreamDataList) {
        Optional.ofNullable(handlerMap.get(proxySelectorData.getPluginName()))
                .ifPresent(handler -> handler.removeUpstreamDataList(proxySelectorData.getName(), upstreamDataList));
    }

    @Override
    public void refresh() {
        ProxySelectorDataSubscriber.super.refresh();
    }
}
