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
package org.casbin.kafka.authorizer;

import org.casbin.kafka.authorizer.config.CasbinAuthorizerConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test CasbinAuthorizerConfig.
 *
 * @author Yixiang Zhao (@seriouszyx)
 * @date 2021-11-25 22:00
 **/
public class CasbinAuthorizerConfigTest {

    @Test
    public void testConfig() {
        Map<String, String> properties = new HashMap<>();
        properties.put("casbin.authorizer.model", "examples/acl.model");
        properties.put("casbin.authorizer.policy", "examples/acl.policy");
        CasbinAuthorizerConfig config = new CasbinAuthorizerConfig(properties);
        Assert.assertEquals("examples/acl.model", config.getModel());
        Assert.assertEquals("examples/acl.policy", config.getPolicy());
    }
}
