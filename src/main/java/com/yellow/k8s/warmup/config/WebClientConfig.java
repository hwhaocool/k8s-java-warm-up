package com.yellow.k8s.warmup.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import javax.net.ssl.SSLException;

@Configuration
@ConfigurationProperties(prefix = "k8s")
public class WebClientConfig {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WebClientConfig.class);
    
    private String masterUrl;        // api-server url
    
    private String token;            // token

    public String getMasterUrl() {
        return masterUrl;
    }

    public void setMasterUrl(String masterUrl) {
        this.masterUrl = masterUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    @Bean
    public ReactorResourceFactory resourceFactory() {
        ReactorResourceFactory factory = new ReactorResourceFactory();
        factory.setUseGlobalResources(false);
        return factory;
    }
    
    @Bean(name = "k8sClient")
    public WebClient k8sClient() throws SSLException {

        ReactorResourceFactory factory = new ReactorResourceFactory();
        factory.setConnectionProvider(ConnectionProvider.create("webflux-k8s", 5));
        factory.setLoopResources(LoopResources.create("webflux-k8s"));
        factory.setUseGlobalResources(false);

        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        ClientHttpConnector httpConnector =
                new ReactorClientHttpConnector(factory ,
                        opt -> opt.secure(t -> t.sslContext(sslContext)));

        return   WebClient.builder()
                .clientConnector(httpConnector)
                .defaultHeader("Authorization", String.format("Bearer %s", token))
                .baseUrl(masterUrl)
                .build();
    }

    @Bean(name = "restClient")
    public WebClient restClient() {

        return WebClient.builder()
                .build();
    }

    

}
