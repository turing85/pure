package de.turing85.pure;

import de.turing85.pure.result.Result;

public class Sample {
  public static void main(String... args) {
    for (int i = 0; i < 10; ++i) {
      int finalI = i;
      var result = Result.<Integer, Boolean>invoke(Sample::mayThrow, () -> finalI);
      System.out.println(result);
      result
          .call(
              value -> System.out.printf(
                  "Value(Type = [%s]) = [%b]%n",
                  value.getClass().getSimpleName(),
                  value),
              exception -> System.out.printf(
                  "errorDescriptor(Type = [%s]) = [%s]%n",
                  exception.getClass().getSimpleName(),
                  exception))
          .callOnSuccess(
              value -> System.out.printf(
                  "Value(Type = [%s]) = [%b]%n",
                  value.getClass().getSimpleName(),
                  value))
          .callOnError(
              exception -> System.out.printf(
                  "errorDescriptor(Type = [%s]) = [%s]%n",
                  exception.getClass().getSimpleName(),
                  exception))
          .mapValue(Object::toString)
          .callOnSuccess(
              valueAsString -> System.out.printf(
                  "Value as %s = [%s], length = [%d]%n",
                  valueAsString.getClass().getSimpleName(),
                  valueAsString,
                  valueAsString.length()))
          .mapErrorDescriptor(Object::toString)
          .callOnError(
              exceptionAsString -> System.out.printf(
                  "Exception as %s = [%s], length = [%d]%n",
                  exceptionAsString.getClass().getSimpleName(),
                  exceptionAsString,
                  exceptionAsString.length()));
    }
  }

  public static boolean mayThrow(int i) throws RuntimeException {
    if (i % 2 == 0) {
      return true;
    }
    throw new RuntimeException();
  }
}
