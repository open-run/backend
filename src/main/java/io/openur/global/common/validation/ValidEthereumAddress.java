package io.openur.global.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EthereumAddressValidator.class)
public @interface ValidEthereumAddress {
    String message() default "Invalid Ethereum address format. Must be 42 characters long, start with '0x', and contain valid hex characters.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
} 