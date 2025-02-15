package com.example.stockmarketsimulator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

//import javax.annotation.PostConstruct;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

//    @dPostConstruct
//    public void init() {
//        // Set a default User-Agent for HTTP requests (used by YahooFinance library)
//        if (System.getProperty("http.agent") == null) {
//            System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
//        }
//    }
}
