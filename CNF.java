/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package cnf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author Gaurab Dahal <gaurab dot d at gmail dot com>
 */
public class CNF {

    private HashMap<String, String> availableNonTerminal;
    HashSet<String> reservedNonTerminal;
    List<Grammar> cnf;

    public CNF(Scanner inputStream) {
        if (inputStream == null) {
            System.out.println("Invalid input");
            System.exit(0);
        }
        //cnf= new List<Grammar>();
        availableNonTerminal = new HashMap<>();
        reservedNonTerminal = new HashSet<>();
        ArrayList<String> inputList = new ArrayList<>();
        System.out.println("Input is :");
        while (inputStream.hasNext()) {
            String temp = inputStream.next();
            inputList.add(temp);
            System.out.println(temp);
        }

        System.out.println("Chomskey normal form is :\n --------------------------");

        ArrayList<Grammar> cfg = this.getHashMappedInput(inputList);

        cnf = new LambdaProductionRemover(cfg).removeLambdaProduction();
        convertTerminalToNonTerminal();
        //normalize();
        //display(cnf);
        normalize();
        display(cnf);
    }

    private void convertTerminalToNonTerminal() {
        for (Grammar g : cnf) {
            for (int i = 0; i < g.expressions.size(); i++) {
                g.expressions.set(i, terminalToNonTerminal(g.expressions.get(i)));
            }

        }
    }
    
    private void normalize() {
        for (Grammar g : cnf) {
            for (int i = 0; i < g.expressions.size(); i++) {
               // g.expressions.set(i, terminalToNonTerminal(g.expressions.get(i)));
               if(g.expressions.get(i).length()>2){
                   String substring = g.expressions.get(i).substring(1, 3); 
                   
                    Iterator it = availableNonTerminal.entrySet().iterator();
                    boolean found=false;
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    if (pair.getValue().equals(substring)) {
                        g.expressions.set(i, g.expressions.get(i).replace(substring,(String)pair.getKey()));
                        found=true;
                    }
                   // it.remove(); // avoids a ConcurrentModificationException
                }
                if(!found){
                    String uniqueChar = getUniqueChar();
                    availableNonTerminal.put(uniqueChar,substring);
                    g.expressions.set(i,  g.expressions.get(i).replace(substring,uniqueChar));
                }
                   //now assign new capital letetr to this substring 
                   //add to reserved word
                   //remove old combinatio
               }
                normalizeIntoTwo();
            }

        }
    }
    
    private String getUniqueChar(){
        String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M"};
        for (String s : letters) {
                        //first select a unique capital letter that not been used in grammar
                        //then assign the small letter to that capital letter
                        //then mark that capital letter as reserved
                        if (!reservedNonTerminal.contains(s)) {
                            reservedNonTerminal.add(s);
                            return s;
                        }
                    }
        return "Q";
    }

    private ArrayList getHashMappedInput(ArrayList<String> input) {
        ListIterator it = input.listIterator();
        ArrayList<Grammar> hs = new ArrayList<Grammar>();
        while (it.hasNext()) {
            String[] item = ((String) it.next()).split("->");
            String[] rhs = item[1].split("\\|");
            List<String> l = new ArrayList<>();
            for (String rh : rhs) {
                l.add(rh.trim());
                saveLetterInReservedHashSet(rh.trim());
            }
            hs.add(new Grammar(item[0].trim(), l));
        }

        return hs;
    }

    //save non terminals to reserved hashset so that they cannot be used again
    //while converting small letter to capital letter in step4
    private void saveLetterInReservedHashSet(String s) {
        for (int i = 0; i < s.length(); i++) {
            int ascii = (int) s.charAt(i);
            if (ascii >= 65 && ascii <= 91) {
                reservedNonTerminal.add(s.charAt(i) + "");
            }
        }
    }

    private void display(List<Grammar> grammars) {
        for (Grammar grammar : grammars) {
            System.out.print(grammar.key + "->");
            iterateList(grammar.expressions);
            System.out.println("");
        }
        printNewVariables();
    }

    private void iterateList(List<String> list) {
        ListIterator it = list.listIterator();
        while (it.hasNext()) {
            System.out.print(it.next() + "|");
        }
    }

    private void printNewVariables() {
        Iterator it = availableNonTerminal.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey() + " -> " + pair.getValue());
           // it.remove(); // avoids a ConcurrentModificationException
        }
    }

    private void terminalToUnitProduction() {

    }

    private String terminalToNonTerminal(String grammar) {
        //System.out.println("grammar is ==>"+grammar);
        String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M"};
        for (int i = 0; i < grammar.length(); i++) {
            if ((int) grammar.charAt(i) >= 97 && (int) grammar.charAt(i) <= 123) {
                boolean notfound = true;
                // search this string through asmap is any variable is already assigned
                Iterator it = availableNonTerminal.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    if (pair.getValue().equals(grammar.charAt(i) + "")) {
                        grammar = grammar.replace(grammar.charAt(i) + "", (String) pair.getKey());
                        notfound = false;
                    }
                   // it.remove(); // avoids a ConcurrentModificationException
                }
                if (notfound) {
                    boolean found = false;
                    for (String s : letters) {
                        //first select a unique capital letter that not been used in grammar
                        //then assign the small letter to that capital letter
                        //then mark that capital letter as reserved
                        if (!reservedNonTerminal.contains(s) && !found) {
                            found = true;
                            availableNonTerminal.put(s, grammar.charAt(i) + "");
                            reservedNonTerminal.add(s);
                            grammar = grammar.replace(grammar.charAt(i) + "", s);
                            // System.out.println("===>"+grammar);
                        }
                    }
                }
            }
        }
        //System.out.println("updated grammar is ==>"+grammar);
        return grammar;
    }
    
    private void normalizeIntoTwo(){
        //if character length is greater than 2, introduce a new variable
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Scanner inputStream = null;
        try {
            inputStream = new Scanner(new FileInputStream("input.txt"));
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
            System.exit(0);
        }

        CNF cnf = new CNF(inputStream);
    }

}

