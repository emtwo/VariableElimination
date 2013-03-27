import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Factor {
  ArrayList<Row> rowList = new ArrayList<Row>();
  HashMap<String, Integer> variableNameToIntMapping = new HashMap<String, Integer>();
  HashMap<String, Double> chartData = new HashMap<String, Double>(); // eg. TFT = 0 1 2
  public ArrayList<String> intToVariableNameMapping;
  int id;
  String probabilityString = "";
  
  public Factor(ArrayList<String> variables, int id) {
    intToVariableNameMapping = variables;
    for (int i = 0; i < variables.size(); i++) {
      variableNameToIntMapping.put(variables.get(i), i);
    }
    this.id = id;

    if (intToVariableNameMapping.size() > 0) {
      String val = intToVariableNameMapping.get(0);
      for (int i = 1; i < intToVariableNameMapping.size(); i++) {
        val += (", " + intToVariableNameMapping.get(i));
      }
      probabilityString = "f" + id +"(" + val + ")";
    }
  }
  
  public Factor(ArrayList<String> variables, int id, String var, String restriction) {
    this(variables, id);
    if (intToVariableNameMapping.size() > 0) {
      String val = intToVariableNameMapping.get(0);
      for (int i = 1; i < intToVariableNameMapping.size(); i++) {
        val += (", " + intToVariableNameMapping.get(i));
      }
      probabilityString = "f" + id +" (" + val + ", " + var + " = " + restriction +")";
    }
  }

  public String nameGivenInt(int position) {
    return intToVariableNameMapping.get(position);
  }
  
  public Integer intGivenName(String name) {
    return variableNameToIntMapping.get(name);
  }
  
  public void addRow(ArrayList<String> vals, String key, double probability) {
    chartData.put(key, probability);
    rowList.add(new Row(vals, probability));
  }
  
  public void addRow(Row row) {
    chartData.put(row.toString(), row.probability);
    rowList.add(row);
  }
  
  public void setChartData(HashMap<String, Double> chartData) {
    this.chartData = chartData;
  }
  
  public static String valueGivenPosition(int position, String key) {
    return String.valueOf(key.charAt(position));
  }
  
  public Integer positionGivenVariableName(String value) {
    return variableNameToIntMapping.get(value);
  }
  
  public Set<String> getVarsSet(boolean includeRightColumn) {
    Set<String> varSet = new HashSet<String>();
    for (int i = 0; i < intToVariableNameMapping.size(); i++) {
      varSet.add(intToVariableNameMapping.get(i));
    }
    return varSet;
  }
  
  public Row getRowGivenVals(HashMap<String, String> valsGivenVars) {
    for (Row row : rowList) {
      boolean match = true;
      for (int i = 0; i < row.tfList.size(); i++) {
        String varName = intToVariableNameMapping.get(i);
        String varInput = valsGivenVars.get(varName);
        if (varInput != null && !varInput.equals(row.tfList.get(i))) {
          match = false;
          break;
        }
      }
      if (match) {
        return row;
      }
    }
    return null;
  }
  
  public ArrayList<String> getVarsArray() {
    return intToVariableNameMapping;
  }
  
  public HashMap<String, Double> getChartData() {
    return chartData;
  }
  
  public ArrayList<Row> getRowList() {
    return rowList;
  }
  
  public void print() {
    System.out.println(probabilityString);
    
    for (int i = 0; i < intToVariableNameMapping.size(); i++) {
      System.out.print(intToVariableNameMapping.get(i) + "\t");
    }
    System.out.println("");
    for (String key : chartData.keySet()) {
      for (int i = 0; i < intToVariableNameMapping.size(); i++) {
        System.out.print(valueGivenPosition(i, key) + "\t");
      }
      System.out.println(chartData.get(key));
    }
    System.out.println("");
  }
}
