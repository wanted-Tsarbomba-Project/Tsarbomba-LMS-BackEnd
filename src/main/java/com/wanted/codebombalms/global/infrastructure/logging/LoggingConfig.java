package com.wanted.codebombalms.global.infrastructure.logging;

import com.wanted.codebombalms.serviceevent.infrastructure.persistence.ServiceEventWriter;
import com.wanted.codebombalms.serviceevent.infrastructure.web.HttpAnomalyGuard;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class LoggingConfig {

    @Bean
    public FilterRegistrationBean<MdcLoggingFilter> mdcLoggingFilter(
            ServiceEventWriter serviceEventWriter,
            HttpAnomalyGuard httpAnomalyGuard,
            @Value("${service-event.anomaly.slow-threshold-ms:3000}") long slowThresholdMs) {
        FilterRegistrationBean<MdcLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new MdcLoggingFilter(serviceEventWriter, httpAnomalyGuard, slowThresholdMs));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}
