// Copyright 2021 The casbin Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.casbin.kafka.authorizer.config;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

/**
 * Configuration for the CasbinAuthorizer.
 *
 * @author Yixiang Zhao (@seriouszyx)
 * @date 2021-11-22 19:25
 **/
public class CasbinAuthorizerConfig extends AbstractConfig {

    private static final String CASBIN_MODEL_CONF = "casbin.authorizer.model";
    private static final String CASBIN_POLICY_CONF = "casbin.authorizer.policy";

    public CasbinAuthorizerConfig(Map<?, ?> originals) {
        super(configDef(), originals);
    }

    private static ConfigDef configDef() {
        return new ConfigDef()
                .define(
                    CASBIN_MODEL_CONF,
                    ConfigDef.Type.STRING,
                    ConfigDef.NO_DEFAULT_VALUE,
                    ConfigDef.Importance.HIGH,
                    "The path to mode.conf"
                ).define(
                    CASBIN_POLICY_CONF,
                    ConfigDef.Type.STRING,
                    ConfigDef.NO_DEFAULT_VALUE,
                    ConfigDef.Importance.HIGH,
                    "The path to policy.csv"
                );
    }

    public String getModel() {
        return getString(CASBIN_MODEL_CONF);
    }

    public String getPolicy() {
        return getString(CASBIN_POLICY_CONF);
    }
}
