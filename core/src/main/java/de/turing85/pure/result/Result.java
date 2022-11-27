package de.turing85.pure.result;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * This class represents a result from a method-/function-call, that may have complete successfully
 * or with an error.
 *
 * <p>
 * If the call completed successfully, {@link #value()} is set, and {@link #errorDescriptor()} is
 * empty. If the call completed with an error, {@link #value()} is empty, and
 * {@link #errorDescriptor()} is set.
 *
 * @param <V> the type of the {@link #value()}.
 * @param <D> the type of the {@link #errorDescriptor()}.
 */
@EqualsAndHashCode
@ToString
public class Result<V, D> {
  @ToString.Exclude
  private final boolean hasValue;
  private final V value;
  private final D errorDescriptor;

  /**
   * Invoke a method that throws an exception to wrap the return-value into a {@link Result}.
   *
   * @param callable the method to wrap.
   * @param <V> the type of the return-value of the method to wrap.
   *
   * @return the Result.
   */
  public static <V> Result<V, Exception> invoke(Callable<V> callable) {
    try {
      return Result.ofValue(callable.call());
    } catch (Exception e) {
      return Result.ofError(e);
    }
  }

  /**
   * Invoke a method and, depending on the {@code errorCondition}, decide whether to return a
   * {@link Result} with a {@link #value()} or an {@link #errorDescriptor()}.
   *
   * <p>
   * In the {@link #value()}-case, the {@link #value()} is set to the return-value of the
   * {@code supplier}.
   *
   * <p>
   * In the {@link #errorDescriptor()} ()}-case, the {@link #value()} is set to the return-value of
   * the {@code supplier}.
   *
   * @param supplier the method to invoke.
   * @param errorCondition the condition to decide whether the Result has a {@link #value()} or an
   *        {@link #errorDescriptor()}.
   * @param <V> The type of both {@link #value()} and {@link #errorDescriptor()}.
   *
   * @return the {@link Result} of the invocation of {@code supplier}.
   */
  public static <V> Result<V, V> invoke(Supplier<V> supplier, Predicate<V> errorCondition) {
    return invoke(supplier, errorCondition, Function.identity());
  }

  /**
   * Invoke a method and, depending on the {@code errorCondition}, decide whether to return a
   * {@link Result} with a {@link #value()} or an {@link #errorDescriptor()}.
   *
   * <p>
   * In the {@link #value()}-case, the {@link #value()} is set to the return-value of the
   * {@code supplier}.
   *
   * <p>
   * In the {@link #errorDescriptor()} ()}-case, the value returned by
   * {@code toErrorDescriptorMapper}, with the return-value of the {@code supplier} as parameter.
   *
   * @param supplier the method to invoke.
   * @param errorCondition the condition to decide whether the Result has a {@link #value()} or an
   *        {@link #errorDescriptor()}.
   * @param toErrorDescriptorMapper mapper to map the return-value of {@code supplier} to an
   *        {@link #errorDescriptor()}.
   * @param <V> The type of {@link #value()}.
   * @param <D> The type of {@link #errorDescriptor()}.
   *
   * @return the {@link Result} of the invocation of {@code supplier}.
   */
  public static <V, D> Result<V, D> invoke(Supplier<V> supplier, Predicate<V> errorCondition,
      Function<V, D> toErrorDescriptorMapper) {
    V value = supplier.get();
    if (errorCondition.test(value)) {
      return Result.ofError(toErrorDescriptorMapper.apply(value));
    } else {
      return Result.ofValue(value);
    }
  }

  /**
   * Invoke a method and, depending on the {@code errorCondition}, decide whether to return a
   * {@link Result} with a {@link #value()} or an {@link #errorDescriptor()}.
   *
   * <p>
   * In the {@link #value()}-case, the {@link #value()} is set to the return-value of the
   * {@code function}.
   *
   * <p>
   * In the {@link #errorDescriptor()} ()}-case, the {@link #value()} is set to the return-value of
   * the {@code function}.
   *
   * @param function the function to invoke.
   * @param parameter the parameter to pass to {@code function}.
   * @param errorCondition the condition to decide whether the Result has a {@link #value()} or an
   *        {@link #errorDescriptor()}.
   * @param <P> The type of the {@code parameter}.
   * @param <V> The type of both {@link #value()} and {@link #errorDescriptor()}.
   *
   * @return the {@link Result} of the invocation of {@code function}.
   */
  public static <P, V> Result<V, V> invoke(Function<P, V> function, P parameter,
      Predicate<V> errorCondition) {
    return invoke(function, parameter, errorCondition, Function.identity());
  }

  /**
   * Invoke a method and, depending on the {@code errorCondition}, decide whether to return a
   * {@link Result} with a {@link #value()} or an {@link #errorDescriptor()}.
   *
   * <p>
   * In the {@link #value()}-case, the {@link #value()} is set to the return-value of the
   * {@code function}.
   *
   * <p>
   * In the {@link #errorDescriptor()} ()}-case, the value returned by
   * {@code toErrorDescriptorMapper}, with the return-value of the {@code function} as parameter.
   *
   * @param function the function to invoke.
   * @param parameter the parameter to pass to {@code function}.
   * @param errorCondition the condition to decide whether the Result has a {@link #value()} or an
   *        {@link #errorDescriptor()}.
   * @param toErrorDescriptorMapper mapper to map the return-value of {@code function} to an
   *        {@link #errorDescriptor()}.
   * @param <P> The type of the {@code parameter}.
   * @param <V> The type of {@link #value()}.
   * @param <D> The type of {@link #errorDescriptor()}.
   *
   * @return the {@link Result} of the invocation of {@code function}.
   */
  public static <P, V, D> Result<V, D> invoke(Function<P, V> function, P parameter,
      Predicate<V> errorCondition, Function<V, D> toErrorDescriptorMapper) {
    V value = function.apply(parameter);
    if (errorCondition.test(value)) {
      return Result.ofError(toErrorDescriptorMapper.apply(value));
    } else {
      return Result.ofValue(value);
    }
  }

  /**
   * Invoke a method and, depending on the {@code errorCondition}, decide whether to return a
   * {@link Result} with a {@link #value()} or an {@link #errorDescriptor()}.
   *
   * <p>
   * In the {@link #value()}-case, the {@link #value()} is set to the return-value of the
   * {@code function}.
   *
   * <p>
   * In the {@link #errorDescriptor()} ()}-case, the {@link #value()} is set to the return-value of
   * the {@code function}.
   *
   * @param function the function to invoke.
   * @param parameterSupplier the supplier for the parameter to pass to {@code function}.
   * @param errorCondition the condition to decide whether the Result has a {@link #value()} or an
   *        {@link #errorDescriptor()}.
   * @param <P> The type of the {@code parameter}.
   * @param <V> The type of both {@link #value()} and {@link #errorDescriptor()}.
   *
   * @return the {@link Result} of the invocation of {@code function}.
   */
  public static <P, V> Result<V, V> invoke(Function<P, V> function, Supplier<P> parameterSupplier,
      Predicate<V> errorCondition) {
    return invoke(function, parameterSupplier.get(), errorCondition);
  }

  /**
   * Invoke a method and, depending on the {@code errorCondition}, decide whether to return a
   * {@link Result} with a {@link #value()} or an {@link #errorDescriptor()}.
   *
   * <p>
   * In the {@link #value()}-case, the {@link #value()} is set to the return-value of the
   * {@code function}.
   *
   * <p>
   * In the {@link #errorDescriptor()} ()}-case, the value returned by
   * {@code toErrorDescriptorMapper}, with the return-value of the {@code function} as parameter.
   *
   * @param function the function to invoke.
   * @param parameterSupplier the supplier for the parameter to pass to {@code function}.
   * @param errorCondition the condition to decide whether the Result has a {@link #value()} or an
   *        {@link #errorDescriptor()}.
   * @param toErrorDescriptorMapper mapper to map the return-value of {@code function} to an
   *        {@link #errorDescriptor()}.
   * @param <P> The type of the {@code parameter}.
   * @param <V> The type of {@link #value()}.
   * @param <D> The type of {@link #errorDescriptor()}.
   *
   * @return the {@link Result} of the invocation of {@code function}.
   */
  public static <P, V, D> Result<V, D> invoke(Function<P, V> function,
      Supplier<P> parameterSupplier, Predicate<V> errorCondition,
      Function<V, D> toErrorDescriptorMapper) {
    return invoke(function, parameterSupplier.get(), errorCondition, toErrorDescriptorMapper);
  }

  /**
   * Invoke a method and, depending on the {@code errorCondition}, decide whether to return a
   * {@link Result} with a {@link #value()} or an {@link #errorDescriptor()}.
   *
   * <p>
   * In the {@link #value()}-case, the {@link #value()} is set to the return-value of the
   * {@code function}.
   *
   * <p>
   * In the {@link #errorDescriptor()} ()}-case, the {@link #errorDescriptor()} is set to the
   * {@link Exception} thrown by the invocation of {@code function}.
   *
   * @param function the function to invoke.
   * @param parameterSupplier the supplier for the parameter to pass to {@code function}.
   * @param <P> The type of the {@code parameter}.
   * @param <V> The type of {@link #value()}.
   *
   * @return the {@link Result} of the invocation of {@code function}.
   */
  public static <P, V> Result<V, Exception> invoke(ThrowingFunction<P, V> function,
      Supplier<P> parameterSupplier) {
    return invoke(function, parameterSupplier.get());
  }

  /**
   * Invoke a method and, depending on the {@code errorCondition}, decide whether to return a
   * {@link Result} with a {@link #value()} or an {@link #errorDescriptor()}.
   *
   * <p>
   * In the {@link #value()}-case, the {@link #value()} is set to the return-value of the
   * {@code function}.
   *
   * <p>
   * In the {@link #errorDescriptor()} ()}-case, the {@link #errorDescriptor()} is set to the
   * {@link Exception} thrown by the invocation of {@code function}.
   *
   * @param function the function to invoke.
   * @param parameter the parameter to pass to {@code function}.
   * @param <P> The type of the {@code parameter}.
   * @param <V> The type of {@link #value()}.
   *
   * @return the {@link Result} of the invocation of {@code function}.
   */
  public static <P, V> Result<V, Exception> invoke(ThrowingFunction<P, V> function, P parameter) {
    try {
      return Result.ofValue(function.apply(parameter));
    } catch (Exception e) {
      return Result.ofError(e);
    }
  }

  /**
   * Factory-method.
   *
   * @param value the value.
   * @param <V> the type of the value.
   * @param <D> the type of the error-descriptor.
   *
   * @return a Result, representing the value (and a possible error that did not occur).
   */
  public static <V, D> Result<V, D> ofValue(V value) {
    return new Result<>(true, value, null);
  }

  /**
   * Factory-method.
   *
   * @param errorDescriptor the errorDescriptor.
   * @param <V> the type of the value.
   * @param <D> the type of the errorDescriptor-errorDescriptor.
   *
   * @return a Result, representing the errorDescriptor (and a value, that was not obtained, due to
   *         the error that occurred).
   */
  public static <V, D> Result<V, D> ofError(D errorDescriptor) {
    return new Result<>(false, null, errorDescriptor);
  }

  protected Result(boolean hasValue, V value, D errorDescriptor) {
    this.hasValue = hasValue;
    this.value = value;
    this.errorDescriptor = errorDescriptor;
  }

  /**
   * Method to check whether the call that created this result lead to no error.
   *
   * @return {@code true} if and only if the call that created this result lead to no error.
   */
  public boolean hasValue() {
    return hasValue;
  }

  /**
   * Method to check whether the call that created this result lead to an error.
   *
   * @return {@code true} if and only if the result has no value, and an error.
   */
  public boolean hasError() {
    return !hasValue();
  }

  /**
   * Getter.
   *
   * @return the {@code value}.
   */
  public Optional<V> value() {
    return Optional.ofNullable(value);
  }

  /**
   * Getter.
   *
   * @return the {@code errorDescriptor}.
   */
  public Optional<D> errorDescriptor() {
    return Optional.ofNullable(errorDescriptor);
  }

  /**
   * Convenience-method to register callbacks for success- and error-callback.
   *
   * <p>
   * If the {@link #value()} might be empty, the {@code onSuccess}-callback should tolerate
   * {@code null}-values.
   *
   * <p>
   * If the {@link #errorDescriptor()} might be empty, the {@code onError}-callback should tolerate
   * {@code null}-values.
   *
   * @param onSuccess success-callback.
   * @param onError error-callback.
   *
   * @return {@code this}, for chaining.
   */
  public Result<V, D> call(Consumer<V> onSuccess, Consumer<D> onError) {
    Objects.requireNonNull(onSuccess);
    Objects.requireNonNull(onError);
    if (hasValue()) {
      callOnSuccess(onSuccess);
    } else {
      callOnError(onError);
    }
    return this;
  }

  /**
   * Mapper-function to transform a {@code Result<V, D>} into a {@code Result<W, D>}.
   *
   * @param valueMapper the value-mapper.
   * @param <W> the {@link #value()}-type for the mapped {@link Result}.
   *
   * @return the mapped result
   */
  public <W> Result<W, D> mapValue(Function<V, W> valueMapper) {
    return map(valueMapper, Function.identity());
  }

  /**
   * Mapper-function to transform a {@code Result<V, D>} into a {@code Result<V, E>}.
   *
   * @param errorDescriptorMapper the errorDescriptor-mapper.
   * @param <E> the {@link #errorDescriptor()}-type for the mapped {@link Result}.
   *
   * @return the mapped result
   */
  public <E> Result<V, E> mapErrorDescriptor(Function<D, E> errorDescriptorMapper) {
    return map(Function.identity(), errorDescriptorMapper);
  }

  /**
   * Mapper-function to transform a {@code Result<V, D>} into a {@code Result<W, E>}.
   *
   * @param valueMapper the value-mapper.
   * @param errorDescriptionMapper the errorDescription-mapper.
   * @param <W> the {@link #value()}-type for the mapped {@link Result}.
   * @param <E> the {@link #errorDescriptor()}-type for the mapped {@link Result}.
   *
   * @return the mapped result
   */
  public <W, E> Result<W, E> map(Function<V, W> valueMapper,
      Function<D, E> errorDescriptionMapper) {
    Objects.requireNonNull(valueMapper);
    Objects.requireNonNull(errorDescriptionMapper);
    if (hasValue()) {
      return Result.ofValue(valueMapper.apply(value));
    } else {
      return Result.ofError(errorDescriptionMapper.apply(errorDescriptor));
    }
  }

  /**
   * Convenience-method to register callbacks for success-case.
   *
   * @param onSuccess success-callback.
   *
   * @return {@code this}, for chaining.
   */
  public Result<V, D> callOnSuccess(Consumer<V> onSuccess) {
    Objects.requireNonNull(onSuccess);
    if (hasValue()) {
      // should never throw
      onSuccess.accept(value().orElse(null));
    }
    return this;
  }

  /**
   * Convenience-method to register callbacks for error-case.
   *
   * <p>
   * If the {@link #errorDescriptor()} might be empty, the {@code onError}-callback should tolerate
   * {@code null}-values.
   *
   * @param onError error-callback.
   *
   * @return {@code this}, for chaining.
   */
  public Result<V, D> callOnError(Consumer<D> onError) {
    Objects.requireNonNull(onError);
    if (hasError()) {
      onError.accept(errorDescriptor().orElse(null));
    }
    return this;
  }
}
