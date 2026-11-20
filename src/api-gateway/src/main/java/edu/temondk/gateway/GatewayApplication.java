package edu.temondk.gateway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
@SpringBootApplication public class GatewayApplication {
  public static void main(String[] args){SpringApplication.run(GatewayApplication.class,args);}
  @org.springframework.context.annotation.Bean KeyResolver ipKeyResolver(){return e->Mono.just(e.getRequest().getRemoteAddress()==null?"unknown":e.getRequest().getRemoteAddress().getAddress().getHostAddress());}
  // RequestRateLimiter commits an empty 429. Supply the declared RFC 7807 representation.
  @org.springframework.context.annotation.Bean GlobalFilter rateLimitProblem(){return new RateLimitProblemFilter();}
  static class RateLimitProblemFilter implements GlobalFilter, Ordered {
    @Override public int getOrder(){return Ordered.HIGHEST_PRECEDENCE;}
    @Override public Mono<Void> filter(ServerWebExchange exchange,GatewayFilterChain chain){
      var response=new ServerHttpResponseDecorator(exchange.getResponse()){
        @Override public Mono<Void> setComplete(){
          if(getStatusCode()==HttpStatus.TOO_MANY_REQUESTS && !isCommitted()){
            getHeaders().setContentType(MediaType.parseMediaType("application/problem+json"));
            String body="{\"type\":\"about:blank\",\"title\":\"Too Many Requests\",\"status\":429,\"detail\":\"Rate limit exceeded\",\"instance\":\""+exchange.getRequest().getPath().value()+"\"}";
            byte[] bytes=body.getBytes(StandardCharsets.UTF_8);getHeaders().setContentLength(bytes.length);
            return writeWith(Mono.just(bufferFactory().wrap(bytes)));
          }
          return super.setComplete();
        }
      };
      return chain.filter(exchange.mutate().response(response).build());
    }
  }
}
