package com.github.davidmoten.msgraph;

import java.util.function.Supplier;

@FunctionalInterface
public interface AccessTokenProvider extends Supplier<String> {

}
