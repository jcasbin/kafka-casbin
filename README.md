# kafka-casbin
![License](https://img.shields.io/github/license/jcasbin/kafka-casbin)

[Kafka](https://kafka.apache.org) authorization plugin based on Casbin.

## Installation

```
git clone https://github.com/jcasbin/kafka-casbin.git
```
## Simple Example

```java
public class Main {

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        // create the CasbinAuthorizer object.
        CasbinAuthorizer authorizer = new CasbinAuthorizer();
        Map<String, String> configs = new HashMap<>();
        configs.put("casbin.authorizer.model", "examples/acl_model.conf");
        configs.put("casbin.authorizer.policy", "examples/acl_policy.csv");
        // Set the configs for the authorizer.
        authorizer.configure(configs);

        // You should create the real serverInfo object
        AuthorizerServerInfo serverInfo = mock(AuthorizerServerInfo.class);
        when(serverInfo.endpoints()).thenReturn(new ArrayList<>());
        authorizer.start(serverInfo);

        // create ResourcePattern
        ResourcePattern TOPIC_RESOURCE = new ResourcePattern(
                org.apache.kafka.common.resource.ResourceType.TOPIC,
                "target_topic",
                PatternType.LITERAL
        );
        Action action = new Action(AclOperation.READ, TOPIC_RESOURCE, 0, true, true);

        // Check the permission.
        List<AuthorizationResult> results =
                authorizer.authorize(requestContext("User", "alice"),
                        Arrays.asList(action));

        for (AuthorizationResult result : results) {
           if (result == AuthorizationResult.ALLOWED) {
               System.out.println("有权限");
           } else {
               System.out.println("无权限");
           }
        }

    }

    private static AuthorizableRequestContext requestContext(String principalType, String name) {
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
}
```

## Getting Help

- [jCasbin](https://github.com/casbin/jCasbin)
- [kafka-client](https://github.com/apache/kafka)

## License

This project is under Apache 2.0 License. See the [LICENSE](https://github.com/jcasbin/kafka-casbin/blob/master/LICENSE) file for the full license text.