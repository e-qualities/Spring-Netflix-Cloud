# Hystrix Dashboard

This project is a Spring Boot application which displays a Hystrix dashboard.  

If you deploy this application locally, the Hystrix dashboard will be available by the following URL:

* [`http://localhost:8999/hystrix`](http://localhost:8999/hystrix)

For Cloud deployments using `cf push` the URL will be 
* `https://<your D/I/C-user>.cfapps.eu10.hana.ondemand.com/hystrix`.

For more information on how to use Hystrix Dashboard, see [this tutorial](https://www.youtube.com/watch?v=Kc7dDxn9cUg) and see the References section below.

# Trying out Hystrix Dashboard

Also note that services `address.service` and `address.service.client` have been modified to show Hystrix in action.  
So to try out Hystrix dashboard, you can proceed as follows:

1. Start `eureka.service`
1. Start `address.service`
1. Start `address.service.client`
1. Start `hystrix-dashboard`
1. Open `http://localhost:8999/hystrix`
1. Enter the `/actuator/hystrix.stream` endpoint of `address.service.client` - i.e. `http://localhost:8081/actuator/hystrix.stream` - into the Hystrix dashboard.
1. Click on **Monitor Stream**

Now several times execute a call to `address.service.client` by continuously refreshing `http://localhost:8081/call-address-service`.  
You will see Hystrix dashboard showing updating stats, which are being read from the `/actuator/hystrix.stream` endpoint of `address.service.client`

If you want to see the circuit popping open at some point, you can try flooding the `address.service.client` with calls like so:

```
bash -c 'while [ 0 ]; do curl http://localhost:8081/call-address-service; done'
```

This will fire requests in a fast pace against the endpoint of `address.service.client` that calls `address.service`. 

# References

* [Hystrix Spring Cloud Tutorial](https://www.youtube.com/watch?v=Kc7dDxn9cUg)
* [Spring Cloud Hystrix Documentation](https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.1.0.RELEASE/single/spring-cloud-netflix.html#_circuit_breaker_hystrix_clients)