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

package org.apache.shenyu.sync.data.zookeeper;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.shenyu.common.constant.DefaultPathConstants;
import org.apache.shenyu.common.dto.PluginData;
import org.apache.shenyu.common.dto.RuleData;
import org.apache.shenyu.common.dto.AppAuthData;
import org.apache.shenyu.common.dto.MetaData;
import org.apache.shenyu.common.dto.SelectorData;
import org.apache.shenyu.common.dto.ProxySelectorData;
import org.apache.shenyu.common.dto.DiscoveryUpstreamData;
import org.apache.shenyu.common.exception.ShenyuException;
import org.apache.shenyu.common.utils.GsonUtils;
import org.apache.shenyu.sync.data.api.AuthDataSubscriber;
import org.apache.shenyu.sync.data.api.MetaDataSubscriber;
import org.apache.shenyu.sync.data.api.PluginDataSubscriber;
import org.apache.shenyu.sync.data.api.ProxySelectorDataSubscriber;
import org.apache.shenyu.sync.data.api.SyncDataService;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Collections;

/**
 * this cache data with zookeeper.
 */
public class ZookeeperSyncDataService implements SyncDataService {

    private final ZookeeperClient zkClient;

    private final PluginDataSubscriber pluginDataSubscriber;

    private final List<MetaDataSubscriber> metaDataSubscribers;

    private final List<AuthDataSubscriber> authDataSubscribers;

    private final List<ProxySelectorDataSubscriber> proxySelectorDataSubscribers;


    /**
     * Instantiates a new Zookeeper cache manager.
     *
     * @param zkClient             the zk client
     * @param pluginDataSubscriber the plugin data subscriber
     * @param metaDataSubscribers  the meta data subscribers
     * @param authDataSubscribers  the auth data subscribers
     */
    public ZookeeperSyncDataService(final ZookeeperClient zkClient,
                                    final PluginDataSubscriber pluginDataSubscriber,
                                    final List<MetaDataSubscriber> metaDataSubscribers,
                                    final List<AuthDataSubscriber> authDataSubscribers,
                                    final List<ProxySelectorDataSubscriber> proxySelectorDataSubscribers) {
        this.zkClient = zkClient;
        this.pluginDataSubscriber = pluginDataSubscriber;
        this.metaDataSubscribers = metaDataSubscribers;
        this.authDataSubscribers = authDataSubscribers;
        this.proxySelectorDataSubscribers = proxySelectorDataSubscribers;
        watcherData();
        watchAppAuth();
        watchMetaData();
    }

    private void watcherData() {
        zkClient.addCache(DefaultPathConstants.PLUGIN_PARENT, new PluginCacheListener());
        zkClient.addCache(DefaultPathConstants.SELECTOR_PARENT, new SelectorCacheListener());
        zkClient.addCache(DefaultPathConstants.RULE_PARENT, new RuleCacheListener());
        zkClient.addCache(DefaultPathConstants.PROXY_SELECTOR_DATA, new ProxySelectorCacheListener());
    }

    private void watchAppAuth() {
        zkClient.addCache(DefaultPathConstants.APP_AUTH_PARENT, new AuthCacheListener());
    }

    private void watchMetaData() {
        zkClient.addCache(DefaultPathConstants.META_DATA, new MetadataCacheListener());
    }

    private void cachePluginData(final PluginData pluginData) {
        Optional.ofNullable(pluginData)
                .flatMap(data -> Optional.ofNullable(pluginDataSubscriber))
                .ifPresent(e -> e.onSubscribe(pluginData));
    }

    private void cacheSelectorData(final SelectorData selectorData) {
        Optional.ofNullable(selectorData)
                .ifPresent(data -> Optional.ofNullable(pluginDataSubscriber)
                        .ifPresent(e -> e.onSelectorSubscribe(data)));
    }

    private void unCacheSelectorData(final String dataPath) {
        SelectorData selectorData = new SelectorData();
        final String selectorId = dataPath.substring(dataPath.lastIndexOf("/") + 1);
        final String str = dataPath.substring(DefaultPathConstants.SELECTOR_PARENT.length());
        final String pluginName = str.substring(1, str.length() - selectorId.length() - 1);
        selectorData.setPluginName(pluginName);
        selectorData.setId(selectorId);

        Optional.ofNullable(pluginDataSubscriber)
                .ifPresent(e -> e.unSelectorSubscribe(selectorData));
    }

