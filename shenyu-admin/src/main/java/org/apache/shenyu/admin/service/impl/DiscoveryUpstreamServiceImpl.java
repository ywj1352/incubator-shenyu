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

package org.apache.shenyu.admin.service.impl;

import org.apache.shenyu.admin.discovery.DiscoveryProcessor;
import org.apache.shenyu.admin.discovery.DiscoveryProcessorHolder;
import org.apache.shenyu.admin.mapper.DiscoveryHandlerMapper;
import org.apache.shenyu.admin.mapper.DiscoveryMapper;
import org.apache.shenyu.admin.mapper.DiscoveryUpstreamMapper;
import org.apache.shenyu.admin.mapper.ProxySelectorMapper;
import org.apache.shenyu.admin.mapper.DiscoveryRelMapper;
import org.apache.shenyu.admin.mapper.SelectorMapper;
import org.apache.shenyu.admin.model.dto.DiscoveryUpstreamDTO;
import org.apache.shenyu.admin.model.entity.DiscoveryDO;
import org.apache.shenyu.admin.model.entity.DiscoveryHandlerDO;
import org.apache.shenyu.admin.model.entity.DiscoveryUpstreamDO;
import org.apache.shenyu.admin.model.entity.ProxySelectorDO;
import org.apache.shenyu.admin.model.entity.DiscoveryRelDO;
import org.apache.shenyu.admin.model.entity.SelectorDO;
import org.apache.shenyu.admin.service.DiscoveryUpstreamService;
import org.apache.shenyu.admin.transfer.DiscoveryTransfer;
import org.apache.shenyu.admin.utils.ShenyuResultMessage;
import org.apache.shenyu.common.dto.DiscoverySyncData;
import org.apache.shenyu.common.dto.DiscoveryUpstreamData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscoveryUpstreamServiceImpl implements DiscoveryUpstreamService {

    private final DiscoveryUpstreamMapper discoveryUpstreamMapper;

    private final DiscoveryHandlerMapper discoveryHandlerMapper;

    private final DiscoveryRelMapper discoveryRelMapper;

    private final ProxySelectorMapper proxySelectorMapper;

    private final DiscoveryMapper discoveryMapper;

    private final SelectorMapper selectorMapper;

    private final DiscoveryProcessorHolder discoveryProcessorHolder;

    public DiscoveryUpstreamServiceImpl(final DiscoveryUpstreamMapper discoveryUpstreamMapper,
                                        final DiscoveryHandlerMapper discoveryHandlerMapper,
                                        final ProxySelectorMapper proxySelectorMapper,
                                        final DiscoveryMapper discoveryMapper,
                                        final DiscoveryRelMapper discoveryRelMapper,
                                        final SelectorMapper selectorMapper,
                                        final DiscoveryProcessorHolder discoveryProcessorHolder) {
        this.discoveryUpstreamMapper = discoveryUpstreamMapper;
        this.discoveryProcessorHolder = discoveryProcessorHolder;
        this.discoveryHandlerMapper = discoveryHandlerMapper;
        this.discoveryMapper = discoveryMapper;
        this.discoveryRelMapper = discoveryRelMapper;
        this.selectorMapper = selectorMapper;
        this.proxySelectorMapper = proxySelectorMapper;

    }

    /**
     * createOrUpdate.
     *
     * @param discoveryUpstreamDTO discoveryUpstreamDTO
     * @return the string
     */
    @Override
    public String createOrUpdate(final DiscoveryUpstreamDTO discoveryUpstreamDTO) {

        return StringUtils.hasLength(discoveryUpstreamDTO.getId())
                ? update(discoveryUpstreamDTO) : create(discoveryUpstreamDTO);
    }

    @Override
    public int nativeCreateOrUpdate(DiscoveryUpstreamDTO discoveryUpstreamDTO) {
        DiscoveryUpstreamDO discoveryUpstreamDO = DiscoveryUpstreamDO.buildDiscoveryUpstreamDO(discoveryUpstreamDTO);
        return StringUtils.hasLength(discoveryUpstreamDTO.getId()) ?
                discoveryUpstreamMapper.updateSelective(discoveryUpstreamDO) : discoveryUpstreamMapper.insert(discoveryUpstreamDO);
    }

    /**
     * delete.
     *
     * @param ids id list
     * @return the string
     */
    @Override
    public String delete(final List<String> ids) {

        discoveryUpstreamMapper.deleteByIds(ids);
        return ShenyuResultMessage.DELETE_SUCCESS;
    }

    @Override
    public List<DiscoverySyncData> listAll() {
        List<DiscoveryHandlerDO> discoveryHandlerDOS = discoveryHandlerMapper.selectAll();
        return discoveryHandlerDOS.stream().map(d -> {
            DiscoveryRelDO discoveryRelDO = discoveryRelMapper.selectByDiscoveryHandlerId(d.getId());
            DiscoverySyncData discoverySyncData = new DiscoverySyncData();
            discoverySyncData.setPluginName(discoveryRelDO.getPluginName());
            if (StringUtils.hasLength(discoveryRelDO.getSelectorId())) {
                String selectorId = discoveryRelDO.getSelectorId();
                discoverySyncData.setSelectorId(selectorId);
                SelectorDO selectorDO = selectorMapper.selectById(selectorId);
                discoverySyncData.setSelectorName(selectorDO.getName());
            } else {
                String proxySelectorId = discoveryRelDO.getProxySelectorId();
                discoverySyncData.setSelectorId(proxySelectorId);
                ProxySelectorDO proxySelectorDO = proxySelectorMapper.selectById(proxySelectorId);
                discoverySyncData.setSelectorName(proxySelectorDO.getName());
            }
            List<DiscoveryUpstreamData> discoveryUpstreamDataList = discoveryUpstreamMapper.selectByDiscoveryHandlerId(d.getId()).stream()
                    .map(DiscoveryTransfer.INSTANCE::mapToData).collect(Collectors.toList());
            discoverySyncData.setUpstreamDataList(discoveryUpstreamDataList);
            return discoverySyncData;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DiscoveryUpstreamData> findBySelectorId(String selectorId) {
        DiscoveryHandlerDO discoveryHandlerDO = discoveryHandlerMapper.selectBySelectorId(selectorId);
        List<DiscoveryUpstreamDO> discoveryUpstreamDOS = discoveryUpstreamMapper.selectByDiscoveryHandlerId(discoveryHandlerDO.getId());
        return discoveryUpstreamDOS.stream().map(DiscoveryTransfer.INSTANCE::mapToData).collect(Collectors.toList());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBySelectorIdAndUrl(String selectorId, String url) {
        DiscoveryHandlerDO discoveryHandlerDO = discoveryHandlerMapper.selectBySelectorId(selectorId);
        discoveryUpstreamMapper.deleteByUrl(discoveryHandlerDO.getId(), url);
    }

    /**
     * create.
     *
     * @param discoveryUpstreamDTO discoveryUpstreamDTO
     * @return the string
     */
    private String create(final DiscoveryUpstreamDTO discoveryUpstreamDTO) {
        DiscoveryUpstreamDO discoveryUpstreamDO = DiscoveryUpstreamDO.buildDiscoveryUpstreamDO(discoveryUpstreamDTO);
        discoveryUpstreamMapper.insert(discoveryUpstreamDO);
        fetchAll(discoveryUpstreamDTO.getDiscoveryHandlerId());
        return ShenyuResultMessage.CREATE_SUCCESS;
    }

    /**
     * update.
     *
     * @param discoveryUpstreamDTO discoveryUpstreamDTO
     * @return the string
     */
    private String update(final DiscoveryUpstreamDTO discoveryUpstreamDTO) {
        DiscoveryUpstreamDO discoveryUpstreamDO = DiscoveryUpstreamDO.buildDiscoveryUpstreamDO(discoveryUpstreamDTO);
        discoveryUpstreamMapper.update(discoveryUpstreamDO);
        fetchAll(discoveryUpstreamDTO.getDiscoveryHandlerId());
        return ShenyuResultMessage.UPDATE_SUCCESS;
    }

    private void fetchAll(final String discoveryHandlerId) {
        List<DiscoveryUpstreamDO> discoveryUpstreamDOS = discoveryUpstreamMapper.selectByDiscoveryHandlerId(discoveryHandlerId);
        DiscoveryHandlerDO discoveryHandlerDO = discoveryHandlerMapper.selectById(discoveryHandlerId);
        ProxySelectorDO proxySelectorDO = proxySelectorMapper.selectByHandlerId(discoveryHandlerId);
        DiscoveryDO discoveryDO = discoveryMapper.selectById(discoveryHandlerDO.getDiscoveryId());
        List<DiscoveryUpstreamDTO> collect = discoveryUpstreamDOS.stream().map(DiscoveryTransfer.INSTANCE::mapToDTO).collect(Collectors.toList());
        DiscoveryProcessor discoveryProcessor = discoveryProcessorHolder.chooseProcessor(discoveryDO.getType());
        discoveryProcessor.changeUpstream(DiscoveryTransfer.INSTANCE.mapToDTO(proxySelectorDO), collect);
    }

}
