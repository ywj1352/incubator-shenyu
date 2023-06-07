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

package org.apache.shenyu.protocol.tcp;

import org.apache.shenyu.common.dto.DiscoveryUpstreamData;
import org.apache.shenyu.common.dto.convert.selector.DiscoveryUpstream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UpstreamProvider.
 */
public final class UpstreamProvider {

    private static final UpstreamProvider SINGLETON = new UpstreamProvider();

    private final Map<String, List<DiscoveryUpstreamData>> cache = new ConcurrentHashMap<>();

    private UpstreamProvider() {
    }

    /**
     * getSingleton.
     *
     * @return UpstreamProvider
     */
    public static UpstreamProvider getSingleton() {
        return SINGLETON;
    }

    /**
     * provide.
     *
     * @param pluginSelectorName pluginSelectorName
     * @return UpstreamList
     */
    public List<DiscoveryUpstreamData> provide(final String pluginSelectorName) {
        return cache.getOrDefault(pluginSelectorName , new ArrayList<>());
    }

    /**
     * createUpstreams.
     *
     * @param pluginSelectorName pluginSelectorName
     * @param upstreams          upstreams
     */
    public void createUpstreams(final String pluginSelectorName, final List<DiscoveryUpstreamData> upstreams) {
        cache.put(pluginSelectorName, upstreams);
    }

    /**
     * refreshCache.
     *
     * @param pluginSelectorName pluginSelectorName
     * @param upstreams          upstreams
     * @return removeList
     */
    public List<DiscoveryUpstreamData> remove(final String pluginSelectorName, final List<DiscoveryUpstreamData> upstreams) {
        return cache.remove(pluginSelectorName);
    }
}
