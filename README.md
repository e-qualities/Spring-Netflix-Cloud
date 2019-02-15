# Cloud Development with Spring-Netflix

This project shows how to integrate and run various Netflix Open Source components on Cloud Foundry to achieve 
Cloud operations, resilience and performance qualities.

Eventually, we are creating a **landscape blue-print**, which should enable teams to get started in the cloud more quickly using Spring Boot and the battle-tested Netflix stack. We take great care that all of the components shown here can be run locally as well as deployed in Cloud Foundry without modifications.

This project is structured in **various branches**. 

| Branch Name                                                               | Description                                                                        |
| ------------------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| **master**                                                                | shows how to run Eureka (a dynamic service registry) in Cloud Foundry.             |
| **master-with-zuul**                                                      | expands on the previous branch and adds basic Netflix Zuul capabilities.           |
| **master-with-zuul-hystrix**                                              | adds Hystrix circuit breaking to the landscape.                                    |
| **master-with-zuul-hystrix-turbine**                                      | bundles Hystrix streams from various services via Turbine and aggregates them in a live dashboard. |
| **master-with-zuul-hystrix-turbine-ribbon**                               | adds Ribbon client-side loadbalancer and service instance selection.               |
| **master-with-zuul-hystrix-turbine-ribbon-cf**                            | makes Ribbon loadbalancing available on Cloud Foundry.                             |
| **master-with-zuul-hystrix-turbine-ribbon-cf-canarytesting**              |  shows an advanced setup of Zuul implementing a canary testing strategy as a Zuul filter and Ribbon rule. Also includes sample filters for Zuul. |
| **master-with-zuul-hystrix-turbine-ribbon-cf-canarytesting-oauth2**       | integrates Spring Security OAuth2.0 with Zuul. Allows for integration with standard OAuth2.0 Authorization servers. |

Make sure to look out for more available branches integrating more Netflix features as we are step by step completing the picture.

