package com.insurance.gateway.filter;


import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(
            org.springframework.web.server.ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();

        System.out.println("\n========== API GATEWAY REQUEST ==========");
        System.out.println("Method : " + method);
        System.out.println("Path   : " + path);

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {

                    Route route = exchange.getAttribute(
                            ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR
                    );

                    if (route != null) {
                        System.out.println("Route  : " + route.getId());
                        System.out.println("Target : " + route.getUri());
                    }

                    System.out.println(
                            "Status : " + exchange.getResponse().getStatusCode()
                    );

                    System.out.println("=========================================\n");
                }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
