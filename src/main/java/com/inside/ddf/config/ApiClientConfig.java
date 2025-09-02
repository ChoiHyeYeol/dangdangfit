package com.inside.ddf.config;

//ApiClientConfig.java
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class ApiClientConfig {

 @Bean
 public WebClient fastApiClient() {
     HttpClient httpClient = HttpClient.create()
             .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
             .responseTimeout(Duration.ofSeconds(40))
             .doOnConnected(conn -> conn
                     .addHandlerLast(new ReadTimeoutHandler(40, TimeUnit.SECONDS))
                     .addHandlerLast(new WriteTimeoutHandler(40, TimeUnit.SECONDS))
             );

     return WebClient.builder()
             .baseUrl("http://localhost:8000") // FastAPI 주소
             .clientConnector(new ReactorClientHttpConnector(httpClient))
             .build();
 }
}
