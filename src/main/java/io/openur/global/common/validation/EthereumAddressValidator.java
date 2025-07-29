package io.openur.global.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class EthereumAddressValidator implements ConstraintValidator<ValidEthereumAddress, String> {
    private static final Pattern ETH_ADDRESS_PATTERN = Pattern.compile("^0x[a-fA-F0-9]{40}$");
    private static final int ETH_ADDRESS_LENGTH = 42; // 0x + 40 hex characters

    @Override
    public void initialize(ValidEthereumAddress constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String address, ConstraintValidatorContext context) {
        if (address == null || address.isEmpty()) {
            return false;
        }

        // Check length and format
        return address.length() == ETH_ADDRESS_LENGTH && ETH_ADDRESS_PATTERN.matcher(address).matches();
    }

    public boolean isValid(String address) {
        return isValid(address, null);
    }
} 