package com.abnamro.assignment.exception;

import org.springframework.validation.BindingResult;

public class ApplicationValidationException extends RuntimeException {
    private final BindingResult bindingResult;

    public ApplicationValidationException(BindingResult bindingResult) {
        super("Validation Failed");
        this.bindingResult = bindingResult;
    }

    public BindingResult getBindingResult() {
        return bindingResult;
    }
}
