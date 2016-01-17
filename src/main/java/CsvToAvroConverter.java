import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.util.Map;
import java.io.BufferedOutputStream;
import java.util.LinkedList;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.file.DataFileWriter.AppendWriteException;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class CsvToAvroConverter {
  private final static String CSV_DELIMITER = ",";

  private CsvReader csvReader;
  private AvroSchema as;
  private Schema schema;
  private int errorCount = 0;
  private BufferedOutputStream out;
  private DatumWriter<GenericRecord> datumWriter;
  private DataFileWriter<GenericRecord> dataFileWriter;
  private Logger logger = Logger.getLogger(CsvToAvroConverter.class.getName());  
  private FileHandler fh; 
     

  public CsvToAvroConverter(String inputPath, String schemaPath, String outputPath) throws IOException {
    csvReader = new CsvReader(inputPath);
    as = new AvroSchema(schemaPath);
    schema = new Schema.Parser().parse(new File(schemaPath));
    datumWriter = new GenericDatumWriter<GenericRecord>(schema);
    dataFileWriter = new DataFileWriter<GenericRecord>(datumWriter);
    dataFileWriter.create(schema, new File(outputPath)); 
  }

  public CsvToAvroConverter(InputStream inStream, String schemaPath, OutputStream outputStream) throws IOException {
    csvReader = new CsvReader(inStream);
    as = new AvroSchema(schemaPath);
    schema = new Schema.Parser().parse(new File(schemaPath));
    datumWriter = new GenericDatumWriter<GenericRecord>(schema);
    dataFileWriter = new DataFileWriter<GenericRecord>(datumWriter);
    dataFileWriter.create(schema, new BufferedOutputStream(outputStream)); 
  }

  public void convert() throws IOException {

    fh = new FileHandler("csvToAvro.log");   
    logger.setUseParentHandlers(false);
    logger.addHandler(fh); 

    String [] csvHeaders = csvReader.getHeaderFields();
    int errorCount = 0;  

    Map<String, String> schemaFields = as.getFields();
    Map<String, String> schemaTypes = as.getTypes();

    String line;
    while ((line = csvReader.readLine()) != null) {
      String [] data = line.split(CSV_DELIMITER);
      // if given line in csv file has different number of fields than
      // the number of fields in headers it mean the line is corrupted
      // - skip it then
      if (data.length != csvHeaders.length) {
        ++errorCount;
        continue;
      }

      GenericRecord product = new GenericData.Record(schema);
      for (int i=0; i<data.length; ++i) {
        // store csv entry in avro file if given csv field exists in avro schema
        // schema it as base name (it case alias is used in csv file)
       
        if (schemaFields.containsKey(csvHeaders[i])) {
          if (data[i].trim().equals("")) {
            product.put(schemaFields.get(csvHeaders[i]), null);
          } else {
            product.put(schemaFields.get(csvHeaders[i]), getAvroValue(data[i].trim(), schemaTypes.get(csvHeaders[i])));
          }
        }
      }

      try {
        dataFileWriter.append(product);
      } catch (AppendWriteException ex) {
        // this exception is thrown when given csv file line has schema different than the 
        // one expected by avro schema (e.g. enum value isn;t in the set of possible values
        // or non-null value has value of null)
        // in that case whole serialization shouldn;t be interrupted
        // but it should continue and appropriate log entry should be added
        logger.warning("Problem serializing object: " + ex.getMessage());
      }
    }

    if (errorCount != 0) {
      logger.info("There was " + errorCount + " errors during serialization");
    }
  }

  public void close() throws IOException {
    if (csvReader != null) {
      csvReader.close();
    }
    if (dataFileWriter != null) {
      dataFileWriter.close();
    }
  }

  // having string representation of avro type
  // return Java type that can be store in avro file
  private Object getAvroValue(String value, String type) {
    if (value.trim().equals("")) {
      return null;
    }
    switch(type) {
      case "array":
        return new LinkedList<String>().add(value);
      case "boolean":
        return Boolean.getBoolean(value);
      case "int":
        return Integer.parseInt(value);
      case "long":
        return Long.parseLong(value);
      case "float":
        return Float.parseFloat(value);
      case "double":
        return Double.parseDouble(value);
      default:
        return value;
    }
  }

}


