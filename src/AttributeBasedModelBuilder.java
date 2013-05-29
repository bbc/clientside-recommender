import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mymedialite.correlation.BinaryCosine;
import org.mymedialite.correlation.CorrelationMatrix;
import org.mymedialite.data.EntityMapping;
import org.mymedialite.datatype.SparseBooleanMatrix;

/**
 * Utility for building for item attribute based kNN models 
 * See TODO below for where additional data is required 
 * @author Chris Newell, BBC R&D
 */
public class AttributeBasedModelBuilder {

  // Configuration
  public static final String MODEL_FILE_NAME = "receng-model.js";
  public static final int K = 20;
  protected static EntityMapping attributeMapping = new EntityMapping();
  
  public static void main(String[] args) {
    try {
      createModel(MODEL_FILE_NAME);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
//--------------------------------------------------------------------------------------------------
  public static void createModel(String filename) throws IOException {
    
    String timestamp = "2013-04-23T10:00:00Z";  // TODO Set as required
    
    List<String> orderedItemIds = new ArrayList<String>();  // TODO add your item identifiers to this list in the required default order (we use item popularity)

    SparseBooleanMatrix itemAttributes = new SparseBooleanMatrix();
    for(int i=0; i < orderedItemIds.size(); i++) {
      Set<String> attributes = new HashSet<String>();  // TODO add the attributes for the item to this list
      for(String attribute : attributes) {
        Integer attributeId = attributeMapping.toInternalID(attribute);
        itemAttributes.set(i, attributeId, true);
      }
    }

    // Build the correlation matrix
    CorrelationMatrix correlationMatrix = BinaryCosine.create(itemAttributes);
    int numItems = itemAttributes.numberOfRows();
    int[][] nn = new int[numItems][];
    for (int i = 0; i < numItems; i++)  nn[i] = correlationMatrix.getNearestNeighbors(i, K);

    // Write metadata
    PrintWriter writer = new PrintWriter(new FileWriter(filename));
    writer.println("Receng.timestamp = \"" + timestamp + "\"");
    writer.print("Receng.items = new Array(");
    boolean firstItem = true;
    
    for(int i=0; i < orderedItemIds.size(); i++) {
      String itemId = orderedItemIds.get(i);
      
      // TODO Set the following metadata fields for each item
      String title = "Item title";
      int genre = 0;                // Use a lookup table e.g. Comedy: 0, Drama: 1, Factual: 3, etc
      int mediaType = 0;            // e.g. Video: 1, Audio: 0 
 
      if(firstItem) { firstItem = false; } else { writer.print(","); }
      writer.print("{p:\"" + itemId + "\",t:\"" + title + "\",g:" + genre + ",v:" + mediaType + "}");       
    }
    writer.println(");");
    writer.println();
    
    // Write the nearest neighbor ids
    writer.print("Receng.nn = new Array(");
    boolean firstRow = true;
    for(int[] row : nn) {
      if(firstRow) { firstRow = false; } else { writer.print(","); }    
      writer.print("new Array(");
      boolean firstCol = true;
      for(int n : row) {
        if(firstCol) { firstCol = false; } else { writer.print(","); } 
        writer.print(n);
      }
      writer.print(")");
    }
    writer.println(");");
    writer.println();

    // Write the nearest neighbor weights
    writer.print("Receng.weights = new Array(");
    firstRow = true;
    for(int i = 0; i < nn.length; i++) {
      int[] row = nn[i];
      if(firstRow) { firstRow = false; } else { writer.print(","); }    
      writer.print("new Array(");
      boolean firstCol = true;
      for(int n : row) {
        if(firstCol) { firstCol = false; } else { writer.print(","); }
        float correlation = correlationMatrix.get(i, n);
        int cc = Math.round(correlation * 1000);
        if(cc == 1000) {
          cc = 999;
        }
        writer.print(cc);
      }
      writer.print(")");
    }
    writer.println(");");
    writer.println();
    
    writer.flush();
    writer.close();
  }
//--------------------------------------------------------------------------------------------------
}
