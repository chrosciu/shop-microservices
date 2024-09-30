package eu.chrost.shopservice;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
@RequiredArgsConstructor
class ShopController {
    private final GreetingClient greetingClient;

    @Value("${shop.name}")
    private String shopName;

    @GetMapping
    String getShopInfo() {
        return shopName;
    }

    @GetMapping("/hello")
    String hello() {
        return greetingClient.hello();
    }
}

