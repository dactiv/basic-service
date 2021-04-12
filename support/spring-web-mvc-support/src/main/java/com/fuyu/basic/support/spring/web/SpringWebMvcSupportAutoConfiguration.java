package com.fuyu.basic.support.spring.web;


import com.fuyu.basic.support.spring.web.argument.DeviceHandlerMethodArgumentResolver;
import com.fuyu.basic.support.spring.web.argument.GenericsListHandlerMethodArgumentResolver;
import com.fuyu.basic.support.spring.web.endpoint.EnumerateEndpoint;
import com.fuyu.basic.support.spring.web.interceptor.InjectionClientHttpRequestInterceptor;
import com.fuyu.basic.support.spring.web.mobile.DeviceResolverHandlerInterceptor;
import com.fuyu.basic.support.spring.web.interceptor.LoggingClientHttpRequestInterceptor;
import com.fuyu.basic.support.spring.web.result.RestResponseBodyAdvice;
import com.fuyu.basic.support.spring.web.result.RestResultErrorAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * spring mvc 自动配置实现
 *
 * @author maurice.chen
 */
@Configuration
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)
@ConditionalOnProperty(prefix = "spring.web.mvc.support", value = "enabled", matchIfMissing = true)
public class SpringWebMvcSupportAutoConfiguration {

    @Autowired(required = false)
    private List<InfoContributor> infoContributors;

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public static class DefaultWebMvcConfigurer implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
            argumentResolvers.add(new GenericsListHandlerMethodArgumentResolver(getValidator()));
            argumentResolvers.add(new DeviceHandlerMethodArgumentResolver());
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new DeviceResolverHandlerInterceptor());
        }
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public RestResultErrorAttributes servletRestResultErrorAttributes() {
        return new RestResultErrorAttributes();
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public RestResponseBodyAdvice restResponseBodyAdvice() {
        return new RestResponseBodyAdvice();
    }

    @Bean
    @ConfigurationProperties("enumerate")
    public EnumerateEndpoint enumerateEndpoint() {
        return new EnumerateEndpoint(infoContributors);
    }

    @Bean
    public LoggingClientHttpRequestInterceptor loggingClientHttpRequestInterceptor() {
        return new LoggingClientHttpRequestInterceptor();
    }

    @Bean
    public RestTemplate restTemplate(List<InjectionClientHttpRequestInterceptor> clientHttpRequestInterceptors) {
        RestTemplate restTemplate = new RestTemplate();

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(clientHttpRequestInterceptors);

        restTemplate.setInterceptors(interceptors);

        return restTemplate;
    }
}
