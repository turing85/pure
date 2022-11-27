package de.turing85.pure.result;

/**
 * Represents a function that may throw an exception.
 *
 * @param <T> the type of the input parameter.
 * @param <R> the type of the return value.
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> {
  /**
   * Maps a parameter of type {@code T} to a value of type {@code R}.
   *
   * @param t the input parameter.
   * @return the mapped value.
   * @throws Exception the exception thrown.
   */
  R apply(T t) throws Exception;
}
