package de.turing85.pure.result;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Result tests")
class ResultTests {

  @Test
  @DisplayName("has correct value \uD83D\uDCB0")
  void hasCorrectValue() {
    // GIVEN
    String expectedValue = "expectedValue";

    // WHEN
    Result<String, Object> result = Result.ofValue(expectedValue);

    // THEN
    assertThat(result.hasValue()).isTrue();
    assertThat(result.hasError()).isFalse();
    assertThat(result.value().isPresent()).isTrue();
    assertThat(result.errorDescriptor().isEmpty()).isTrue();
    assertThat(result.value().get()).isEqualTo(expectedValue);
  }

  @Test
  @DisplayName("has correct errorDescriptor \uD83D\uDCA3")
  void hasCorrectErrorDescriptor() {
    // GIVEN
    Object expectedErrorDescriptor = "expectedErrorDescriptor";

    // WHEN
    Result<String, Object> result = Result.ofError(expectedErrorDescriptor);

    // THEN
    assertThat(result.hasValue()).isFalse();
    assertThat(result.hasError()).isTrue();
    assertThat(result.value().isEmpty()).isTrue();
    assertThat(result.errorDescriptor().isPresent()).isTrue();
    assertThat(result.errorDescriptor().get()).isEqualTo(expectedErrorDescriptor);
  }

  @Nested
  @DisplayName("Callable tests")
  class CallableTest {
    @SuppressWarnings("unchecked")
    private final Callable<Integer> callable = mock(Callable.class);

    @Test
    @DisplayName("no exception occurs \uD83C\uDF89")
    void noException() throws Exception {
      // GIVEN
      int expectedValue = 1;
      when(callable.call()).thenReturn(expectedValue);

      // WHEN
      var actual = Result.invoke(callable);

      // THEN
      assertThat(actual.errorDescriptor().isEmpty()).isTrue();
      assertThat(actual.value().isPresent()).isTrue();
      assertThat(actual.value().get()).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("exception occurs \uD83D\uDCA5")
    void exception() throws Exception {
      // GIVEN
      RuntimeException expectedErrorDescriptor = new RuntimeException();
      when(callable.call()).thenThrow(expectedErrorDescriptor);

      // WHEN
      var actual = Result.invoke(callable);

      // THEN
      assertThat(actual.value().isEmpty()).isTrue();
      assertThat(actual.errorDescriptor().isPresent()).isTrue();
      assertThat(actual.errorDescriptor().get()).isEqualTo(expectedErrorDescriptor);
    }
  }

  @Nested
  @DisplayName("Supplier tests")
  class SupplierTests {
    @SuppressWarnings("unchecked")
    private final Supplier<Boolean> supplier = mock(Supplier.class);
    private final Predicate<Boolean> errorCondition = Predicate.not(value -> value);
    private final IllegalStateException errorDescriptor = new IllegalStateException();
    private final Function<Boolean, IllegalStateException> toErrorMapper = value -> errorDescriptor;

    @Test
    @DisplayName("no error occurs \uD83C\uDF89")
    void noError() {
      // GIVEN
      boolean expectedErrorDescriptor = true;
      when(supplier.get()).thenReturn(expectedErrorDescriptor);

      // WHEN
      var actual = Result.invoke(supplier, errorCondition);

      // THEN
      assertThat(actual.errorDescriptor().isEmpty()).isTrue();
      assertThat(actual.value().isPresent()).isTrue();
      assertThat(actual.value().get()).isEqualTo(expectedErrorDescriptor);
    }

    @Test
    @DisplayName("error occurs \uD83D\uDCA5")
    void error() {
      // GIVEN
      when(supplier.get()).thenReturn(false);

      // WHEN
      var actual = Result.invoke(supplier, errorCondition, toErrorMapper);

      // THEN
      assertThat(actual.value().isEmpty()).isTrue();
      assertThat(actual.errorDescriptor().isPresent()).isTrue();
      assertThat(actual.errorDescriptor().get()).isEqualTo(errorDescriptor);
    }
  }

  @Nested
  @DisplayName("Function tests")
  class FunctionTest {
    private final Function<Boolean, String> function = Object::toString;
    private final Predicate<String> errorCondition = Predicate.not("true"::equals);
    private final String expectedErrorDescriptor = "Ouch!";
    private final Function<String, String> toErrorMapper = value -> expectedErrorDescriptor;

    @Test
    @DisplayName("no error occurs \uD83C\uDF89")
    void noError() {
      // GIVEN
      String expectedValue = "true";
      Supplier<Boolean> parameterProvider = () -> true;

      // WHEN
      var result =
          Result.invoke(function, parameterProvider, errorCondition, toErrorMapper);

      // THEN
      assertThat(result.value().isPresent()).isTrue();
      assertThat(result.errorDescriptor().isEmpty()).isTrue();
      assertThat(result.value().get()).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("error occurs \uD83D\uDCA5")
    void error() {
      // GIVEN
      String expectedValue = "false";
      Supplier<Boolean> parameterProvider = () -> false;

      // WHEN
      var result =
          Result.invoke(function, parameterProvider, errorCondition);

      // THEN
      assertThat(result.value().isEmpty()).isTrue();
      assertThat(result.errorDescriptor().isPresent()).isTrue();
      assertThat(result.errorDescriptor().get()).isEqualTo(expectedValue);
    }
  }

