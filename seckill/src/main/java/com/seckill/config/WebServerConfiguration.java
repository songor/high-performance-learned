package com.seckill.config;

import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * 当 Spring 容器内没有 TomcatEmbeddedServletContainerFactory Bean 的时候加载
 */
@Component
public class WebServerConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    @Value("${custom.server.tomcat.keep-alive-time-out}")
    private Integer keepAliveTimeout;

    @Value("${custom.server.tomcat.max-keep-alive-requests}")
    private Integer maxKeepAliveRequests;

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        ((TomcatServletWebServerFactory) factory).addConnectorCustomizers((TomcatConnectorCustomizer) connector -> {
            Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
            protocol.setKeepAliveTimeout(keepAliveTimeout);
            protocol.setMaxKeepAliveRequests(maxKeepAliveRequests);
        });
    }

}
