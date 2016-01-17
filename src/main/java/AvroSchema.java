import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import org.apache.avro.AvroRuntimeException;

public class AvroSchema {

  private final static String NAME_PATH = "name";
  private final static String FIELDS_PATH = "fields";
  private final static String ALIASES_PATH = "aliases";
  private final static String TYPE_PATH = "type";

  private String className;
  // keep fields in hash collection for performance reason
  // so looking for fields (while checking if they exist in csv file)
  // is in constant time, not in e.g. linear time
  // the key is name of field, the value is the field the key is alias for
  // if given key has no aliases key and value are the same
  private Map<String,String> fields = new HashMap<String,String>();
  private Map<String,String> types = new HashMap<String,String>();

  public AvroSchema(String schemaFile) throws IOException {
    // read JSON file
    // is assume schema file has reasonable size and can be safely kept in memory
    byte[] jsonData = Files.readAllBytes(Paths.get(schemaFile));

    ObjectMapper objectMapper = new ObjectMapper();

    // read JSON like DOM parser
    JsonNode rootNode = objectMapper.readTree(jsonData);

    if (rootNode == null) {
      throw new AvroRuntimeException("Corrupted avro schema file - file is empty");
    }

    JsonNode nameNode = rootNode.path(NAME_PATH);
    if (nameNode == null) {
      throw new AvroRuntimeException("Corrupted avro schema file - no 'name' field");
    }
      
    className = nameNode.asText();

    JsonNode fieldsNode = rootNode.path(FIELDS_PATH);
    if (fieldsNode == null) {
      throw new AvroRuntimeException("Corrupted avro schema file - no 'fields' field");
    }

    if (fieldsNode.isArray()) {
      for (JsonNode field: fieldsNode) {
        String fieldName = field.path(NAME_PATH).asText();
        fields.put(fieldName, fieldName);

        // parse types of fields
        String type;
        if (field.path(TYPE_PATH) == null) {
           throw new AvroRuntimeException("Corrupted avro schema file - no 'type' field");
        } 
        else {
          JsonNode typeNode = field.path(TYPE_PATH);
          // if this is primitive type its string representation starts with "
          if (typeNode.toString().startsWith("\"")) {
            type = typeNode.asText();
            types.put(fieldName, type);
          } 
          // if it starts with [ it's an union - I have no option to discover in csv file
          // union consisting of different types so I assume that in out case union means either given type or null
          else if (typeNode.toString().startsWith("[")) {
            type = parseUnion(typeNode.toString().substring(1, typeNode.toString().length() - 1));
            types.put(fieldName, type);
          }
          // other types in our case are enums so 
          else {
            type = getType(typeNode.toString());
            types.put(fieldName, type);
          }
        }

        // add to the map also the aliases with information what given field is alias for
        JsonNode aliasesNode = field.path(ALIASES_PATH);
        if (aliasesNode.isArray()) {
           for (JsonNode alias: aliasesNode) {
             fields.put(alias.asText(), fieldName);
             types.put(alias.asText(), type);
           }
        }
      }
    } else {
      throw new AvroRuntimeException("Corrupted avro schema file - 'fields' field not an array");
    }
  }

  public String getClassName() {
    return className;
  }

  public Set<String> getFieldsSet() {
    return fields.keySet();
  }

  public Map<String, String> getFields() {
    return fields;
  }

  public Map<String, String> getTypes() {
    return types;
  }

  // I only support subset of avro types
  // (assuming that using csv file as input limits 
  // avro types we can use)
  // the 'unsupported' avro types are threated as string
  private String getType(String type) { 
    switch (type.trim().toLowerCase()) {
        case "array":
            return "array";
        case "boolean":
            return "boolean";
        case "int":
          return "int";
        case "long":
            return "long";
        case "float":
            return "float";
        case "double":
            return "double";
        default:
            return "string";
    }
  }

  // having an union get its time
  // (asumming in our case union means eithter some type
  // or null)
  private String parseUnion(String union) {
    for (String s: union.replace("\"", "").split(",")) {
      if (s != "null") {
        if (s.startsWith("{")) {
          return parseDict(s.substring(1, s.length()));
        } else {
          return getType(s);
        }
      }
    }
    return "";
  }

  // naivly parse simple dict "type":"array"
  private String parseDict(String dict) {
    String [] toRet = dict.split(":");
    if (toRet[1] != null) {
      return getType(toRet[1]);
    }
    return "";
  }
}
