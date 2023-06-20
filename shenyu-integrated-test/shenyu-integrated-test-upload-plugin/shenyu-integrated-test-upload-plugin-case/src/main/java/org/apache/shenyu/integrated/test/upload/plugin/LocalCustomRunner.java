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

package org.apache.shenyu.integrated.test.upload.plugin;

import org.apache.shenyu.common.dto.PluginData;
import org.apache.shenyu.integratedtest.common.helper.HttpHelper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * mock user upload custom-plugin jar.
 */
@Component
public class LocalCustomRunner implements CommandLineRunner {

    public static final String JAR_PATH = "/opt/shenyu-integrated-test-upload-plugin-case/shenyu-custom-plugin.jar";

    @Override
    public void run(final String... args) throws Exception {
        Path path = Paths.get(JAR_PATH);
        byte[] jarData = Files.readAllBytes(path);
        String jarTxt = Base64.getEncoder().encodeToString(jarData);
        PluginData pluginData = new PluginData();
        pluginData.setEnabled(true);
        pluginData.setName("CustomPlugin");
        pluginData.setRole("Test");
        pluginData.setId("1");
        pluginData.setPluginJar(jarTxt);
        HttpHelper.INSTANCE.postGateway("/shenyu/plugin/saveOrUpdate", pluginData, String.class);
    }
}
