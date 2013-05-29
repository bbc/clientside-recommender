import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import org.mymedialite.correlation.BinaryCosine;
import org.mymedialite.correlation.CorrelationMatrix;
import org.mymedialite.data.EntityMapping;
import org.mymedialite.data.IPosOnlyFeedback;
import org.mymedialite.io.ItemData;
import org.mymedialite.itemrec.Extensions;
import org.mymedialite.itemrec.MostPopular;

/**
 * Utility for building collaborative filtering kNN models from binary (positive only) feedback.
 * See TODO below for where additional data is required
 * Default ordering for programmes is by popularity
 * @author Chris Newell, BBC R&D
 */
public class CollaborativeFilteringModelBuilder {

  // Configuration
  public static final String FEEDBACK_FILE_NAME = "feedback.dat";
  public static final String MODEL_FILE_NAME = "receng-model.js";
  protected static EntityMapping userMapping = new EntityMapping();
  protected static EntityMapping itemMapping = new EntityMapping();
  public static final int K = 20;
  
  public static void main(String[] args) {
    try {
      createModel(FEEDBACK_FILE_NAME, MODEL_FILE_NAME);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
//--------------------------------------------------------------------------------------------------
  public static void createModel(String feedbackFilename, String modelFilename) throws Exception {
    
    String timestamp = "2013-04-23T10:00:00Z";  // TODO Set as required
    
    /*
     * Read data from binary (positive-only) feedback file
     * Line format:  <userId> <delim> <itemId>
     * Where <delim> is either:
     *   a tab
     *   a space
     *   a comma
     *   a semicolon
     *   two colons (i.e. "::")
     */
    IPosOnlyFeedback trainingData = ItemData.read(feedbackFilename, userMapping, itemMapping, false);
    System.out.println("Users: " + trainingData.allUsers().size() + 
      " Items: " + trainingData.allItems().size() +
      " Viewings: " + trainingData.size());
    
    MostPopular popular = new MostPopular();
    popular.setFeedback(trainingData); 
    popular.train();
    List<Integer> internalIds = Extensions.predictItems(popular, 0, trainingData.allItems());
    
    // Build the correlation matrix
    CorrelationMatrix correlationMatrix = BinaryCosine.create(trainingData.itemMatrix());
    int numItems = trainingData.itemMatrix().numberOfRows();
    int[][] nn = new int[numItems][];
    for (int i = 0; i < numItems; i++)  nn[i] = correlationMatrix.getNearestNeighbors(i, K);

    PrintWriter writer = new PrintWriter(new FileWriter(modelFilename));
    writer.println("Receng.timestamp = \"" + timestamp + "\";");
    writer.print("Receng.items = new Array(");
    boolean firstItem = true;

    // Write metadata
    for(int i : internalIds) {
      String itemId = itemMapping.toOriginalID(i);
      
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
    for(int i : internalIds) {
      int[] row = correlationMatrix.getNearestNeighbors(i, K);
      if(firstRow) { firstRow = false; } else { writer.print(","); }    
      writer.print("new Array(");
      boolean firstCol = true;
      for(int n : row) {
        if(firstCol) { firstCol = false; } else { writer.print(","); }
        writer.print(internalIds.indexOf(n));
      }
      writer.print(")");
    }
    writer.println(");");
    writer.println();

    // Write the nearest neighbor weights
    writer.print("Receng.weights = new Array(");
    firstRow = true;
    for(int i : internalIds) {
      int[] row = nn[i];
      if(firstRow) { firstRow = false; } else { writer.print(","); }    
      writer.print("new Array(");
      boolean firstCol = true;
      for(int n : row) {
        if(firstCol) { firstCol = false; } else { writer.print(","); }
        float correlation = correlationMatrix.get(i, n);
        int cc = Math.round(correlation * 1000);
        if(cc > 999) cc = 999;
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
