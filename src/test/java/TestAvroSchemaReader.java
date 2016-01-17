import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
 	
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.Map;

public class TestAvroSchemaReader {

  private final static String AVRO_SCHEMA_PATH = "src/test/resources/test_schema.avsc";

  private static AvroSchema as;

  @BeforeClass
  public static void initialize() {
    try {
      as = new AvroSchema(AVRO_SCHEMA_PATH);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  @Test
  public void testClassName() {
    assertEquals(as.getClassName(), "product");
  }

  @Test
  public void testDetectedFields() {
    String [] fields = {"id", "look_id", "color_id", "photo_group_id", "product_id", "item_group_id",
              "name", "title", "description", "gender", "age_group", "sale_price", 
              "return_days", "additional_image_link"};

    Set<String> schemaFields = as.getFieldsSet();

    assertTrue(fields.length == schemaFields.size());

    for (int i=0; i<fields.length; ++i) {
      assertTrue(schemaFields.contains(fields[i]));
    }
  }

  @Test
  public void testAliases() {
    // check if given fields is an alias for other given fields
    Map<String, String> fields = as.getFields();
    assertTrue(fields.containsKey("id"));
    assertEquals(fields.get("id"), "id");
    assertTrue(fields.containsKey("look_id"));
    assertEquals(fields.get("look_id"), "look_id");
    assertEquals(fields.get("color_id"), "look_id");
    assertEquals(fields.get("photo_group_id"), "look_id");
    assertTrue(fields.containsKey("name"));
    assertEquals(fields.get("name"), "name");
    assertEquals(fields.get("title"), "name");
  }
}
