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

import org.apache.kafka.common.Endpoint;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.network.ClientInformation;
import org.apache.kafka.common.network.ListenerName;
import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.requests.RequestContext;
import org.apache.kafka.common.requests.RequestHeader;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.server.authorizer.Action;
import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.authorizer.AuthorizationResult;
import org.apache.kafka.server.authorizer.AuthorizerServerInfo;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test CasbinAuthorizer.
 *
 * @author Yixiang Zhao (@seriouszyx)
 * @date 2021-11-26 18:33
 **/
public class CasbinAuthorizerTest {

    private static final ResourcePattern TOPIC_RESOURCE = new ResourcePattern(
            org.apache.kafka.common.resource.ResourceType.TOPIC,
            "target_topic",
            PatternType.LITERAL
    );

    private CasbinAuthorizer authorizer;

    @Before
    public void init() {
        authorizer = new CasbinAuthorizer();
        Map<String, String> configs = new HashMap<>();
        configs.put("casbin.authorizer.model", "examples/acl_model.conf");
        configs.put("casbin.authorizer.policy", "examples/acl_policy.csv");
        authorizer.configure(configs);
    }

    @Test
    public void testStart() {
        List<Endpoint> endpoints = Arrays.asList(
                new Endpoint("PLAINTEXT", SecurityProtocol.PLAINTEXT, "localhost", 9092),
                new Endpoint("SSL", SecurityProtocol.SSL, "localhost", 9093)
        );
        AuthorizerServerInfo serverInfo = mock(AuthorizerServerInfo.class);
        when(serverInfo.endpoints()).thenReturn(endpoints);
        assertThat(authorizer.start(serverInfo)).allSatisfy(
                (endpoint, completionStage) -> assertThatNoException().isThrownBy(
                        () -> completionStage.toCompletableFuture().get(0, TimeUnit.MICROSECONDS)
                )
        );
    }

    @Test
    public void testCasbinAuthorizer() {
        AuthorizerServerInfo serverInfo = mock(AuthorizerServerInfo.class);
        when(serverInfo.endpoints()).thenReturn(new ArrayList<>());
        authorizer.start(serverInfo);
        // Basic ACL checks
        testSingleAction(requestContext("User", "alice"), action(AclOperation.READ, TOPIC_RESOURCE), true);
        testSingleAction(requestContext("User", "bob"), action(AclOperation.WRITE, TOPIC_RESOURCE), true);
        testSingleAction(requestContext("User", "alice"), action(AclOperation.WRITE, TOPIC_RESOURCE), false);
        testSingleAction(requestContext("User", "bob"), action(AclOperation.READ, TOPIC_RESOURCE), false);
    }

    private void testSingleAction(AuthorizableRequestContext context, Action action, boolean allowed) {
        List<AuthorizationResult> results = authorizer.authorize(context, Arrays.asList(action));
        for (AuthorizationResult result : results) {
            assertThat(result).isEqualTo(allowed ? AuthorizationResult.ALLOWED : AuthorizationResult.DENIED);
        }
    }

    private AuthorizableRequestContext requestContext(String principalType, String name) {
        return new RequestContext(
                new RequestHeader(ApiKeys.METADATA, (short) 0, "test-client-id", 123),
                "test-connection-id",
                InetAddress.getLoopbackAddress(),
                new KafkaPrincipal(principalType, name),
                new ListenerName("PLAINTEXT"),
                SecurityProtocol.PLAINTEXT,
                ClientInformation.EMPTY,
                false
        );
    }

    private Action action(AclOperation operation, ResourcePattern resourcePattern) {
        return new Action(operation, resourcePattern, 0, true, true);
    }
}
