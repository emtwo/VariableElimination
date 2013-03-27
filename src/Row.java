import java.util.ArrayList;

public class Row {
  ArrayList<String> tfList;
  double probability;
  public Row(ArrayList<String> rowVals, double stat) {
    tfList = rowVals;
    probability = stat;
  }
  
  public String toString() {
    String rowString = "";
    for (String var : tfList) {
      rowString += var;
    }
    return rowString;
  }
  
  public static Row toRow(String varString, double probability) {
    ArrayList<String> varStrings = new ArrayList<String>(varString.length());
    for (int i = 0; i < varString.length(); i++) {
      varStrings.add(String.valueOf(varString.charAt(i)));
    }
    return new Row(varStrings, probability);
  }
}