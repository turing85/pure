package de.turing85.pure.result;

@FunctionalInterface
public interface ThrowingFunction<T, R> {
  R apply(T t) throws Exception;
}