  @Nested
  @DisplayName("Throwing function tests")
  class ThrowingFunctionTests {
    @Test
    @DisplayName("no error occurs \uD83C\uDF89")
    void noError() {
      // GIVEN
      String expectedValue = "true";
      Supplier<Boolean> parameterProvider = () -> true;
      ThrowingFunction<Boolean, String> function = Object::toString;

      // WHEN
      var result = Result.invoke(function, parameterProvider);

      // THEN
      assertThat(result.value().isPresent()).isTrue();
      assertThat(result.errorDescriptor().isEmpty()).isTrue();
      assertThat(result.value().get()).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("error occurs \uD83D\uDCA5")
    void error() {
      // GIVEN
      Exception expectedErrorDescriptor = new Exception();
      Supplier<Boolean> parameterProvider = () -> false;
      ThrowingFunction<Boolean, String> function = ignored -> {
        throw expectedErrorDescriptor;
      };

      // WHEN
      var result =
          Result.invoke(function, parameterProvider);

      // THEN
      assertThat(result.value().isEmpty()).isTrue();
      assertThat(result.errorDescriptor().isPresent()).isTrue();
      assertThat(result.errorDescriptor().get()).isEqualTo(expectedErrorDescriptor);
    }
  }

  @Nested
  @DisplayName("Call tests")
  class CallTests {
    @Test
    @DisplayName("call on value result \uD83D\uDCB0 \u2B95 \uD83D\uDCE3")
    void callOnValue() {
      // GIVEN
      @SuppressWarnings("unchecked")
      Consumer<String> onSuccess = mock(Consumer.class);
      @SuppressWarnings("unchecked")
      Consumer<Object> onError = mock(Consumer.class);
      String expectedValue = "expectedValue";
      Result<String, Object> result = Result.ofValue(expectedValue);

      // WHEN
      result.call(onSuccess, onError);

      // THEN
      verify(onSuccess).accept(expectedValue);
      verifyNoInteractions(onError);
    }

    @Test
    @DisplayName("call success on value result \uD83D\uDCB0 \u2B95  \uD83D\uDCE3 \u2B95 \uD83D\uDCB0")
    void callSuccessOnValue() {
      // GIVEN
      @SuppressWarnings("unchecked")
      Consumer<String> onSuccess = mock(Consumer.class);
      String expectedValue = "expectedValue";
      Result<String, Object> result = Result.ofValue(expectedValue);

      // WHEN
      result.callOnSuccess(onSuccess);

      // THEN
      verify(onSuccess).accept(expectedValue);
    }

    @Test
    @DisplayName("call error on value result \uD83D\uDCB0 \u2B95 \uD83D\uDCE3 \u2B95 \uD83D\uDCA3")
    void callErrorOnValue() {
      // GIVEN
      @SuppressWarnings("unchecked")
      Consumer<Object> onError = mock(Consumer.class);
      Result<String, Object> result = Result.ofValue("expectedValue");

      // WHEN
      result.callOnError(onError);

      // THEN
      verifyNoInteractions(onError);
    }

    @Test
    @DisplayName("call on error result \uD83D\uDCA3 \u2B95 \uD83D\uDCE3")
    void callOnError() {
      // GIVEN
      @SuppressWarnings("unchecked")
      Consumer<String> onSuccess = mock(Consumer.class);
      @SuppressWarnings("unchecked")
      Consumer<Object> onError = mock(Consumer.class);
      Object expectedErrorDescriptor = new Object();
      Result<String, Object> result = Result.ofError(expectedErrorDescriptor);

      // WHEN
      result.call(onSuccess, onError);

      // THEN
      verifyNoInteractions(onSuccess);
      verify(onError).accept(expectedErrorDescriptor);
    }

    @Test
    @DisplayName("call success on error result \uD83D\uDCA3 \u2B95 \uD83D\uDCE3 \u2B95 \uD83D\uDCB0")
    void callSuccessOnError() {
      // GIVEN
      @SuppressWarnings("unchecked")
      Consumer<String> onSuccess = mock(Consumer.class);
      Result<String, Object> result = Result.ofError(new Object());

      // WHEN
      result.callOnSuccess(onSuccess);

      // THEN
      verifyNoInteractions(onSuccess);
    }

    @Test
    @DisplayName("call onError on error result \uD83D\uDCA3 \u2B95 \uD83D\uDCE3 \u2B95 \uD83D\uDCA3")
    void callErrorOnError() {
      // GIVEN
      @SuppressWarnings("unchecked")
      Consumer<Object> onError = mock(Consumer.class);
      Object expectedErrorDescriptor = new Object();
      Result<String, Object> result = Result.ofError(expectedErrorDescriptor);

      // WHEN
      result.callOnError(onError);

      // THEN
      verify(onError).accept(expectedErrorDescriptor);
    }
  }

