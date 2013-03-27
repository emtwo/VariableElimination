import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class VariableElimination {

  public static int i = 0;
  
  public static Factor inference(Factor[] factorList, String[] queryVariables,
      String[] orderedListOfHiddenVariables, HashMap<String, String> evidenceList) {
    ArrayList<Factor> restrictedFactorList = new ArrayList<Factor>();
    
    // Restrict factors.
    for (int i = 0; i < factorList.length; i++) {
      Factor restrictedFactor = factorList[i];
      for (String variable : evidenceList.keySet()) {
        Integer position = restrictedFactor.intGivenName(variable);
        if (position != null) {
          System.out.println("f" + restrictedFactor.id + " (" + variable + " = " + evidenceList.get(variable) + ")");
          restrictedFactor = restrict(restrictedFactor, variable, evidenceList.get(variable));
          restrictedFactor.print();
        }
      }
      restrictedFactorList.add(restrictedFactor);
      
    }

    for (String hiddenVar : orderedListOfHiddenVariables) {
      // Multiply restricted factors.
      ArrayList<Factor> factorsToMultiply = new ArrayList<Factor>();
      String factorMultiplyString = "";
      for (Factor restrictedFactor : restrictedFactorList) {
        Set<String> restrictedFactorVars = restrictedFactor.getVarsSet(true);
        if (restrictedFactorVars.contains(hiddenVar)) {
          factorsToMultiply.add(restrictedFactor);
          if (!factorMultiplyString.equals("")) {
            factorMultiplyString += " * ";
          }
          factorMultiplyString += restrictedFactor.probabilityString;
        }
      }
      
      if (factorsToMultiply.size() == 0) {
        System.out.println("Nothing to multiply for hidden variable " + hiddenVar);
        continue;
      }
      
      System.out.println("Sum over " + hiddenVar + " ( " + factorMultiplyString +" )");
      Factor multipliedFactorResult = factorsToMultiply.get(0);
      for (int i = 1; i < factorsToMultiply.size(); i++) {
        System.out.print(multipliedFactorResult.probabilityString + " * " + factorsToMultiply.get(i).probabilityString + " = ");
        multipliedFactorResult = multiply(multipliedFactorResult, factorsToMultiply.get(i));
        multipliedFactorResult.print();
      }

      // Sumout the hidden variables.
      Factor summedOutFactor = sumout(multipliedFactorResult, hiddenVar);
      System.out.println("Summout " + hiddenVar + " from " + multipliedFactorResult.probabilityString);
      summedOutFactor.print();
      
      // Add the newly created factor
      restrictedFactorList.add(summedOutFactor);
      
      // Remove the summed out factors.
      for (Factor f : factorsToMultiply) {
        restrictedFactorList.remove(f);
      }
      
      // Print updated Factor list.
      System.out.print("Updated Factor Set: { ");
      for (Factor f : restrictedFactorList) {
        System.out.print(f.probabilityString + " ");
      }
      System.out.println("}\n");
    }

    System.out.print("Remaining Factors to be Normalized { ");
    for (Factor f : restrictedFactorList) {
      System.out.print(f.probabilityString + " ");
    }
    System.out.println("}\n");
    
    Factor multipliedFactorResult = restrictedFactorList.get(0);
    for (int i = 1; i < restrictedFactorList.size(); i++) {
      System.out.print(multipliedFactorResult.probabilityString + " * " + restrictedFactorList.get(i).probabilityString + " = ");
      multipliedFactorResult = multiply(multipliedFactorResult, restrictedFactorList.get(i));
      multipliedFactorResult.print();  
    }
    
    System.out.println("Normalized Result:");
    return normalize(multipliedFactorResult);
  }
  
  public static Factor normalize(Factor factor) {
    Factor returnFactor = new Factor(factor.intToVariableNameMapping, i++);
    double sum = 0;
    for (Row row : factor.rowList) {
      sum += row.probability;
    }
    for (Row row : factor.rowList) {
      returnFactor.addRow(new Row(row.tfList, row.probability / sum));
    }
    return returnFactor;
  }
  
  public static Factor sumout(Factor factor, String variable) {
    Integer position = factor.positionGivenVariableName(variable);
    if (position == null) {
      return factor;
    }
    HashMap<String, Double> groupedMap = new HashMap<String, Double>();
    for (Row row : factor.getRowList()) {
      String key = "";
      for (int i = 0; i < row.tfList.size(); i++) {
        if (i != position) {
          key += row.tfList.get(i);
        }
      }
      Double probability = groupedMap.get(key);
      if (probability != null) {
        probability += row.probability;
      } else {
        probability = row.probability;
      }
      groupedMap.put(key, probability);
    }

    ArrayList<String> variables = factor.getVarsArray();
    ArrayList<String> newVariables = new ArrayList<String>(variables.size() - 1);
    for (String v : variables) {
      if (!v.equals(variable)) {
        newVariables.add(v);
      }
    }
    Factor returnFactor = new Factor(newVariables, i++);
    for (String key : groupedMap.keySet()) {
      Double val = groupedMap.get(key);
      Row newRow = Row.toRow(key, val);
      returnFactor.addRow(newRow);
    }
    
    return returnFactor;
  }
  
  public static Factor multiply(Factor factor1, Factor factor2) {
    Set<String> factor1Vars = factor1.getVarsSet(true);
    Set<String> factor2Vars = factor2.getVarsSet(true);

    Set<String> union = new HashSet<String>(factor1Vars);
    union.addAll(factor2Vars);
    
    // Put the union into an array to be used for the new factor
    ArrayList<String> unionArray = new ArrayList<String>(union.size());
    for (String var : union) {
      unionArray.add(var);
    }
    Factor returnFactor = new Factor(unionArray, i++);
    
    for (int k = 0; k < Math.pow(2, union.size()); k++) {
      ArrayList<String> newTFList = new ArrayList<String>();
      HashMap<String, String> varsToVals = new HashMap<String, String>();
      
      for (int j = unionArray.size() - 1; j > -1; j--) {
        int val = (1 << j) & k;
        String stringVal;
        if (val == 0) {
          stringVal = "F";
        } else {
          stringVal = "T";
        }
        newTFList.add(stringVal);
        varsToVals.put(unionArray.get(unionArray.size() - 1 - j), stringVal);
        
      }
      Row row1 = factor1.getRowGivenVals(varsToVals);
      Row row2 = factor2.getRowGivenVals(varsToVals);
      
      returnFactor.addRow(new Row(newTFList, row1.probability * row2.probability));
    }
    return returnFactor;
  }

  public static Factor restrict(Factor factor, String variable, String value) {
    // Create a new factor without the column to be restricted.
    ArrayList<String> varSet = new ArrayList<String>(factor.getVarsArray());
    varSet.remove(variable);
    Factor newFactor = new Factor(varSet, i++, variable, value);

    int position = factor.intGivenName(variable);
    
    ArrayList<Row> rowList = factor.getRowList();
    for (Row row : rowList) {
      ArrayList<String> newTFList = new ArrayList<String>(row.tfList);
      newTFList.remove(position);
      if (row.tfList.get(position).equals(value)) {
        newFactor.addRow(new Row(newTFList, row.probability));
      }
    }
    return newFactor;
  }
  
  public static void main(String args[]) {
    System.out.println("INITIAL FACTORS:");
    System.out.println("================");
    Factor LW = new Factor(new ArrayList<String>(Arrays.asList("LW")), i++);
    LW.addRow(new Row(new ArrayList<String>(Arrays.asList("T")), 0.6));
    LW.addRow(new Row(new ArrayList<String>(Arrays.asList("F")), 0.4));
    LW.print();
    
    Factor Lett = new Factor(new ArrayList<String>(Arrays.asList("Lett")), i++);
    Lett.addRow(new Row(new ArrayList<String>(Arrays.asList("T")), 0.05));
    Lett.addRow(new Row(new ArrayList<String>(Arrays.asList("F")), 0.95));
    Lett.print();
    
    Factor Help = new Factor(new ArrayList<String>(Arrays.asList("Lett", "Help")), i++);
    Help.addRow(new Row(new ArrayList<String>(Arrays.asList("T", "T")), 0.25));
    Help.addRow(new Row(new ArrayList<String>(Arrays.asList("F", "T")), 0.004));
    Help.addRow(new Row(new ArrayList<String>(Arrays.asList("T", "F")), 0.75));
    Help.addRow(new Row(new ArrayList<String>(Arrays.asList("F", "F")), 0.996));
    Help.print();

    Factor Em = new Factor(new ArrayList<String>(Arrays.asList("LW", "Em")), i++);
    Em.addRow(new Row(new ArrayList<String>(Arrays.asList("T", "T")), 0.9));
    Em.addRow(new Row(new ArrayList<String>(Arrays.asList("F", "T")), 0.001));
    Em.addRow(new Row(new ArrayList<String>(Arrays.asList("T", "F")), 0.1));
    Em.addRow(new Row(new ArrayList<String>(Arrays.asList("F", "F")), 0.999));
    Em.print();
    
    Factor Clik = new Factor(new ArrayList<String>(Arrays.asList("Help", "Lett", "Clik")), i++);
    Clik.addRow(new Row(new ArrayList<String>(Arrays.asList("T", "T", "T")), 0.9));
    Clik.addRow(new Row(new ArrayList<String>(Arrays.asList("T", "F", "T")), 0.1));
    Clik.addRow(new Row(new ArrayList<String>(Arrays.asList("F", "T", "T")), 0.9));
    Clik.addRow(new Row(new ArrayList<String>(Arrays.asList("F", "F", "T")), 0.01));
    Clik.addRow(new Row(new ArrayList<String>(Arrays.asList("T", "T", "F")), 0.1));
    Clik.addRow(new Row(new ArrayList<String>(Arrays.asList("T", "F", "F")), 0.9));
    Clik.addRow(new Row(new ArrayList<String>(Arrays.asList("F", "T", "F")), 0.1));
    Clik.addRow(new Row(new ArrayList<String>(Arrays.asList("F", "F", "F")), 0.99));
    Clik.print();
    
    Factor Worr = new Factor(new ArrayList<String>(Arrays.asList("Help", "LW", "Worr")), i++);
    Worr.addRow(new Row(new ArrayList<String>(Arrays.asList("T", "T", "T")), 0.02));
    Worr.addRow(new Row(new ArrayList<String>(Arrays.asList("T", "F", "T")), 0.011));
    Worr.addRow(new Row(new ArrayList<String>(Arrays.asList("F", "T", "T")), 0.01));
    Worr.addRow(new Row(new ArrayList<String>(Arrays.asList("F", "F", "T")), 0.001));
    Worr.addRow(new Row(new ArrayList<String>(Arrays.asList("T", "T", "F")), 0.98));
    Worr.addRow(new Row(new ArrayList<String>(Arrays.asList("T", "F", "F")), 0.989));
    Worr.addRow(new Row(new ArrayList<String>(Arrays.asList("F", "T", "F")), 0.99));
    Worr.addRow(new Row(new ArrayList<String>(Arrays.asList("F", "F", "F")), 0.999));
    Worr.print();
    
    System.out.println("PROBLEM 1: FIND P (Help)");
    System.out.println("=======================");
    Factor f1 = inference(new Factor[] {LW, Lett, Help, Em, Clik, Worr},
        new String[] {"Help"}, new String[] {"Lett", "Clik", "Worr", "LW", "Em"}, new HashMap<String, String>());
    f1.print();
    i = 0;
    
    System.out.println("PROBLEM 2: FIND P (Help | Lett = T)");
    System.out.println("==================================");
    HashMap<String, String> restrictions = new HashMap<String, String>();
    restrictions.put("Lett", "T");
    Factor f2 = inference(new Factor[] {LW, Lett, Help, Em, Clik, Worr},
        new String[] {"Help"}, new String[] {"Clik", "Worr", "LW", "Em"}, restrictions);
    f2.print();
    i = 0;

    System.out.println("PROBLEM 3: FIND P (Help | Lett = T, Worr = T)");
    System.out.println("============================================");
    restrictions = new HashMap<String, String>();
    restrictions.put("Lett", "T");
    restrictions.put("Worr", "T");
    Factor f3 = inference(new Factor[] {LW, Lett, Help, Em, Clik, Worr},
        new String[] {"Help"}, new String[] {"Clik", "LW", "Em"}, restrictions);
    f3.print();
    i = 0;
      
    System.out.println("PROBLEM 4: FIND P (Help | Lett = T, Worr = T, Em = T)");
    System.out.println("====================================================");
    restrictions = new HashMap<String, String>();
    restrictions.put("Lett", "T");
    restrictions.put("Worr", "T");
    restrictions.put("Em", "T");
    Factor f4 = inference(new Factor[] {LW, Lett, Help, Em, Clik, Worr},
        new String[] {"Help"}, new String[] {"Clik", "LW", "Em"}, restrictions);
    f4.print();
    i = 0;
    
    System.out.println("PROBLEM 5: FIND P (Help | Lett = T, Worr = T, Em = F)");
    System.out.println("====================================================");
    restrictions = new HashMap<String, String>();
    restrictions.put("Lett", "T");
    restrictions.put("Worr", "T");
    restrictions.put("Em", "F");
    Factor f5 = inference(new Factor[] {LW, Lett, Help, Em, Clik, Worr},
        new String[] {"Help"}, new String[] {"Clik", "LW"}, restrictions);
    f5.print();
  }
}
