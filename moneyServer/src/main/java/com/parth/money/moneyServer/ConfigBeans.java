package com.parth.money.moneyServer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ConfigBeans {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
