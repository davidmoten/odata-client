package com.github.davidmoten.msgraph.builder;

import java.util.function.Supplier;

@FunctionalInterface
public interface AccessTokenProvider extends Supplier<String> {

}
