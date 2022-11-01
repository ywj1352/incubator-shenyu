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

package org.apache.shenyu.integrated.test.sdk.http.dto;

import java.util.StringJoiner;

/**
 * Response.
 */
public class SdkTestData {

    /**
     * code.
     */
    private Integer code;

    /**
     * message.
     */
    private String message;

    /**
     * getCode.
     *
     * @return code
     */
    public Integer getCode() {
        return code;
    }

    /**
     * set code.
     *
     * @param code code
     */
    public void setCode(final Integer code) {
        this.code = code;
    }

    /**
     * getMessage.
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * set message.
     *
     * @param message message
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * toString.
     *
     * @return string
     */
    @Override
    public String toString() {
        return new StringJoiner(", ", SdkTestData.class.getSimpleName() + "[", "]")
                .add("code=" + code)
                .add("message=" + message)
                .toString();
    }
}
