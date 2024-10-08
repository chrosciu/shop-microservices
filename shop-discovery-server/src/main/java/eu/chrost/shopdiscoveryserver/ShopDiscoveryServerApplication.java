package eu.chrost.shopdiscoveryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ShopDiscoveryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopDiscoveryServerApplication.class, args);
    }

}