class LambdaProductionRemover {

    ArrayList<Grammar> map;
    HashSet<String> lambdaList;

    public LambdaProductionRemover(ArrayList<Grammar> map) {
        this.map = map;
        lambdaList = new HashSet<>();
    }

    protected ArrayList removeLambdaProduction() {
        ArrayList<String> variablesWithLambdaProduction = new ArrayList<>();
        for (Grammar grammar : map) {
            if (hasLambdaProduction(grammar.expressions)) {
                grammar.expressions.remove(grammar.expressions.indexOf("^"));
                variablesWithLambdaProduction.add(grammar.key);
                lambdaList.add(grammar.key);
                grammar.expressions = removeLambda(grammar);
            }
        }

        //if rule is S->ASA and A->^ , produce S->ASA|AS|SA
        if (variablesWithLambdaProduction.size() > 0) {
            ArrayList<String> expandedRules = null;
            for (Grammar grammar : map) {
                ArrayList<String> temp = new ArrayList<>();
                for (String s : grammar.expressions) {
                    for (String variableWithLambdaProduction : variablesWithLambdaProduction) {
                        expandedRules = replaceLambda(s, variableWithLambdaProduction.charAt(0));
                        if (expandedRules != null) {
                            for (Object x : expandedRules) {
                                if (!temp.contains(x)) {
                                    temp.add((String) x);
                                }
                            }
                        }
                    }
                }
                for (Object x : temp) {
                    if (!grammar.expressions.contains(x)) {
                        grammar.expressions.add((String) x);
                    }
                }
            }
        }
        return map;
    }

    private List removeLambda(Grammar grammar) {
        ArrayList<String> t = new ArrayList<>();
        ListIterator it = grammar.expressions.listIterator();
        while (it.hasNext()) {
            String temp = it.next() + "";
            t.add(temp);
            if (temp.contains(grammar.key)) {
                String a = temp.replace(grammar.key, "");
                t.add(grammar.expressions.indexOf(temp), a);
            }
        }
        return t;
    }

    public  ArrayList<String> replaceLambda(String grammar, char key) {
                String tempString = "";
                ArrayList<String> list = new ArrayList<String>();
                char tempChar;
                int prevLoc = 0;
                int count = 0;
                int countCheck = 0;
                for (int i = 0; i < grammar.length(); i++) {
                        if (grammar.charAt(i) == key) {
                                count++;
                        }
                }
                for (int i = 0; i < count; i++) {
                        tempString = "";
                      //  System.out.println("|");
                        if (prevLoc < count) {
                                prevLoc++;
                        }
                        countCheck = 0;
                        for (int j = 0; j < grammar.length(); j++) {
                                if (grammar.charAt(j) == key) {
                                        countCheck++;
                                        if (countCheck != prevLoc) {
                                                tempChar = grammar.charAt(j);
                                                tempString += tempChar;
                                        }
                                } else {
                                        tempChar = grammar.charAt(j);
                                        tempString += tempChar;
                                }
                        }
                       // System.out.println(tempString);
                        list.add(tempString);
                }
                return (list);
        }
    
//    private ArrayList<String> replaceLambda(String grammar, String key) {
//        //return null;
//        ArrayList<String> t = new ArrayList<>();
//        if (grammar.equals("ASA")) {
//            t.add("ASA");
//            t.add("AS");
//            t.add("SA");
//            return t;
//        }
//
//        if (grammar.equals("aAb")) {
//            t.add("aAb");
//            t.add("ab");
//            return t;
//        }
//        return null;
//    }

    /*
    Checks if given array list contains any lambda production
     */
    private boolean hasLambdaProduction(List<String> expressions) {
        ListIterator it = expressions.listIterator();
        while (it.hasNext()) {
            if (it.next().equals("^")) {
                return true;
            }
        }
        return false;
    }
}

class Grammar {

    protected String key;
    protected List<String> expressions;

    public Grammar(String key, List<String> list) {
        this.key = key;
        expressions = list;
    }
}
