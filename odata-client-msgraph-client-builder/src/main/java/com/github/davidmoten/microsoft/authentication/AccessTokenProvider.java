package com.github.davidmoten.microsoft.authentication;

import java.util.function.Supplier;

@FunctionalInterface
public interface AccessTokenProvider extends Supplier<String> {

}
