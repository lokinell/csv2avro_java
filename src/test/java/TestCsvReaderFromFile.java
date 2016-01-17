import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
 	
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

public class TestCsvReaderFromFile {
  private static CsvReader csvReader;
  private final static String CSV_FILE_PATH = "src/test/resources/small.csv";

  @BeforeClass
  public static void initialize() {
    try {
      csvReader = new CsvReader(CSV_FILE_PATH);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Test
  public void checkHeaderFieldsCount() {
    assertEquals(csvReader.getFieldsCount(), 9);
  }

  @Test
  public void checkHeaderFields() {
    String [] fields = {"gender", "brand", "size", "product_type", "id", "look_id", "link", "age_group", "product_id"};
    String [] csvFields = csvReader.getHeaderFields();
    assertTrue(fields.length == csvFields.length);
    for (int i=0; i<fields.length; ++i) {
      assertEquals(fields[i], csvFields[i]);
    }
  }

  @Test
  public void testLinesWithDataCount() {
    int linesCount = 0;
    try {
      while(csvReader.readLine() != null) {
        ++linesCount;
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    assertEquals(linesCount, 8);
  }
  
  @AfterClass 
  public static void close() {
    try {
      csvReader.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
