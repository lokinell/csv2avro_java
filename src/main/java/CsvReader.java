import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.IOException;

public class CsvReader {
  private static final String CSV_DELIMITER = ",";

  private BufferedReader br;
  private String [] headers;
  private int fieldsCount;

  public CsvReader(InputStream inStream) throws IOException {
    br = new BufferedReader(new InputStreamReader(inStream));
    parseHeader();
  }

  public CsvReader(String csvFilePath) throws IOException {
    br = new BufferedReader(new FileReader(csvFilePath));
    parseHeader();
  }

  private void parseHeader() throws IOException {
    if (br != null) {
      String headersLine = br.readLine();
      if (headersLine != null) {
        headers = headersLine.split(CSV_DELIMITER);
        // split doesn't return empty strings
        // I assume that if there is an empty string in the header (like 'sth, , sth2') this is an error      
        // first calculate number of commas in the header
        int commasCount = headersLine.length() - headersLine.replace(CSV_DELIMITER, "").length();
        // there can't be less than commaCount - 1 commas in the header (because this is as split() works)
        // the only correct case is when number of header fields equals commasCount - 1 (each field is separated by comma)
        // if there is more tha commasCount - 1 commas in the header it means the csv file is corrupted
        // (I also don't allow adding auxilary comma after the last element of the header)
        if (commasCount >= headers.length) {
          throw new CorruptedCsvException("corrupted csv header - null header fields exist");
        } 
        
        // remove any spaces that can be before or after field name
        // so the processing later is easier
        for (int i=0; i<headers.length; ++i) {
          headers[i] = headers[i].trim();
        }
        fieldsCount = headers.length;
      } else {
        throw new CorruptedCsvException("csv file is empty");
      }
    }
  }

  public String [] getHeaderFields() {
    return headers;
  }

  public int getFieldsCount() {
    return fieldsCount;
  }

  public String readLine() throws IOException {
    return br.readLine();
  }

  public void close() throws IOException {
    if (br != null) {
      br.close();
    }
  }
}
