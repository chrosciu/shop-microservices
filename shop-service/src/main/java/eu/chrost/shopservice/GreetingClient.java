package eu.chrost.shopservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("greeting")
public interface GreetingClient {
    @GetMapping
    String hello();
}
