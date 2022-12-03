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

  @Nested
  @DisplayName("Field tests")
  class FieldTests {
    @Test
    @DisplayName("has correct value \uD83D\uDCB0")
    void hasCorrectValue() {
      // GIVEN
      String expectedValue = "expectedValue";

      // WHEN
      Result<String, Object> result = Result.ofValue(expectedValue);

      // THEN
      assertThat(result.hasValue()).isTrue();
      assertThat(result.value().isPresent()).isTrue();
      assertThat(result.value().get()).isEqualTo(expectedValue);
      assertThat(result.hasError()).isFalse();
      assertThat(result.errorDescriptor().isEmpty()).isTrue();
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
      assertThat(result.value().isEmpty()).isTrue();
      assertThat(result.hasError()).isTrue();
      assertThat(result.errorDescriptor().isPresent()).isTrue();
      assertThat(result.errorDescriptor().get()).isEqualTo(expectedErrorDescriptor);
    }
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
      assertThat(actual.value().isPresent()).isTrue();
      assertThat(actual.value().get()).isEqualTo(expectedValue);
      assertThat(actual.errorDescriptor().isEmpty()).isTrue();
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
      boolean expectedValue = true;
      when(supplier.get()).thenReturn(expectedValue);

      // WHEN
      var actual = Result.invoke(supplier, errorCondition);

      // THEN
      assertThat(actual.value().isPresent()).isTrue();
      assertThat(actual.value().get()).isEqualTo(expectedValue);
      assertThat(actual.errorDescriptor().isEmpty()).isTrue();
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
      var result = Result.invoke(function, parameterProvider, errorCondition, toErrorMapper);

      // THEN
      assertThat(result.value().isPresent()).isTrue();
      assertThat(result.value().get()).isEqualTo(expectedValue);
      assertThat(result.errorDescriptor().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("error occurs \uD83D\uDCA5")
    void error() {
      // GIVEN
      String expectedValue = "false";
      Supplier<Boolean> parameterProvider = () -> false;

      // WHEN
      var result = Result.invoke(function, parameterProvider, errorCondition);

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
      ThrowingFunction<Boolean, String> function = Object::toString;
      Supplier<Boolean> parameterProvider = () -> true;

      // WHEN
      var result = Result.invoke(function, parameterProvider);

      // THEN
      assertThat(result.value().isPresent()).isTrue();
      assertThat(result.value().get()).isEqualTo(expectedValue);
      assertThat(result.errorDescriptor().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("error occurs \uD83D\uDCA5")
    void error() {
      // GIVEN
      Exception expectedErrorDescriptor = new Exception();
      ThrowingFunction<Boolean, String> function = ignored -> {
        throw expectedErrorDescriptor;
      };
      Supplier<Boolean> parameterProvider = () -> false;

      // WHEN
      var result = Result.invoke(function, parameterProvider);

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
    @DisplayName("call on value result \uD83D\uDCB0 ⮕ \uD83D\uDCE3")
    void callOnValue() {
      // GIVEN
      String expectedValue = "expectedValue";
      Result<String, Object> result = Result.ofValue(expectedValue);
      @SuppressWarnings("unchecked")
      Consumer<String> onSuccess = mock(Consumer.class);
      @SuppressWarnings("unchecked")
      Consumer<Object> onError = mock(Consumer.class);

      // WHEN
      result.call(onSuccess, onError);

      // THEN
      verify(onSuccess).accept(expectedValue);
      verifyNoInteractions(onError);
    }

    @Test
    @DisplayName("call success on value result \uD83D\uDCB0 ⮕ \uD83D\uDCE3 ⮕ \uD83D\uDCB0")
    void callSuccessOnValue() {
      // GIVEN
      String expectedValue = "expectedValue";
      Result<String, Object> result = Result.ofValue(expectedValue);
      @SuppressWarnings("unchecked")
      Consumer<String> onSuccess = mock(Consumer.class);

      // WHEN
      result.callOnSuccess(onSuccess);

      // THEN
      verify(onSuccess).accept(expectedValue);
    }

    @Test
    @DisplayName("call error on value result \uD83D\uDCB0 ⮕ \uD83D\uDCE3 ⮕ \uD83D\uDCA3")
    void callErrorOnValue() {
      // GIVEN
      Result<String, Object> result = Result.ofValue("expectedValue");
      @SuppressWarnings("unchecked")
      Consumer<Object> onError = mock(Consumer.class);

      // WHEN
      result.callOnError(onError);

      // THEN
      verifyNoInteractions(onError);
    }

    @Test
    @DisplayName("call on error result \uD83D\uDCA3 ⮕ \uD83D\uDCE3")
    void callOnError() {
      // GIVEN
      Object expectedErrorDescriptor = new Object();
      Result<String, Object> result = Result.ofError(expectedErrorDescriptor);
      @SuppressWarnings("unchecked")
      Consumer<String> onSuccess = mock(Consumer.class);
      @SuppressWarnings("unchecked")
      Consumer<Object> onError = mock(Consumer.class);

      // WHEN
      result.call(onSuccess, onError);

      // THEN
      verifyNoInteractions(onSuccess);
      verify(onError).accept(expectedErrorDescriptor);
    }

    @Test
    @DisplayName("call success on error result \uD83D\uDCA3 ⮕ \uD83D\uDCE3 ⮕ \uD83D\uDCB0")
    void callSuccessOnError() {
      // GIVEN
      Result<String, Object> result = Result.ofError(new Object());
      @SuppressWarnings("unchecked")
      Consumer<String> onSuccess = mock(Consumer.class);

      // WHEN
      result.callOnSuccess(onSuccess);

      // THEN
      verifyNoInteractions(onSuccess);
    }

    @Test
    @DisplayName("call onError on error result \uD83D\uDCA3 ⮕ \uD83D\uDCE3 ⮕ \uD83D\uDCA3")
    void callErrorOnError() {
      // GIVEN
      Object expectedErrorDescriptor = new Object();
      Result<String, Object> result = Result.ofError(expectedErrorDescriptor);
      @SuppressWarnings("unchecked")
      Consumer<Object> onError = mock(Consumer.class);

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
    @DisplayName("map on value result \uD83D\uDCB0 ⮕ \uD83D\uDDFA️")
    void mapOnValue() {
      // GIVEN
      String value = "value";
      Result<String, Object> result = Result.ofValue(value);
      Function<String, String> onSuccess = String::toUpperCase;
      @SuppressWarnings("unchecked")
      Function<Object, Object> onError = mock(Function.class);

      // WHEN
      var mappedResult = result.map(onSuccess, onError);

      // THEN
      assertThat(mappedResult.value().isPresent()).isTrue();
      assertThat(mappedResult.value().get()).isEqualTo(value.toUpperCase());
      assertThat(mappedResult.errorDescriptor().isEmpty()).isTrue();

      verifyNoInteractions(onError);
    }

    @Test
    @DisplayName("map on error result \uD83D\uDCA3 ⮕ \uD83D\uDDFA️️")
    void mapOnError() {
      // GIVEN
      Object expectedErrorDescriptor = new Object();
      Result<String, Object> result = Result.ofError(new Object());
      @SuppressWarnings("unchecked")
      Function<String, String> onSuccess = mock(Function.class);
      Function<Object, Object> onError = object -> expectedErrorDescriptor;

      // WHEN
      var mappedResult = result.map(onSuccess, onError);

      // THEN
      assertThat(mappedResult.value().isEmpty()).isTrue();
      assertThat(mappedResult.errorDescriptor().isPresent()).isTrue();
      assertThat(mappedResult.errorDescriptor().get()).isEqualTo(expectedErrorDescriptor);

      verifyNoInteractions(onSuccess);
    }

    @Test
    @DisplayName("map value on value result \uD83D\uDCB0 ⮕ \uD83D\uDDFA️️ ⮕ \uD83D\uDCB0")
    void mapValueOnValue() {
      // GIVEN
      String value = "value";
      Result<String, Object> result = Result.ofValue(value);
      Function<String, String> onSuccess = String::toUpperCase;

      // WHEN
      var mappedResult = result.mapValue(onSuccess);

      // THEN
      assertThat(mappedResult.value().isPresent()).isTrue();
      assertThat(mappedResult.value().get()).isEqualTo(value.toUpperCase());
      assertThat(mappedResult.errorDescriptor().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("map value on error result \uD83D\uDCA3 ⮕  \uD83D\uDDFA️️ ⮕ \uD83D\uDCB0")
    void mapValueOnError() {
      // GIVEN
      Object errorDescriptor = new Object();
      Result<String, Object> result = Result.ofError(errorDescriptor);
      @SuppressWarnings("unchecked")
      Function<String, String> onSuccess = mock(Function.class);

      // WHEN
      var mappedResult = result.mapValue(onSuccess);

      // THEN
      assertThat(mappedResult.value().isEmpty()).isTrue();
      assertThat(mappedResult.errorDescriptor().isPresent()).isTrue();
      assertThat(mappedResult.errorDescriptor().get()).isEqualTo(errorDescriptor);

      verifyNoInteractions(onSuccess);
    }

    @Test
    @DisplayName("map error on value result \uD83D\uDCA3 ⮕  \uD83D\uDDFA️️ ⮕  \uD83D\uDCB0")
    void mapErrorOnValue() {
      // GIVEN
      String value = "value";
      Result<String, Object> result = Result.ofValue(value);
      @SuppressWarnings("unchecked")
      Function<Object, Object> onError = mock(Function.class);

      // WHEN
      var mappedResult = result.mapErrorDescriptor(onError);

      // THEN
      assertThat(mappedResult.value().isPresent()).isTrue();
      assertThat(mappedResult.value().get()).isEqualTo(value);
      assertThat(mappedResult.errorDescriptor().isEmpty()).isTrue();

      verifyNoInteractions(onError);
    }

    @Test
    @DisplayName("map error on error result \uD83D\uDCA3 ⮕ \uD83D\uDDFA️️ ⮕ \uD83D\uDCA3")
    void mapErrorOnError() {
      // GIVEN
      Object expectedErrorDescriptor = new Object();
      Result<String, Object> result = Result.ofError(new Object());
      Function<Object, Object> onError = object -> expectedErrorDescriptor;

      // WHEN
      var mappedResult = result.mapErrorDescriptor(onError);

      // THEN
      assertThat(mappedResult.value().isEmpty()).isTrue();
      assertThat(mappedResult.errorDescriptor().isPresent()).isTrue();
      assertThat(mappedResult.errorDescriptor().get()).isEqualTo(expectedErrorDescriptor);
    }
  }
}
