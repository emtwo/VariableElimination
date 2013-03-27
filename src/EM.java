import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class EM {
  static String dataFile = "/Users/emtwo/Desktop/Documents/4B/CS 486/A4/trainData.txt";
  
  public static void main(String[] args) {
    try {
      FileInputStream fstream = new FileInputStream(dataFile);
      DataInputStream in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));

      String strLine;
      while ((strLine = br.readLine()) != null)   {
        System.out.println (strLine);
      }
      in.close();
    } catch (Exception e){
      System.err.println("Error: " + e.getMessage());
    }
  }
}
