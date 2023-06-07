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

package org.apache.shenyu.sync.data.api;

import org.apache.shenyu.common.dto.DiscoveryUpstreamData;
import org.apache.shenyu.common.dto.ProxySelectorData;
import org.apache.shenyu.common.dto.convert.selector.DiscoveryUpstream;

import java.util.List;

/**
 * ProxySelectorDataSubscriber.
 */
public interface ProxySelectorDataSubscriber {

    /**
     * On subscribe.
     *
     * @param proxySelectorData the proxySelector data
     */
    void onSubscribeProxySelector(ProxySelectorData proxySelectorData);


    /**
     * On subscribe.
     *
     * @param proxySelectorData the proxySelector data
     */
    void unSubscribeProxySelector(ProxySelectorData proxySelectorData);


    void onSubscribeDiscoveryUpstreamData(ProxySelectorData proxySelectorData, List<DiscoveryUpstreamData> upstreamDataList);


    void unSubscribeDiscoveryUpstreamData(ProxySelectorData proxySelectorData, List<DiscoveryUpstreamData> upstreamDataList);

    /**
     * Refresh.
     */
    default void refresh() {
    }
}
