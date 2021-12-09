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
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.server.authorizer.*;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.kafka.authorizer.config.CasbinAuthorizerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Kafka Authorizer implementation with Casbin.
 *
 * @author Yixiang Zhao (@seriouszyx)
 * @date 2021-11-22 18:56
 **/
public class CasbinAuthorizer implements Authorizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasbinAuthorizer.class);
    private CasbinAuthorizerConfig config;
    private Enforcer enforcer;

    public CasbinAuthorizer() {
    }

    @Override
    public Map<Endpoint, ? extends CompletionStage<Void>> start(AuthorizerServerInfo authorizerServerInfo) {
        enforcer = new Enforcer(config.getModel(), config.getPolicy());
        return authorizerServerInfo.endpoints().stream()
                .collect(Collectors.toMap(
                    endpoint -> endpoint,
                    endpoint -> CompletableFuture.completedFuture(null)
                ));
    }

    @Override
    public List<AuthorizationResult> authorize(AuthorizableRequestContext authorizableRequestContext, List<Action> list) {
        KafkaPrincipal kafkaPrincipal = authorizableRequestContext.principal();
        List<AuthorizationResult> results = new ArrayList<>(list.size());
        for (Action action : list) {
            boolean result = enforcer.enforce(kafkaPrincipal.getName(), action.resourcePattern().name(), action.operation().name());
            results.add(result ? AuthorizationResult.ALLOWED : AuthorizationResult.DENIED);
            if (result) {
                LOGGER.info("[ALLOW] Auth request {} on {}:{} by {} {}",
                        action.operation().name(), action.resourcePattern().resourceType(), action.resourcePattern().name(),
                        kafkaPrincipal.getPrincipalType(), kafkaPrincipal.getName());
            } else {
                LOGGER.info("[DENY] Auth request {} on {}:{} by {} {}",
                        action.operation().name(), action.resourcePattern().resourceType(), action.resourcePattern().name(),
                        kafkaPrincipal.getPrincipalType(), kafkaPrincipal.getName());
            }
        }
        return results;
    }

    @Override
    public List<? extends CompletionStage<AclCreateResult>> createAcls(AuthorizableRequestContext authorizableRequestContext, List<AclBinding> list) {
        return null;
    }

    @Override
    public List<? extends CompletionStage<AclDeleteResult>> deleteAcls(AuthorizableRequestContext authorizableRequestContext, List<AclBindingFilter> list) {
        return null;
    }

    @Override
    public Iterable<AclBinding> acls(AclBindingFilter aclBindingFilter) {
        return null;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void configure(Map<String, ?> map) {
        config = new CasbinAuthorizerConfig(map);
    }
}
