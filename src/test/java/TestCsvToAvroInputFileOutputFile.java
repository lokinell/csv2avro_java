import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
 	
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.file.DataFileReader;

public class TestCsvToAvroInputFileOutputFile {
  private final static String CSV_FILE_PATH = "src/test/resources/small.csv";
  private final static String AVRO_SCHEMA_PATH = "src/test/resources/test_schema.avsc";
  private final static String AVRO_OUTPUT_PATH = "src/test/resources/products.avro";

  private static CsvToAvroConverter ac;
  private static Schema schema;

  @BeforeClass
  public static void initialize() {

    try {
      ac = new CsvToAvroConverter(CSV_FILE_PATH, AVRO_SCHEMA_PATH, AVRO_OUTPUT_PATH);  
      ac.convert();
      schema = new Schema.Parser().parse(new File(AVRO_SCHEMA_PATH));
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Test
  public void testConversion() {
    String [] avroFields = {"id", "look_id", "product_id", "name", "description", "gender"};
    String [][] expectedOutput = {{"1", "null", "123", "null", "null", "male", "newborn"},
          {"2", "12", "23", "null", "null", "female", "infant"},
          {"3", "14", "123", "null", "null", "unisex", "infant"},
          {"5", "12", "123", "null", "null", "female", "toddler"},
          {"13", "12", "333", "null", "null", "unisex", "toddler"},
          {"15", "3", "444", "null", "null", "male", "kids"},
          {"9", "15", "123", "null", "null", "female", "kids"}};

    try {
      DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
      DataFileReader<GenericRecord> dataFileReader = new DataFileReader<GenericRecord>(new File(AVRO_OUTPUT_PATH), datumReader);

      GenericRecord product = null;
      int i = 0;
      while (dataFileReader.hasNext()) {
        product = dataFileReader.next(product);
        if (product != null) {
          for (int j=0; j<avroFields.length; ++j) {
            if (product.get(avroFields[j]) != null) {
              assertEquals(product.get(avroFields[j]).toString(), expectedOutput[i][j]);
            } else {
              assertEquals(expectedOutput[i][j], "null");
            }
          }
          ++i;
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Test
  public void testConvertedLinesCount() {
    int linesCount = 0;
    try {
      DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
      DataFileReader<GenericRecord> dataFileReader = new DataFileReader<GenericRecord>(new File(AVRO_OUTPUT_PATH), datumReader);

      GenericRecord product = null;
      while (dataFileReader.hasNext()) {
        product = dataFileReader.next(product);
        ++linesCount;
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    assertEquals(linesCount, 7);
  }



}