  @Nested
  @DisplayName("Map tests")
  class MapTests {
    @Test
    @DisplayName("map on value result️ \uD83D\uDCB0 \u2B95 \uD83D\uDDFA")
    void mapOnValue() {
      // GIVEN
      Function<String, String> onSuccess = String::toUpperCase;
      @SuppressWarnings("unchecked")
      Function<Object, Object> onError = mock(Function.class);
      String value = "value";
      Result<String, Object> result = Result.ofValue(value);

      // WHEN
      var mappedResult = result.map(onSuccess, onError);

      // THEN
      assertThat(mappedResult.value().isPresent()).isTrue();
      assertThat(mappedResult.errorDescriptor().isEmpty()).isTrue();
      assertThat(mappedResult.value().get()).isEqualTo(value.toUpperCase());

      verifyNoInteractions(onError);
    }

    @Test
    @DisplayName("map on error result \uD83D\uDCA3 \u2B95 \uD83D\uDDFA️")
    void mapOnError() {
      // GIVEN
      @SuppressWarnings("unchecked")
      Function<String, String> onSuccess = mock(Function.class);
      Object expectedErrorDescriptor = new Object();
      Function<Object, Object> onError = object -> expectedErrorDescriptor;
      Result<String, Object> result = Result.ofError(new Object());

      // WHEN
      var mappedResult = result.map(onSuccess, onError);

      // THEN
      assertThat(mappedResult.value().isEmpty()).isTrue();
      assertThat(mappedResult.errorDescriptor().isPresent()).isTrue();
      assertThat(mappedResult.errorDescriptor().get()).isEqualTo(expectedErrorDescriptor);

      verifyNoInteractions(onSuccess);
    }

    @Test
    @DisplayName("map value on value result \uD83D\uDCB0 \u2B95 \uD83D\uDDFA️ \u2B95 \uD83D\uDCB0")
    void mapValueOnValue() {
      // GIVEN
      Function<String, String> onSuccess = String::toUpperCase;
      String value = "value";
      Result<String, Object> result = Result.ofValue(value);

      // WHEN
      var mappedResult = result.mapValue(onSuccess);

      // THEN
      assertThat(mappedResult.value().isPresent()).isTrue();
      assertThat(mappedResult.errorDescriptor().isEmpty()).isTrue();
      assertThat(mappedResult.value().get()).isEqualTo(value.toUpperCase());
    }

    @Test
    @DisplayName("map value on error result \uD83D\uDCA3 \u2B95  \uD83D\uDDFA️ \u2B95 \uD83D\uDCB0")
    void mapValueOnError() {
      // GIVEN
      @SuppressWarnings("unchecked")
      Function<String, String> onSuccess = mock(Function.class);
      Object errorDescriptor = new Object();
      Result<String, Object> result = Result.ofError(errorDescriptor);

      // WHEN
      var mappedResult = result.mapValue(onSuccess);

      // THEN
      assertThat(mappedResult.value().isEmpty()).isTrue();
      assertThat(mappedResult.errorDescriptor().isPresent()).isTrue();
      assertThat(mappedResult.errorDescriptor().get()).isEqualTo(errorDescriptor);

      verifyNoInteractions(onSuccess);
    }

    @Test
    @DisplayName("map error on value result \uD83D\uDCA3 \u2B95  \uD83D\uDDFA️ \u2B95  \uD83D\uDCB0")
    void mapErrorOnValue() {
      // GIVEN
      @SuppressWarnings("unchecked")
      Function<Object, Object> onError = mock(Function.class);
      String value = "value";
      Result<String, Object> result = Result.ofValue(value);

      // WHEN
      var mappedResult = result.mapErrorDescriptor(onError);

      // THEN
      assertThat(mappedResult.value().isPresent()).isTrue();
      assertThat(mappedResult.errorDescriptor().isEmpty()).isTrue();
      assertThat(mappedResult.value().get()).isEqualTo(value);

      verifyNoInteractions(onError);
    }

    @Test
    @DisplayName("map error on error result \uD83D\uDCA3 \u2B95 \uD83D\uDDFA️ \u2B95 \uD83D\uDCA3")
    void mapErrorOnError() {
      // GIVEN
      Object expectedErrorDescriptor = new Object();
      Function<Object, Object> onError = object -> expectedErrorDescriptor;
      Result<String, Object> result = Result.ofError(new Object());

      // WHEN
      var mappedResult = result.mapErrorDescriptor(onError);

      // THEN
      assertThat(mappedResult.value().isEmpty()).isTrue();
      assertThat(mappedResult.errorDescriptor().isPresent()).isTrue();
      assertThat(mappedResult.errorDescriptor().get()).isEqualTo(expectedErrorDescriptor);
    }
  }
}