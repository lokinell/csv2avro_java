import java.util.Set;
import java.util.HashSet;


public class CsvToAvro {
  private final static String PROGRAM_NAME = "CsvToAvro";
  private final static String SCHEMA_ARG = "--schema";
  private final static String INPUT_ARG = "--input";
  private final static String OUTPUT_ARG = "--output";
  private final static String STREAM_ARG = "-c";

  private static boolean streamInputOutput = false;
  private static boolean fileInput = false;
  private static boolean fileOutput = false;

  private static String inputPath;
  private static String outputPath;
  private static String schemaPath;  


  public static void main(String [] args) {
    if (processArgs(args) == false) {
      printUsage();
      return; 
    }
    try {
      CsvToAvroConverter converter;

      if (fileInput && fileOutput) {
        converter = new CsvToAvroConverter(inputPath, schemaPath, outputPath);
        converter.convert();
      } else if (streamInputOutput) {
        converter = new CsvToAvroConverter(System.in, schemaPath, System.out);
        converter.convert();
      }

    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  private static void printUsage() {
    System.out.println("usage: CsvToAvro --input <csv_file> --schema <avro_schema> --output <avro_file>");
    System.out.println("       CsvToAvro --schema <avro_schema> -c < <intput_csv> > <avro_file>");
  }

  private static boolean processArgs(String [] args) {

    boolean schemaInput = false;

    Set<String> allowedArgs = new HashSet<String>();
    allowedArgs.add(SCHEMA_ARG);
    allowedArgs.add(INPUT_ARG);
    allowedArgs.add(OUTPUT_ARG);
    allowedArgs.add(STREAM_ARG);

    for (int i=0; i<args.length; ++i) {
      if (args[i].trim().equals(SCHEMA_ARG)) {
        ++i;
        if ((args[i] != null) && !allowedArgs.contains(args[i].trim())) {
          schemaPath = args[i];
          schemaInput = true;
        } 
      } else if (args[i].trim().equals(INPUT_ARG)) {
        ++i;
        if ((args[i] != null) && !allowedArgs.contains(args[i].trim())) {
          inputPath = args[i];
          fileInput = true;
        } 
      } else if (args[i].trim().equals(OUTPUT_ARG)) {
        ++i;
        if ((args[i] != null) && !allowedArgs.contains(args[i].trim())) {
          outputPath = args[i];
          fileOutput = true;
        }
      } else if (args[i].trim().equals(STREAM_ARG)) {
        streamInputOutput = true;
      } else if (args[i].trim().equals(PROGRAM_NAME)) {
        continue;
      } else {
        System.out.println("4 >>>" + args[i] + "<<<");
        return false;
      }
    }
    // schema is an obligatory parameter
    if (!schemaInput) {
      return false;
    }
    // if any of fileInput/fileOutput is set, the other also has to be set
    if ((fileInput || fileOutput) && !(fileInput && fileOutput)) {
      return false;
    }
    // it should be one of in/out from file or stream
    if ((fileInput || fileOutput) && streamInputOutput) {
      return false;
    }
    // it should be at least on of in/out from file or stream
    if (!(fileInput || fileOutput) && !streamInputOutput) {
      return false;
    }
    return true;
  }
}
