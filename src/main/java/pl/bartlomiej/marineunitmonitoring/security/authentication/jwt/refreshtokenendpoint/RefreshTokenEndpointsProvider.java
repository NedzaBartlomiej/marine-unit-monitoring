package pl.bartlomiej.marineunitmonitoring.security.authentication.jwt.refreshtokenendpoint;

import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RefreshTokenEndpointsProvider {

    private final ApplicationContext applicationContext;
    private final List<String> refreshTokenPaths = new ArrayList<>();

    public RefreshTokenEndpointsProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods =
                applicationContext.getBean(RequestMappingHandlerMapping.class).getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            if (entry.getValue().hasMethodAnnotation(RefreshTokenConsumer.class)) {
                entry.getKey().getPatternsCondition().getPatterns()
                        .forEach(pattern -> refreshTokenPaths.add(pattern.getPatternString()));
            }
        }
    }

    public List<String> getRefreshTokenPaths() {
        return refreshTokenPaths;
    }
}
