import java.io.IOException;

public class CorruptedCsvException extends IOException {
  public CorruptedCsvException(String message) {
    super(message);
  }

  public CorruptedCsvException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
