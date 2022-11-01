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

package org.apache.shenyu.integrated.test.sdk.http.http;

import org.apache.shenyu.common.dto.ConditionData;
import org.apache.shenyu.common.dto.PluginData;
import org.apache.shenyu.common.enums.PluginEnum;
import org.apache.shenyu.integrated.test.sdk.http.dto.SdkTestData;
import org.apache.shenyu.integratedtest.common.AbstractPluginDataInit;
import org.apache.shenyu.integratedtest.common.helper.HttpHelper;
import org.apache.shenyu.web.controller.LocalPluginController;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpSdkIntegratedTest extends AbstractPluginDataInit {

    @Test
    public void testCallShenyuSelectorRule() throws IOException {
        LocalPluginController.SelectorRulesData selectorRulesData = buildSelectorRulesRequest();
        Map<String, Object> headers = new HashMap<>();
        SdkTestData response = HttpHelper.INSTANCE.postHttpService("http://localhost:8899/shenyu-sdk/selectorRule", headers, selectorRulesData, SdkTestData.class);
        assertNotNull(response);
    }

    @Test
    public void testShenyuSaveOrUpdate() throws IOException {
        PluginData pluginData = buildPluginDataRequest();
        Map<String, Object> headers = new HashMap<>();
        SdkTestData response = HttpHelper.INSTANCE.postHttpService("http://localhost:8899/shenyu-sdk/saveOrUpdate", headers, pluginData, SdkTestData.class);
        assertNotNull(response);
    }

    private LocalPluginController.SelectorRulesData buildSelectorRulesRequest() {
        LocalPluginController.SelectorRulesData selectorRulesData = new LocalPluginController.SelectorRulesData();
        selectorRulesData.setSelectorName("selector");
        selectorRulesData.setSelectorHandler("[{\"upstreamUrl\":\"127.0.0.1:8080\"}]");
        selectorRulesData.setPluginName("divide");
        ConditionData conditionData = new ConditionData();
        conditionData.setParamType("uri");
        conditionData.setOperator("match");
        conditionData.setParamValue("/**");
        selectorRulesData.setConditionDataList(Collections.singletonList(conditionData));
        LocalPluginController.RuleLocalData ruleLocalData = new LocalPluginController.RuleLocalData();
        ruleLocalData.setRuleHandler("{\"loadBalance\":\"random\"}");
        ruleLocalData.setConditionDataList(Collections.singletonList(conditionData));
        selectorRulesData.setRuleDataList(Collections.singletonList(ruleLocalData));
        return selectorRulesData;
    }

    private PluginData buildPluginDataRequest() {
        String config = "{\"register\":\"zookeeper://shenyu-zk:2181\",\"threadpool\": \"shared\"}";
        PluginData pluginData = new PluginData();
        pluginData.setConfig(config);
        pluginData.setName(PluginEnum.GRPC.getName());
        pluginData.setEnabled(true);
        return pluginData;
    }
}
