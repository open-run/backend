package io.openur.global.common.evmaddress;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that validates EVM address format and automatically converts to EVMAddress instance.
 * 
 * Usage:
 * @RequestParam @EVMAddressParam EVMAddress walletAddress
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface EVMAddressParam {
} 