package io.openur.global.common.evmaddress;

import io.openur.domain.user.model.EVMAddress;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Argument resolver that automatically converts validated String parameters to EVMAddress instances.
 * This resolver works in conjunction with @EVMAddressParam annotation.
 */
public class EVMAddressArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(EVMAddressParam.class) &&
               parameter.getParameterType().equals(EVMAddress.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        
        // Get the parameter name from the annotation or use the parameter name
        String paramName = parameter.getParameterName();
        String value = webRequest.getParameter(paramName);
        
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("EVM address parameter is required");
        }
        
        // Validate and create EVMAddress instance
        if (!EVMAddress.isValid(value)) {
            throw new IllegalArgumentException("Invalid EVM address format");
        }
        
        return new EVMAddress(value);
    }
} 