Table of Contents
=================

   * [Deployment](#deployment)
   * [Eureka](#eureka)
      * [Eureka Server Application](#eureka-server-application)
         * [Maven Dependencies for Eureka Server](#maven-dependencies-for-eureka-server)
         * [Eureka Server Spring Boot Application](#eureka-server-spring-boot-application)
         * [Eureka Server Configuration](#eureka-server-configuration)
         * [Eureka Server Manifest.yml](#eureka-server-manifestyml)
      * [Eureka Clients](#eureka-clients)
         * [Address Service](#address-service)
            * [Maven Dependencies](#maven-dependencies)
            * [Address Service Eureka Client Configuration](#address-service-eureka-client-configuration)
         * [Cloud Deployment Configurations](#cloud-deployment-configurations)
            * [Address Service Manifest.yml](#address-service-manifestyml)
         * [Address Service Client](#address-service-client)
            * [Service Discovery with Discovery Client](#service-discovery-with-discovery-client)
            * [Service Discovery Using Loadbalanced RestTemplate](#service-discovery-using-loadbalanced-resttemplate)
            * [Service Discovery Using Feign Client](#service-discovery-using-feign-client)
   * [What did we gain so far? - Or: Why The Hassle?](#what-did-we-gain-so-far---or-why-the-hassle)
   * [Using Custom Service Metadata](#using-custom-service-metadata)
      * [Static Custom Service Metadata](#static-custom-service-metadata)
   * [References](#references)

# Deployment

:gift: For easy deployment to Cloud Foundry, we provide a `manifest.yml` and `manifest-variables.yml` as well as a `cf-push.sh` shell script at the root of the project. **You will have to modify the `manifest-variables.yml` file and enter a unique ID where specified. This is to make deployment URLs unique.**

After having done that you can easily push all components to the Cloud in one go either by calling:

* `cf push --vars-file manifest-variables.yml` or
* `./cf-push.sh` as a short version

Some branches require Cloud Foundry services (e.g. XSUAA) to have been created. In this case, you will also find a `services-manifest.yml` and two shell scripts (`cf-create-services.sh` and `cf-create-services-push.sh` ) at the project root. Using the `Create-Service-Push` Cloud Foundry CLI plugin, you can use these to simply create service instances by calling either

* `cf create-service-push --no-push --vars-file manifest-variables.yml` (to just create the service instances) or
* `./cf-create-services.sh` as a short version or
* `./cf-create-services-push.sh` to first create the service instances then push the applications afterwards.

Note, that `./cf-create-services.sh` checks if the `Create-Service-Push` plugin is installed and if not will attempt to install it for you.

# Eureka

[Eureka](https://github.com/Netflix/eureka) is a dynamic service registry (and client) developed by Netflix.
If you want to understand Eureka in detail and how it is used by Netflix (on AWS) you can have a look at [this page](https://github.com/Netflix/eureka/wiki/Eureka-at-a-glance).

For our purposes, Eureka can serve as
- a service registry for various microservices and -versions.
- a mechanism for dynamic service discovery
- a way of advertising service metadata to possible clients (to be verified)

Furthermore, Eureka can provide the basis for intelligent and highly dynamic load balancing, and aid in realizing canary deployments, blue green deployments and true zero downtime.

While we will focus on these more advanced use cases in other branches of this project, here we focus on how to configure and run Eureka in Cloud Foundry.

For now we build the following scenario:  
![Eureka Setup](.documentation/eureka.png)

Eureka plays the role of a service registry and there are two services (`employee.service` and `address.service`) that register to Eureka.  
Their clients (also realized as services) register to Eureka as well, but first and foremost use Eureka to lookup / discover one or more instances of the services they consume. Once the service instances have been discovered, the clients call the instances directly.

## Eureka Server Application

The Eureka server, i.e. the actual service registry, is a Spring Boot application located in the [eureka.service](./eureka.service) folder.  
To run the application locally, proceed as follows:

- In `eureka.service` folder execute `mvn clean package`
- Execute `java -jar ./target/eureka.service-0.0.1-SNAPSHOT.jar`

To deploy Eureka Server on Cloud Foundry, proceed as follows:

- Log in to your Cloud Foundry account and space using `cf login`
- In `eureka.service` folder execute `mvn clean package`
- Execute `cf push --vars-file ../manifest-variables.yml`

### Maven Dependencies for Eureka Server
First you need to add the proper dependencies to your maven pom.xml

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
</dependencies>

...

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>Greenwich.RELEASE</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

This uses the mechanism of a Maven bill of material (BOM). In the `dependencyManagement` section a dependency to a `pom.xml` file is declared, which specifies compatible versions of Spring cloud libraries (which Netflix Eureka Server is a part of).  
As a consequence, the dependency to `spring-cloud-starter-netflix-eureka-server` is not specified with a version, but the version is coming from the BOM.

### Eureka Server Spring Boot Application

All that is required to make the Spring Boot application a Eureka server, is to add the `@EnableEurekaServer` annotation.

```java
@SpringBootApplication
@EnableEurekaServer
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

### Eureka Server Configuration

Eureka server is configured (as any Spring Boot application) in an [`application.yml`](./eureka.service/src/main/resources/application.yml) file. The file looks as follows:

```yaml
---
spring:
  application:
    name: eureka-server

server:
  port: ${PORT:8761}
 
eureka:
  instance:
    hostname: ${vcap.application.uris[0]:localhost}
  client:
    registerWithEureka: false
    fetchRegistry: false
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka
```

First, the name of the application is specified (in this case _eureka-server_).  
Then the port Eureka is running on needs to be specified. In this case, since we want to be able to run Eureka server on a local machine for testing as well as in a Cloud Foundry environment, we specify the port to by either the value of the `PORT` environment variable injected into every Cloud Foundry application by the Cloud Foundry runtime (usually defaults to port `8080`) or port `8761` if no `PORT` variable was found on the environment.

The next set of configurations turn the Eureka application into a standalone server.  
Eureka servers can also be (and usually are) deployed as clusters and across Cloud regions / zones.  
If deployed as a cluster, the Eureka server instances form a peer-to-peer network, and each Eureka server instance also acts as a client to its peers. In fact, this is the default behaviour when you run Eureka out of the box. As a result, when deploying just one instance of Eureka server, you will see some exceptions related to Eureka not finding its peers. Although these are just warnings, they may distract from other problems.  
With the configurations above, we disable this feature and make the Eureka instance run as a standalone server. Note, that for productive uses this is not recommended! See the Eureka documentation for more details.

With the configurations above, the Eureka server instance will use the hostname as given by the first URI in the `VCAP_APPLICATION` environment variable, if it is defined. Otherwise it will fall back to `localhost`. Thus, the Eureka Spring Boot application is runnable both locally as well as in Cloud Foundry.

### Eureka Server Manifest.yml

To deploy the Eureka server to Cloud Foundry, the `manifest.yml` should look as follows:

```yaml
---
applications:
- name: <a unique ID>-eureka-server
  memory: 1024M
  disk_quota: 512M
  instances: 1
  buildpack: java_buildpack
  path: ./target/eureka.service-0.0.1-SNAPSHOT.jar
  
  routes: 
  - route: <a unique ID>-eureka-server.<your.cf.domain>
```

We have outsourced `<a unique ID>` and other deployment-specific variables to a dedicated [`manifest-variables.yml`](./manifest-variables.yml) file located at the root of this project.  
You can simply push Eureka server to Cloud Foundry by executing `cf push --vars-file ../manifest-variables.yml` from the `eureka.service` folder.  

## Eureka Clients

Eureka clients are services that register to Eureka server.  

Services use the `spring-cloud-starter-netflix-eureka-client` library to find, connect and register to Eureka server.  
The Eureka server needs to be configured in every client (service).  
Note, that this does **not mean** that it needs to be hard coded - it could also be injeced via an environment entry.

A service _registers_ itself by a given name. That name can be used by other services to _look up_ that service from the Eureka server (registry).

In this project, we have created 4 Eureka clients. 

1. [address.service](./address.service)
2. [address.service.client](./address.service.client)
3. [employee.service](./employee.service)
4. [employee.service.client](./employee.service.client)

[address.service](./address.service) and [employee.service](./employee.service) are actual services, i.e. they provide a REST API that their clients are going to call. They register with Eureka Server and can subsequently be discovered by [address.service.client](./address.service.client) and [employee.service.client](./employee.service.client).

[address.service.client](./address.service.client) and [employee.service.client](./employee.service.client) are also realized as services, even though they act as clients to `address.service` and `employee.service`, respectively.   
Since they have also been configured as Eureka Clients, they also register with Eureka Server. This may seem confusing, but in reality, a consumer (client) of a service is often a service itself, so it makes sense to have them register to the service registry as well.

In the following we will show the configurations of one client-service-pair using [address.service](./address.service) and [address.service.client](./address.service.client). The descriptions are analogous for [employee.service](./employee.service) and [employee.service.client](./employee.service.client).

### Address Service 

[address.service](./address.service) is a plain Spring Boot application that exposes a REST controller serving addresses (city, postal code, etc.).  
To run [address.service](./address.service), proceed as follows:

- In `address.service` folder execute `mvn clean package`
- Execute `java -jar ./target/address.service-0.0.1-SNAPSHOT.jar`

To deploy to Cloud Foundry, proceed as follows:

- Login to your account and space using `cf login`
- In `address.service` folder, execute `mvn clean package`
- Execute `cf push --vars-file ../manifest-variables.yml`

You can deploy Eureka Server and `address.service` in any order you like. They will find each other! 
If you have Eureka Server running, you can inspect `http://localhost:8761/eureka` or `https://<a unique ID>-eureka-server.<your.cf.domain>/eureka` respectively. This will show you the server's Web UI where you should see `address.service` registered now (together with information about the service instance(s)).

#### Maven Dependencies
 In code there is nothing specific to Eureka.  
 However, you need to have the proper Eureka client dependencies on your classpath and in your `pom.xml`

```xml
<dependencies>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    
    <!-- Required for health checks and info pages advertised by the service and read by Eureka -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>

...

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>Greenwich.RELEASE</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### Address Service Eureka Client Configuration

By having `spring-cloud-starter-netflix-eureka-client` on its classpath, Address Service automatically acts as a Eureka client - meaning, it will try to look for a Eureka Server instance to register itself to.

In order to succeed, you need the following configurations in [`application.yml`](./address.service/src/main/resources/application.yml):

```yaml
---
spring:
  application:
    name: address-service

server:
  port: ${PORT:8080}

eureka:
  client: 
    serviceUrl:
      defaultZone: http://localhost:8761/eureka
  instance:
    healthcheck: 
      enabled: true

---
... # cloud profile (see below)
```

This is a little more involved, as it uses **Spring Cloud Profiles** and some environment variables set in `manifest.yml`.

First, we specify the name of the service - `address-service` - using the `spring.application.name`.  
Then we specify the port that the service will be running on. Again, this tries to read the port from the environment, and if not set, will default to port `8080`.

Next, using `eureka.client.serviceUrl.defaultZone` we specify the Eureka server URI, where to connect and register to. In a local environment, this will be a URI pointing to `localhost`. However, we will see below, how this can be overridden in a Cloud deployment scenario.

The `eureka.instance.healthcheck.enabled` property tells the Eureka Client not just to send a _heartbeat_ to the server to indicate it is healthy, but actually the _application-defined_ health status which can be controlled using **Spring Actuator** framework. Though not strictly necessary, it allows a Spring Boot application like `address.service` to expose a `/health` endpoint returning a JSON structure that can contain application-defined health information. See the [Spring Cloud Netflix documentation](https://cloud.spring.io/spring-cloud-netflix/single/spring-cloud-netflix.html) for details.

### Cloud Deployment Configurations

When deploying to Cloud Foundry, we need slightly different configurations.  
We use Spring Cloud Profiles to achieve this:

```yaml
---
... # default profile (see above)
      
---
spring.profiles: cloud

eureka:
  client: 
    serviceUrl:
      defaultZone: ${eureka-server-url}/eureka  # URL of the form https://<unique prefix>-eureka-server.<your.cf.domain>/eureka
                                                # Resolved from environment set in manifest.yml
  instance:
    ## set the host name explicitly when in Cloud Foundry.
    hostname:             ${vcap.application.uris[0]:localhost}
    nonSecurePortEnabled: false
    securePortEnabled:    true
    securePort:           443
    homePageUrl:          https://${vcap.application.uris[0]:localhost}/   
    statusPageUrl:        https://${vcap.application.uris[0]:localhost}/actuator/info 
    healthCheckUrl:       https://${vcap.application.uris[0]:localhost}/actuator/health
    secureHealthCheckUrl: https://${vcap.application.uris[0]:localhost}/actuator/health
```

First we define a new profile named `cloud` using the `spring.profiles: cloud` directive.  
All properties below it, will only be active, if the `cloud` profile is active. 

We need to set `eureka.client.serviceUrl.defaultZone` to point to the deployment URL of the Eureka server instance.  
We could hardcode the value according to the route of our deployed Eureka server, but a better approach is to look up the Eureka server URL from the environment.  
The line `defaultZone: ${eureka-server-url}/eureka` does exactly that. It searches for a variable `eureka-server-url` in the environment of the `address.service` application. We make sure that the `eureka-server-url` is in the environment by adding it in `address.service`'s [`manifest.yml`](./address.service/manifest.yml).

```yaml
...
  env:
    eureka-server-url: https://...
```

In a Cloud Foundry environment we need to explicitly set `eureka.instance.hostname` (i.e. the host name of the Eureka client).
A simple way to do that, without hardcoding it, is to consult the `VCAP_APPLICATION` environment variable that Cloud Foundry injects.  
In the configuration above, we use `vcap.application.uris[0]` to reference the first URI found in the `VCAP_APPLICATION` environment of `address.service`. At runtime, this will be the hostname of the route that is maintained in `manifest.yml`.  
If no URI was found, we default to `localhost`.

Next, we enforce HTTPS. We do so by disabling HTTP (`nonSecurePortEnabled: false`), enabling HTTPS (`securePortEnabled: true`) and specifying which port to use for HTTPS (`securePort: 443`).

As a last configuration step, we need to explicitly expose the URLs for ...
- home page
- status page
- health check URL and
- secure health check URL, 
... that will be exposed by `address.service` when it registers to Eureka. You will find the home page as a link in the Eureka UI (just click on an instance).  
The rest will be exposed as metadata and can be looked up by Eureka clients, i.e. other services.

Finally, we need to make sure that the cloud profile is activated, when we are deploying to Cloud Foundry.  
A Spring Cloud profile can be activated in various ways. The mechanism we use, is to set the `spring.profiles.active` environment variable which we set to `cloud` in [`manifest.yml`](./address.service/manifest.yml).

#### Address Service Manifest.yml

The `manifest.yml` for `address.service` is rather straight forward.

```yaml
---
applications:
- name: ((unique-prefix))-address-service
  memory: 1024M
  disk_quota: 512M
  instances: 1
  buildpacks:
    - java_buildpack
  path: ./target/address.service-0.0.1-SNAPSHOT.jar
  
  routes: 
  - route: ((unique-prefix))-address-service.<your.cf.domain>
  
  env:
    spring.profiles.active: cloud # activate the spring profile named 'cloud'.
    eureka-server-url: https://((eureka-server-route))
```

Note the `route` property. This will reflect in the URI in `VCAP_APPLICATION` that we referenced in the Eureka Client configuration.  
We are using a manifest variable `unique-prefix` which we maintain in `manifest-variables.yml`. This makes it easy to change the unique name prefix depending on your needs or landscape.

Also note the `spring.profiles.active` environment entry that activates the `cloud` profile we defined in `application.yml`.

Finally, note that we are injecting the `eureka-server-url` into the environment (again making use of a variable from `manifest-variables.yml`).  
This environment variable is referenced by `application.yml` as well.

With this relatively simple and straight-forward set of configurations, we have achieved:
- deployment on Cloud Foundry
- local deployment
- outsourcing of user- / landscape-specific prefixes and variables.

This is a highly flexible setup that will allow you to deploy and test both locally as well as in the Cloud!

### Address Service Client

`address.service.client` is a plain Spring Boot application. It declares the same dependencies to `spring-cloud-starter-netflix-eureka-client` and `spring-boot-starter-actuator` as `address.service`. Thus, it also acts as a Eureka Client and registers itself with the Eureka Server.  
Also, `address.service.client`'s `application.yml` is very similar to that of `address.service`.

It is interesting, however, how `address.service.client` uses Eureka Server to **discover** `address.service` from the registry and then calls it.  
This is codified in [3 different ways](https://spring.io/blog/2015/01/20/microservice-registration-and-discovery-with-spring-cloud-and-netflix-s-eureka) in the following classes:

* [DCAddressServiceClient](./address.service.client/src/main/java/com/sap/cloud/address/service/client/DCAddressServiceClient.java) - an implementation based on `DiscoveryClient`, which programmatically queries Eureka for service instances.
* [ETAddressServiceClient](./address.service.client/src/main/java/com/sap/cloud/address/service/client/ETAddressServiceClient.java) - an implementation using *loadbalanced* `RestTemplate`. 
* [FeignAddressServiceClient](./address.service.client/src/main/java/com/sap/cloud/address/service/client/FeignAddressServiceClient.java) - an implementation using Spring Cloud's declarative REST client Open Feign.

In the following, we will describe these approaches in more detail.

#### Service Discovery with Discovery Client

In this implementation, we use Spring's `DiscoveryClient` and `EurekaClient` from Netflix to lookup services instances in Eureka, retrieve their URLs and calling them using RestTemplate (or any other HTTP client).  

Both Eureka clients are possible options. However, `EurekaClient` provides a lot more details about registered service instances.   
Spring's `DiscoveryClient` is an abstraction intended for service lookups from any supported service registry - not just Eureka. As such, its capabilities are more limited.

```java
public class DCAddressServiceClient {
    
    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private EurekaClient eurekaClient;

    public void getAddress() throws RestClientException, IOException {
        
        String baseUrlFromEurekaClient = getServiceURLwithEurekaClient();
        String baseUrlFromSpringDiscoveryClient = getServiceURLwithSpringDiscoveryClient();
        
        System.out.println("--> Service URL from Eureka Client: " + baseUrlFromEurekaClient);           // with    trailing '/' 
        System.out.println("--> Service URL from Spring Client: " + baseUrlFromSpringDiscoveryClient);  // without trailing '/'

        String baseUrl = baseUrlFromSpringDiscoveryClient;
        baseUrl = baseUrl + "/address";

        System.out.println("Address Service Instance Address: " + baseUrl);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(baseUrl, HttpMethod.GET, getHeaders(), String.class);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        System.out.println(response.getBody());
    }
    ...

    private String getServiceURLwithSpringDiscoveryClient() {
        
        List<ServiceInstance> instances = discoveryClient.getInstances("address-service");
        
        dumpServiceInstancesInformationFromSpringDiscoveryClient(instances);
        
        ServiceInstance serviceInstance = instances.get(0);
        return serviceInstance.getUri().toString();
    }

    ...
    
    private String getServiceURLwithEurekaClient() throws JsonProcessingException {

        List<InstanceInfo> serviceInstances = eurekaClient.getInstancesByVipAddress("address-service", false);

        dumpServiceInstancesInformationFromEurekaClient(serviceInstances);

        InstanceInfo instanceInfo = serviceInstances.get(0);
        return instanceInfo.getHomePageUrl();
    }
}
```

In the snippet above, a `DiscoveryClient` and `EurekaClient` instance is autowired and used to get instances of a service that is registered by a given name to Eureka - in this case `address-service`.  

Every service instance has a URI by which it can be accessed by an HTTP client. But there are other interesting properties.  
For exmaple, a service instance may offer *metadata*. That metadata can be customized and might contain information about the service's (not necessarily REST-only) APIs. This metadata can be used by clients to find out more about the capabilities or features of the service.

Since there may be several instances of the service running, both `DiscoveryClient` and `EurekaClient` return references to each of them.   
Clients can thus pick a specific instance to communicate with.

[Netflix Zuul](https://github.com/Netflix/zuul/wiki) and [Netflix Ribbon](https://github.com/Netflix/ribbon) framework use this mechanism to realize load balancing.   

Note, that `DiscoveryClient` is a Spring Cloud abstraction on top of `EurekaClient` which acts as an implementation and needs to be on the classpath by including the dependency `spring-cloud-starter-netflix-eureka-client` in pom.xml

#### Service Discovery Using Loadbalanced RestTemplate

`RestTemplate` is one of Spring's way to make REST calls. Underlying `RestTemplate` is an HTTP client implementation that can differ depending on what is on the classpath. In particular, if your classpath contains Netflix Ribbon - a client-side loadbalancer - `RestTemplate` will use Ribbon to make (loadbalanced) HTTP requests.

Ribbon, on the other hand, has a tight integration with Eureka, and is capable of dynamically discovering service instances registered to Eureka and balancing load between them.

As a result, using Ribbon in combination with `RestTemplate` makes service discovery and calling services easier. [`ETAddressServiceClient`](./address.service.client/src/main/java/com/sap/cloud/address/service/client/ETAddressServiceClient.java) makes use of this approach:

```java
public class ETAddressServiceClient {

    @Autowired
    private RestTemplate restTemplate;
    
    public void getAddress() throws RestClientException, IOException {
        Address address = restTemplate.getForObject("http://address-service/address", Address.class);
        
        System.out.println("Address from RestTemplate: ");
        System.out.println(address.toString());
    }
}
```

The `RestTemplate` is autowired and used to look up and call the remote `address.service`'s endpoint.  

Note in particular, that the URI `http://address-service/address` **does not specify the real service URL!**  
Instead, it references the service's ID (`address-service`) as registered in Eureka (i.e. the `vip address` in Netflix terms).   
Ribbon's HTTP client will look up the service, pick a service instance and then call its `/address` endpoint. The JSON returned by the endpoint is then unmarshalled into an `Address` object instance, automatically.

For this to work, two more things are required.  
First, you need to annotate your Spring Boot application class with `@EnableDiscoveryClient`:

```java
@SpringBootApplication
@EnableDiscoveryClient
public class ClientApp {
  ...
}
```

Second, you need to declare a `@Bean` returning a *loadbalanced* `RestTemplate` in your Spring Boot configuration:

```java
@Bean
@LoadBalanced
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

Note the `@LoadBalanced` annotation, which automatically injects `Ribbon` as the underlying load balancer into the returned `RestTemplate` instance. 

#### Service Discovery Using Feign Client

Feign client is a declarative REST client developed and open-sourced by Netflix and made available by Spring Cloud.

Feign Client integrates tightly with Eureka and can enormeously simplify discovery and REST-based access to remote services.

[`FeignAddressServiceClient`](./address.service.client/src/main/java/com/sap/cloud/address/service/client/FeignAddressServiceClient.java) makes use of this approach:

```java
@Component
public class FeignAddressServiceClient {

    @Autowired
    private AddressServiceProxy addressServiceProxy;

    public void getAddress() {
        Address address = addressServiceProxy.loadAddress();
        
        System.out.println("Address from FeignClient: ");
        System.out.println(address.toString());
    }
}

@FeignClient("address-service") // 'address-service' is the name of the service in Eureka!
interface AddressServiceProxy {
    @RequestMapping(method = RequestMethod.GET, value = "/address")
    Address loadAddress();
}
```

In this example, `AddressServiceProxy` is simply an annotated interface that serves as a client-side proxy for the remote `address.service` and will be backed / implemented by FeignClient. The `@FeignClient("address-service")` annotation declares the ID of the service to call as registered in Eureka. The `@RequestMapping` annotation on the `loadAddress()` method specifies the path / endpoint to call - this may also include variables that can be resolved with method parameters (see [details here](https://spring.io/blog/2015/01/20/microservice-registration-and-discovery-with-spring-cloud-and-netflix-s-eureka)).

`FeigAddressServiceClient` then simply autowires an instance of `AddressProxy` created by Feign and calls its `loadAddress()` method.

For this to work, you need to do two more things.
First, you need to add the following dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

Second, you need to annotate your Spring Boot application class with `@EnableFeignClients`:

```java
@SpringBootApplication
@EnableFeignClients
public class ClientApp {
  ...
}
```

**Note:** Both loadbalanced `RestTemplate` and `FeignClient` use the service ID / alias name as registered in Eureka rather than the exact physical URL of the service instance. This abstraction not only hides the fact that there may be multiple service instances from clients, but it also provides a *logical grouping* of service instances into a *logical service*. This abstraction makes it possible to change and exchange service instances "under the hood" without clients being aware of it. This opens the door for powerful dev ops strategies like blue-green, canary and with it true zero-downtime deployments!

# What did we gain so far? - Or: Why The Hassle?

If you have read the last section carefully and know a little bit about Cloud Foundry you may have been thinking: "Cloud Foundry already has a load balancer! So,  even if I have multiple instances of a service registered, they all share the same URI, why do I need Eureka then? I cannot pick a dedicated instance to talk to anyway!"

Indeed, Cloud Foundry already provides its own load balancer: the Go-Router.   
In fact, if you deploy `eureka.service` and `address.service` to the Cloud and create 3 instances of `address-service` they will each share the exact same URI, i.e. Cloud Foundry **route**.  
Hence, a client cannot connect to a specific instance. Instead, a client makes a call to the given URI and Cloud Foundry's Go-Router will route / balance the request to an instance it deems fit to handle it.

However, using Eureka you gain a dynamic mechanism to add and remove services from your Cloud (product) landscape. This allows you to provide a runtime catalog of available services to your Cloud users, and allows technical clients to discover the endpoints they need to communicate with. Paired with meaningful metadata exposed by your services, this can be powerful feature.

Furthermore, Eureka can not only be valuable for your customers but also your operations teams.  
Consider the following scenario: 

- You have v1 of your service deployed and it exposes its version information `version: 1.0.0` as metadata.
- You may have multiple instances of that service running, and Eureka will return each of them by the same route.
- You have a smart / dynamic router (like Netflix Zuul or Hybris' Jupiter) connected to Eureka and all services and service clients always communicate via that router
- Now if you have a v2 of your service ready, and would like to switch some or all of the clients using v1 of your service to v2.
- Given that the smart router supports it, you could now do the following:
  - You deploy v2 of your service to Cloud Foundry and have it expose `version: 2.0.0` as metadata.
  - You have v2 of your service register to Eureka server by the same logical service name as v1, but they register by a new route.
  - Clients do not see the new route, they only see the logical service name, so they will not even notice there is a v2 of your service available under the hood.
  - The dynamic router will now be able to look up the new instances and be able to distinguish them by the different version metadata.
  - Based on the metadata of a service instance returned by Eureka, the dynamic router can now decide where to route specific clients' requests. For example, if there is a list of clients that should act as canary users for v2 of your service, the dynamic router could read that list and dynamically route all requests from the given clients to the instances of `version: 2.0.0`.

This allows you to switch clients / users to new versions of your software without them noticing, thus incurring no downtime at all.

# Using Custom Service Metadata

One major benefit of Eureka is that services can provide metadata describing their instances. This metadata is passed to Eureka server when the service registers.  
Clients can inspect that metadata when they look up services they want to consume. If you are using Netflix Ribbon or Zuul, then you can create rules or filters that act upon that metadata to select service instances to balance traffic or route clients to.

There is a certain set of default metadata that all Eureka instances will provide (e.g. the management port, the JMX port, etc.). But the true power lies in the ability to provide custom metadata. This metadata could e.g. be:
- a description of the service
- links to documentation
- links to API endpoint descriptors (swagger files)
- descriptions of events the service emits, together with their structure and semantics.
- version information
- landscape / data center zone information
- etc.

The way this is done with Eureka is very simple:
1. You can statically define custom metadata in Eureka client configs, i.e. `application.yml`. We show this below.
2. You can [dynamically define the metadata in code](https://blog.codecentric.de/en/2018/01/spring-cloud-service-discovery-dynamic-metadata/)

## Static Custom Service Metadata 

The sample configuration below declares custom service metadata using the `eureka.instance.metadata-map` property.
This is the static way of declaring metadata. In this case we pretend that the service emits events (e.g. from Kafka or RabbitMQ), and describes the type and payload of the events using custom metadata.

```yaml
---
spring:
  application:
    name: address-service

server:
  port: ${PORT:8080}

eureka:
  client: 
    serviceUrl:
      defaultZone: http://localhost:8761/eureka
  instance:
    metadata-map:
      instanceId: "${vcap.application.instance_id:-}"
      events: > 
        { 
          [
            {
              "type" : "customerCreated", 
              "registry" : "https://kafka.registry.com"
            }, 
            {
              "type" : "customerDeleted", 
              "registry" : "https://kafka.registry.com"
            }
          ]
        }
```
**Note:** If you deploy `address.service` and `address.service.client`, you will see that when `address.service.client` starts up, it will look up an instance of `address-service` and print out the metadata it retrieved from the instance.  
You can try this out locally, or on Cloud Foundry. The metadata is printed to standard output.

# References
* [Spring Cloud Netflix Documentation](https://cloud.spring.io/spring-cloud-netflix/single/spring-cloud-netflix.html)
* [Understanding Eureka Peer-2-Peer Communication](https://github.com/Netflix/eureka/wiki/Understanding-Eureka-Peer-to-Peer-Communication)
* [Self Preservation: Why Eureka keeps showing warnings in the UI](https://groups.google.com/forum/#!searchin/eureka_netflix/chris/eureka_netflix/6pVPVjIMiG0/YAn3YrvWNN0J)
* [NetflixOSS FAQ](https://github.com/cfregly/fluxcapacitor/wiki/NetflixOSS-FAQ)
* [Using Custom Service Metadata](https://blog.codecentric.de/en/2018/01/spring-cloud-service-discovery-dynamic-metadata/)
* [Cloud Foundry Headers to Route Requests to Specific Instances](https://docs.cloudfoundry.org/concepts/http-routing.html#app-instance-routing)
* https://www.javainuse.com/spring/springcloud
* https://www.javainuse.com/spring/spring_eurekaregister
* https://www.javainuse.com/spring/spring_eurekaregister2
* https://www.javainuse.com/spring/spring_eurekaregister3
* https://www.javainuse.com/spring/spring_eurekaregister4