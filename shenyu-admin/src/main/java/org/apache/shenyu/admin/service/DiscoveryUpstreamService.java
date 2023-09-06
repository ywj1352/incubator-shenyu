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

package org.apache.shenyu.admin.service;

import org.apache.shenyu.admin.model.dto.DiscoveryUpstreamDTO;
import org.apache.shenyu.common.dto.DiscoverySyncData;
import org.apache.shenyu.common.dto.DiscoveryUpstreamData;

import java.util.List;

public interface DiscoveryUpstreamService {

    /**
     * createOrUpdate.
     *
     * @param discoveryUpstreamDTO discoveryUpstreamDTO
     * @return the string
     */
    String createOrUpdate(DiscoveryUpstreamDTO discoveryUpstreamDTO);


    /**
     * nativeCreateOrUpdate.
     *
     * @param discoveryUpstreamDTO discoveryUpstreamDTO
     * @return effect rows
     */
    int nativeCreateOrUpdate(DiscoveryUpstreamDTO discoveryUpstreamDTO);

    /**
     * delete.
     *
     * @param ids id list
     * @return the string
     */
    String delete(List<String> ids);

    /**
     * listAll.
     *
     * @return DiscoverySyncDataList
     */
    List<DiscoverySyncData> listAll();


    /**
     * findBySelectorId.
     *
     * @param selectorId selectorId
     * @return DiscoverySyncData
     */
    List<DiscoveryUpstreamData> findBySelectorId(String selectorId);

    /**
     *
     * @param selectorId
     * @param url
     */
    void deleteBySelectorIdAndUrl(String selectorId, String url);
}