    private void cacheRuleData(final RuleData ruleData) {
        Optional.ofNullable(ruleData)
                .ifPresent(data -> Optional.ofNullable(pluginDataSubscriber)
                        .ifPresent(e -> e.onRuleSubscribe(data)));
    }

    private void unCacheRuleData(final String dataPath) {
        String substring = dataPath.substring(dataPath.lastIndexOf("/") + 1);
        final String str = dataPath.substring(DefaultPathConstants.RULE_PARENT.length());
        final String pluginName = str.substring(1, str.length() - substring.length() - 1);
        final List<String> list = Lists.newArrayList(Splitter.on(DefaultPathConstants.SELECTOR_JOIN_RULE).split(substring));

        RuleData ruleData = new RuleData();
        ruleData.setPluginName(pluginName);
        ruleData.setSelectorId(list.get(0));
        ruleData.setId(list.get(1));

        Optional.ofNullable(pluginDataSubscriber)
                .ifPresent(e -> e.unRuleSubscribe(ruleData));
    }

    private void cacheAuthData(final AppAuthData appAuthData) {
        Optional.ofNullable(appAuthData)
                .ifPresent(data -> authDataSubscribers.forEach(e -> e.onSubscribe(data)));
    }

    private void unCacheAuthData(final String dataPath) {
        final String key = dataPath.substring(DefaultPathConstants.APP_AUTH_PARENT.length() + 1);
        AppAuthData appAuthData = new AppAuthData();
        appAuthData.setAppKey(key);
        authDataSubscribers.forEach(e -> e.unSubscribe(appAuthData));
    }

    private void cacheMetaData(final MetaData metaData) {
        Optional.ofNullable(metaData)
                .ifPresent(data -> metaDataSubscribers.forEach(e -> e.onSubscribe(metaData)));
    }

    private void unCacheMetaData(final MetaData metaData) {
        Optional.ofNullable(metaData)
                .ifPresent(data -> metaDataSubscribers.forEach(e -> e.unSubscribe(metaData)));
    }

    @Override
    public void close() {
        if (Objects.nonNull(zkClient)) {
            zkClient.close();
        }
    }

    abstract static class AbstractDataSyncListener implements TreeCacheListener {
        @Override
        public final void childEvent(final CuratorFramework client, final TreeCacheEvent event) {
            ChildData childData = event.getData();
            if (null == childData) {
                return;
            }
            String path = childData.getPath();
            if (Strings.isNullOrEmpty(path)) {
                return;
            }
            event(event.getType(), path, childData);
        }

        /**
         * data sync event.
         *
         * @param type tree cache event type.
         * @param path tree cache event path.
         * @param data tree cache event data.
         */
        protected abstract void event(TreeCacheEvent.Type type, String path, ChildData data);
    }

    class PluginCacheListener extends AbstractDataSyncListener {

        @Override
        public void event(final TreeCacheEvent.Type type, final String path, final ChildData data) {
            // if not uri register path, return.
            if (!path.contains(DefaultPathConstants.PLUGIN_PARENT)) {
                return;
            }

            String pluginName = path.substring(path.lastIndexOf("/") + 1);

            // delete a plugin
            if (type.equals(TreeCacheEvent.Type.NODE_REMOVED)) {
                final PluginData pluginData = new PluginData();
                pluginData.setName(pluginName);
                Optional.ofNullable(pluginDataSubscriber).ifPresent(e -> e.unSubscribe(pluginData));
                return;
            }

            // create or update
            Optional.ofNullable(data)
                    .ifPresent(e -> cachePluginData(GsonUtils.getInstance().fromJson(new String(data.getData(), StandardCharsets.UTF_8), PluginData.class)));
        }
    }

    class SelectorCacheListener extends AbstractDataSyncListener {

        @Override
        public void event(final TreeCacheEvent.Type type, final String path, final ChildData data) {

            // if not uri register path, return.
            if (!path.contains(DefaultPathConstants.SELECTOR_PARENT)) {
                return;
            }
            if (!zkClient.getChildren(path).isEmpty()) {
                return;
            }
            if (type.equals(TreeCacheEvent.Type.NODE_REMOVED)) {
                unCacheSelectorData(path);
                return;
            }

            // create or update
            Optional.ofNullable(data)
                    .ifPresent(e -> cacheSelectorData(GsonUtils.getInstance().fromJson(new String(data.getData(), StandardCharsets.UTF_8), SelectorData.class)));
        }
    }

    class MetadataCacheListener extends AbstractDataSyncListener {

