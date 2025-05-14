package io.openur.global.common.validation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfig {
    
    @Bean
    public EthereumAddressValidator ethereumAddressValidator() {
        return new EthereumAddressValidator();
    }
} 