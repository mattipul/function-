package function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Database {

    private HashMap<String, Set> sets;
    private HashMap<String, Vector> vectors;
    private HashMap<String, String> variables;
    private HashMap<String, Function> functions;
    private HashMap<String, Tape> tapes;
    private ArrayList<String> generatedList;
    private ArrayList<String> rollbacks;

    public Database() {
        this.sets = new HashMap<>();
        this.variables = new HashMap<>();
        this.functions = new HashMap<>();
        this.vectors = new HashMap<>();
        this.tapes=new HashMap<>();
        this.rollbacks = new ArrayList<>();
        this.generatedList = new ArrayList<>();
    }

    public int getRealType(String s) {
        Set set = getSetReal(s);
        Vector vector = getVectorReal(s);
        Function f = getFunctionReal(s);
        Tape t=getTapeReal(s);
        if (set != null) {
            return 1;
        }
        if (vector != null) {
            return 2;
        }
        if (f != null) {
            return 3;
        }
        if(t!=null){
            return 5;
        }
        return 4;
    }

    public String outputFunction(Function f) {
        String fStr = "";
        if (!f.getDefinitions().isEmpty()) {
            for (String def : f.getDefinitions().keySet()) {
                if (!def.equals("otherwise")) {
                    fStr += f.getDefinition() + "=" + f.getDefinitions().get(def) + ", when " + def + "\n";
                } else {
                    if (f.getDefinitions().size() == 1) {
                        fStr += f.getDefinition() + "=" + f.getDefinitions().get(def) + "\n";
                    } else {
                        fStr += f.getDefinition() + "=" + f.getDefinitions().get(def) + ", otherwise" + "\n";
                    }
                }
            }
        }
        return fStr;
    }

    public String outputSet(Set set) {
        String setStr = "";
        for (String s : set.getSymbols()) {
            if (getType(s) == 1) {
                if (this.generatedList.contains(s)) {
                    setStr += outputSet(getSetReal(s)) + ",";
                } else {
                    setStr += s + ",";
                }
            }
            if (getType(s) == 2) {
                int realType = getRealType(s);
                if (realType == 4) {
                    if (this.generatedList.contains(s)) {
                        Double var = toValueInt(s);
                        if (var != null) {
                            setStr += var + ",";
                        }
                    } else {
                        setStr += s + ",";
                    }
                }
                if (realType == 1) {
                    if (this.generatedList.contains(s)) {
                        setStr += outputSet(getSetReal(s)) + ",";
                    } else {
                        setStr += s + ",";
                    }
                }
                if (realType == 2) {
                    if (this.generatedList.contains(s)) {
                        setStr += outputVector(getVectorReal(s)) + ",";
                    } else {
                        setStr += s + ",";
                    }
                }
                if (realType == 3) {
                    setStr += s + ",";
                }
            }
            if (getType(s) == 3) {
                setStr += s + ",";
            }
            if (getType(s) == 4) {
                if (this.generatedList.contains(s)) {
                    setStr += outputVector(getVectorReal(s)) + ",";
                } else {
                    setStr += s + ",";
                }
            }
        }
        if (setStr.charAt(setStr.length() - 1) == ',') {
            setStr = setStr.substring(0, setStr.length() - 1);
        }
        return "{" + setStr + "}";
    }

    public String outputVector(Vector v) {
        String vectorStr = "";
        for (String s : v.getSymbols()) {
            if (getType(s) == 1) {
                if (this.generatedList.contains(s)) {
                    vectorStr += outputSet(getSetReal(s)) + ",";
                } else {
                    vectorStr += s + ",";
                }
            }
            if (getType(s) == 2) {
                int realType = getRealType(s);
                if (realType == 4) {
                    if (this.generatedList.contains(s)) {
                        Double var = toValueInt(s);
                        if (var != null) {
                            vectorStr += var + ",";
                        }
                    } else {
                        vectorStr += s + ",";
                    }
                }
                if (realType == 1) {
                    vectorStr += outputSet(getSetReal(s)) + ",";
                }
                if (realType == 2) {
                    if (this.generatedList.contains(s)) {
                        vectorStr += outputVector(getVectorReal(s)) + ",";
                    } else {
                        vectorStr += s + ",";
                    }
                }
                if (realType == 3) {
                    vectorStr += s + ",";
                }
            }
            if (getType(s) == 3) {
                vectorStr += s + ",";
            }
            if (getType(s) == 4) {
                if (this.generatedList.contains(s)) {
                    vectorStr += outputVector(getVectorReal(s)) + ",";
                } else {
                    vectorStr += s + ",";
                }
            }
        }
        if (vectorStr.charAt(vectorStr.length() - 1) == ',') {
            vectorStr = vectorStr.substring(0, vectorStr.length() - 1);
        }
        return "(" + vectorStr + ")";
    }

    public void emptyRollbacks() {
        this.rollbacks = new ArrayList<>();
    }

    public void rollback() {
        for (String r : rollbacks) {
            sets.remove(r);
            variables.remove(r);
            vectors.remove(r);
            functions.remove(r);
        }
    }

    public Double toValueInt(String e) {
        //System.out.println("ToValueInt(): " + e);
        if (e != null) {
            String ee = e.trim();
            Tape t=this.getTapeReal(ee);
            if (ee.matches("^[-+]?(\\d*[.])?\\d+$")) {
                //System.out.println("Numerical");
                return Double.parseDouble(ee);
            } else if(ee.matches("true")){
                return 1D;
            } else if(ee.matches("false")){
                return 0D;
            } else if(t!=null){
                if(t.get()!=null){
                    return Double.parseDouble(t.get());
                }
                return null;
            }
            else {
                //System.out.println(ee);
                return toValueInt(getVar(ee));
            }
        }
        return null;
    }

    public Boolean toValueBoolean(String e) {
        if (e != null) {
            String ee = e.trim();
            //System.out.println(ee);
            if (ee.matches("(true|false)")) {
                return Boolean.valueOf(ee);
            } else {
                return toValueBoolean(getVar(ee));
            }
        }
        return null;
    }

    /*public void output(Main f) {       
     if(!f.getDefinitions().isEmpty()){
     for(String def:f.getDefinitions().keySet()){
     if(!def.equals("otherwise")){
     System.out.println(f.getDefinition()+"="+f.getDefinitions().get(def)+", when "+def);
     }else{
     if(f.getDefinitions().size()==1){
     System.out.println(f.getDefinition()+"="+f.getDefinitions().get(def));
     }else{
     System.out.println(f.getDefinition()+"="+f.getDefinitions().get(def)+", otherwise");
     }
     }
     }
     }
     }

     public String setToString(Set set, String h) {
     String ret = "";
     for (String s : set.getSymbols()) {
     if (getType(s) == 1) {
     ret += setToString(getSet(s), h + " ") + "\n";
     }
     if (getType(s) == 2) {
     ret += h + getVar(s) + "\n";
     }
     if (getType(s) == 3) {
     ret += h + getFunction(s) + "\n";
     }
     }
     System.out.println(ret);
     return ret;
     }

     public void output(Set set, String h) {
     String setStr = "";
     for (String s : set.getSymbols()) {
     if (getType(s) == 1) {
     output(getSetReal(s), h + " ");
     }
     if (getType(s) == 2) {
     Set sett = getSetReal(s);
     Vector vector = getVectorReal(s);
     Main f = getFunctionReal(s);
     if (sett == null && vector == null && f == null) {
     System.out.println(h+toValueInt(s));
     }
     if (sett != null) {
     output(sett, h + " ");
     }
     if (vector != null) {
     output(vector, h + " ");
     }
     if (f != null) {
     System.out.println(h+s);
     }
               
     }
     if (getType(s) == 3) {
     System.out.println(h + s);
     }
     }
     }

     public void output(Vector v, String h) {
     String vectorStr = "";
     for (String s : v.getSymbols()) {
     if (getType(s) == 1) {
     output(getVectorReal(s), h + " ");
     }
     if (getType(s) == 2) {
     Set sett = getSetReal(s);
     Vector vector = getVectorReal(s);
     Main f = getFunctionReal(s);
     if (sett == null && vector == null && f == null) {
     System.out.println(h+toValueInt(s));
     }
     if (sett != null) {
     output(sett, h + " ");
     }
     if (vector != null) {
     output(vector, h + " ");
     }
     if (f != null) {
     System.out.println(h+v);
     }
               
     }
     if (getType(s) == 3) {
     System.out.println(h + s);
     }
     }
     }
     */
    public boolean exists(String input) {
        String k = input;
        if (input.matches("^[0-9]*[a-zA-Z]+[a-zA-Z\\-0-9]*[\\[]{1}[^\\[\\]]+[\\]]{1}$")) {
            String[] splittedSymbol = input.split("[\\[]{1}", 2);
            k = splittedSymbol[0];
        }
        if (sets.containsKey(k)) {
            return true;
        }
        if (variables.containsKey(k)) {
            return true;
        }
        if (functions.containsKey(k)) {
            return true;
        }
        if (vectors.containsKey(k)) {
            return true;
        }
        return false;
    }
    
    public boolean isSubSetOf(Set set1, Set set2){
        Set s=intersection(set1, set2);
        boolean b1=isSameSet(s, set2);
        boolean b2=isSameSet(set2, s);
        return b1&&b2;
    }

    public Set union(Set set1, Set set2) {
        Set s = new Set();
        ArrayList<String> symbols = new ArrayList<>();
        symbols.addAll(set1.getSymbols());
        symbols.addAll(set2.getSymbols());
        s.setSymbols(symbols);
        return s;
    }

    public Set not(Set set1, Set set2) {
        Set s = new Set();
        for (String e1 : set1.getSymbols()) {
            int i = 0;
            for (String e2 : set2.getSymbols()) {
                if (getType(e1) == 2 && getType(e2) == 2) {
                    if (getVar(e1).equals(getVar(e2))) {
                        i++;
                    }
                }
                if (getType(e1) == 1 && getType(e2) == 1) {
                    if (isSameSet(getSetReal(e1), getSetReal(e2))) {
                        i++;
                    }
                }
            }
            if (i == 0) {
                if (getType(e1) == 2) {
                    UUID uuid = UUID.randomUUID();
                    this.addVar(uuid.toString(), getVar(e1), true);
                    s.add(uuid.toString());
                }
                if (getType(e1) == 1) {
                    UUID uuid = UUID.randomUUID();
                    this.addSet(uuid.toString(), getSetReal(e1), true);
                    s.add(uuid.toString());
                }
            }
        }
        return s;
    }

    public Set intersection(Set set1, Set set2) {
        Set s = new Set();
        for (String e1 : set1.getSymbols()) {
            for (String e2 : set2.getSymbols()) {
                if (getType(e1) == 2 && getType(e2) == 2) {
                    if (getVar(e1).equals(getVar(e2))) {
                        UUID uuid = UUID.randomUUID();
                        this.addVar(uuid.toString(), getVar(e1), true);
                        s.add(uuid.toString());
                    }
                }
                if (getType(e1) == 1 && getType(e2) == 1) {
                    if (isSameSet(getSetReal(e1), getSetReal(e2)) && isSameSet(getSetReal(e2), getSetReal(e1))) {
                        UUID uuid = UUID.randomUUID();
                        this.addSet(uuid.toString(), getSetReal(e1), true);
                        s.add(uuid.toString());
                    }
                }
            }
        }
        return s;
    }

    public boolean isSameSet(Set set1, Set set2) {
        int in = 0;
        for (String e1 : set1.getSymbols()) {
            for (String e2 : set2.getSymbols()) {
                if (getType(e1) == 2 && getType(e2) == 2) {
                    if (getVar(e1).equals(getVar(e2))) {
                        in++;
                    }
                }
                if (getType(e1) == 1 && getType(e2) == 1) {
                    if (isSameSet(getSetReal(e1), getSetReal(e2)) && isSameSet(getSetReal(e2), getSetReal(e1))) {
                        in++;
                    }
                }
            }
        }
        if (in == set1.getSymbols().size()) {
            return true;
        }
        return false;
    }

    public int getType(String k) {
        if (sets.containsKey(k)) {
            return 1;
        }
        if (variables.containsKey(k)) {
            return 2;
        }
        if (functions.containsKey(k)) {
            return 3;
        }
        if (vectors.containsKey(k)) {
            return 4;
        }
        if (tapes.containsKey(k)) {
            return 5;
        }
        return 0;
    }

    public Function getFunctionReal(String k) {
        String ff = k;
        int t = getType(ff);
        while (t == 2) {
            ff = getVar(ff);
            t = getType(ff);
        }
        return this.functions.get(ff);
    }

    public String getVar(String k) {
        return this.variables.get(k);
    }

    public Set getSetReal(String k) {
        String ff = k;
        int t = getType(ff);
        while (t == 2) {
            ff = getVar(ff);
            t = getType(ff);
        }
        return this.sets.get(ff);
    }

    public Vector getVectorReal(String k) {
        String ff = k;
        int t = getType(ff);
        while (t == 2) {
            ff = getVar(ff);
            t = getType(ff);
        }
        return this.vectors.get(ff);
    }
    
    public Tape getTapeReal(String k) {
        String ff = k;
        int t = getType(ff);
        while (t == 2) {
            ff = getVar(ff);
            t = getType(ff);
        }
        return this.tapes.get(ff);
    }

    //MUISTA POISTOT
    public String addSet(String k, Set s, boolean generated) {
        if (generated) {
            generatedList.add(k);
        }
        rollbacks.add(k);
        this.sets.put(k, s);
        this.tapes.remove(k);
        this.variables.remove(k);
        this.functions.remove(k);
        this.vectors.remove(k);
        return k;
    }

    //MUISTA POISTOT
    public String addVar(String k, String v, boolean generated) {
        if (generated) {
            generatedList.add(k);
        }
        rollbacks.add(k);
        this.variables.put(k, v);
        this.sets.remove(k);
        this.tapes.remove(k);
        this.functions.remove(k);
        this.vectors.remove(k);
        return k;
    }

    //MUISTA POISTOT
    public String addFunction(String k, Function f, boolean generated) {
        if (generated) {
            generatedList.add(k);
        }
        rollbacks.add(k);
        this.functions.put(k, f);
        this.sets.remove(k);
        this.tapes.remove(k);
        this.variables.remove(k);
        this.vectors.remove(k);
        return k;
    }

    //MUISTA POISTOT    
    public String addVector(String k, Vector v, boolean generated) {
        if (generated) {
            generatedList.add(k);
        }
        rollbacks.add(k);
        this.vectors.put(k, v);
        this.sets.remove(k);
        this.functions.remove(k);
        this.tapes.remove(k);
        this.variables.remove(k);
        return k;
    }
    
    public String addTape(String k, Tape v, boolean generated) {
        if (generated) {
            generatedList.add(k);
        }
        rollbacks.add(k);
        this.tapes.put(k, v);
        this.sets.remove(k);
        this.functions.remove(k);
        this.vectors.remove(k);
        this.variables.remove(k);
        return k;
    }

    public HashMap<String, Set> getSets() {
        return sets;
    }

    public HashMap<String, String> getVariables() {
        return variables;
    }

    public HashMap<String, Vector> getVectors() {
        return vectors;
    }

    public HashMap<String, Function> getFunctions() {
        return functions;
    }

    public HashMap<String, Tape> getTapes() {
        return tapes;
    }

}