        @Override
        public void event(final TreeCacheEvent.Type type, final String path, final ChildData data) {
            // if not uri register path, return.
            if (!path.contains(DefaultPathConstants.META_DATA)) {
                return;
            }

            if (type.equals(TreeCacheEvent.Type.NODE_REMOVED)) {
                final String realPath = path.substring(DefaultPathConstants.META_DATA.length() + 1);
                MetaData metaData = new MetaData();
                try {
                    metaData.setPath(URLDecoder.decode(realPath, StandardCharsets.UTF_8.name()));
                } catch (UnsupportedEncodingException e) {
                    throw new ShenyuException(e);
                }
                unCacheMetaData(metaData);
                return;
            }

            // create or update
            Optional.ofNullable(data)
                    .ifPresent(e -> cacheMetaData(GsonUtils.getInstance().fromJson(new String(data.getData(), StandardCharsets.UTF_8), MetaData.class)));
        }
    }

    class AuthCacheListener extends AbstractDataSyncListener {

        @Override
        public void event(final TreeCacheEvent.Type type, final String path, final ChildData data) {
            // if not uri register path, return.
            if (!path.contains(DefaultPathConstants.APP_AUTH_PARENT)) {
                return;
            }

            if (type.equals(TreeCacheEvent.Type.NODE_REMOVED)) {
                unCacheAuthData(path);
                return;
            }

            // create or update
            Optional.ofNullable(data)
                    .ifPresent(e -> cacheAuthData(GsonUtils.getInstance().fromJson(new String(data.getData(), StandardCharsets.UTF_8), AppAuthData.class)));
        }
    }

    class RuleCacheListener extends AbstractDataSyncListener {

        @Override
        public void event(final TreeCacheEvent.Type type, final String path, final ChildData data) {
            // if not uri register path, return.
            if (!path.contains(DefaultPathConstants.RULE_PARENT)) {
                return;
            }
            if (!zkClient.getChildren(path).isEmpty()) {
                return;
            }
            if (type.equals(TreeCacheEvent.Type.NODE_REMOVED)) {
                unCacheRuleData(path);
                return;
            }

            // create or update
            Optional.ofNullable(data)
                    .ifPresent(e -> cacheRuleData(GsonUtils.getInstance().fromJson(new String(data.getData(), StandardCharsets.UTF_8), RuleData.class)));
        }
    }

    class ProxySelectorCacheListener extends AbstractDataSyncListener {

        @Override
        protected void event(final TreeCacheEvent.Type type, final String path, final ChildData data) {
            // if not uri register path, return.
            if (!path.contains(DefaultPathConstants.PROXY_SELECTOR_DATA)) {
                return;
            }
            String[] pathInfoArray = path.split("/");
            if (pathInfoArray.length < 5) {
                return;
            }
            String pluginName = pathInfoArray[3];
            String proxySelectorName = pathInfoArray[4];
            ProxySelectorData proxySelectorData = new ProxySelectorData();
            proxySelectorData.setPluginName(pluginName);
            proxySelectorData.setName(proxySelectorName);
            String json = new String(data.getData(), StandardCharsets.UTF_8);
            switch (type) {
                case NODE_ADDED:
                    if (pathInfoArray.length == 5) {
                        proxySelectorDataSubscribers.forEach(e -> e.onSubscribeProxySelector(proxySelectorData));
                    } else if (pathInfoArray.length == 6) {
                        DiscoveryUpstreamData addDiscoveryUpstream = GsonUtils.getInstance().fromJson(json, DiscoveryUpstreamData.class);
                        proxySelectorDataSubscribers.forEach(e -> e.onSubscribeDiscoveryUpstreamData(proxySelectorData, Collections.singletonList(addDiscoveryUpstream)));
                    }
                    break;
                case NODE_REMOVED:
                    if (pathInfoArray.length == 5) {
                        proxySelectorDataSubscribers.forEach(e -> e.unSubscribeProxySelector(proxySelectorData));
                    } else if (pathInfoArray.length == 6) {
                        DiscoveryUpstreamData discoveryUpstreamData = GsonUtils.getInstance().fromJson(json, DiscoveryUpstreamData.class);
                        proxySelectorDataSubscribers.forEach(e -> e.unSubscribeDiscoveryUpstreamData(proxySelectorData, Collections.singletonList(discoveryUpstreamData)));
                    }
                    break;
                default:
                    //ignore
                    break;
            }
        }
    }
}
