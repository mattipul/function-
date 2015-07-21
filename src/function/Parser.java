package function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    interface IStdFunction {

        public String eval(String[] params);
    }

    class FElement implements IStdFunction {

        @Override
        public String eval(String[] params) {
            if (params.length == 2) {
                String p1 = params[0];
                String p2 = params[1];
                if (p1 != null && p2 != null) {
                    UUID u1 = UUID.randomUUID();
                    UUID u2 = UUID.randomUUID();
                    evalStatement(u1.toString(), inside(p1));
                    evalStatement(u2.toString(), inside(p2));
                    Set s1 = FunctionConfig.database.getSetReal(u1.toString());
                    Set s2 = new Set();
                    s2.add(p2);
                    if (s1 != null && s2 != null) {
                        boolean b = FunctionConfig.database.isSubSetOf(s1, s2);
                        UUID u = UUID.randomUUID();
                        FunctionConfig.database.addVar(u.toString(), "" + b, true);
                        return u.toString();
                    }
                }
            }
            return null;
        }

    }

    class FSubset implements IStdFunction {

        @Override
        public String eval(String[] params) {
            if (params.length == 2) {
                String p1 = params[0];
                String p2 = params[1];
                if (p1 != null && p2 != null) {
                    UUID u1 = UUID.randomUUID();
                    UUID u2 = UUID.randomUUID();
                    evalStatement(u1.toString(), inside(p1));
                    evalStatement(u2.toString(), inside(p2));
                    Set s1 = FunctionConfig.database.getSetReal(u1.toString());
                    Set s2 = FunctionConfig.database.getSetReal(u2.toString());
                    if (s1 != null && s2 != null) {
                        boolean b = FunctionConfig.database.isSubSetOf(s1, s2);
                        UUID u = UUID.randomUUID();
                        FunctionConfig.database.addVar(u.toString(), "" + b, true);
                        return u.toString();
                    }
                }
            }
            return null;
        }

    }

    class FTape implements IStdFunction {

        @Override
        public String eval(String[] params) {
            if (params.length == 0) {
                UUID u1 = UUID.randomUUID();
                Tape t = new Tape();
                FunctionConfig.database.addTape(u1.toString(), t, true);
                return u1.toString();
            }
            return null;
        }

    }

    class FUnion implements IStdFunction {

        @Override
        public String eval(String[] params) {
            if (params.length == 2) {
                String p1 = params[0];
                String p2 = params[1];
                if (p1 != null && p2 != null) {
                    UUID u1 = UUID.randomUUID();
                    UUID u2 = UUID.randomUUID();
                    evalStatement(u1.toString(), inside(p1));
                    evalStatement(u2.toString(), inside(p2));
                    Set s1 = FunctionConfig.database.getSetReal(u1.toString());
                    Set s2 = FunctionConfig.database.getSetReal(u2.toString());
                    if (s1 != null && s2 != null) {
                        Set sR = FunctionConfig.database.union(s1, s2);
                        UUID u = UUID.randomUUID();
                        FunctionConfig.database.addSet(u.toString(), sR, true);
                        return u.toString();
                    }
                }
            }
            return null;
        }

    }

    class FIntersection implements IStdFunction {

        @Override
        public String eval(String[] params) {
            if (params.length == 2) {
                String p1 = params[0];
                String p2 = params[1];
                if (p1 != null && p2 != null) {
                    UUID u1 = UUID.randomUUID();
                    UUID u2 = UUID.randomUUID();
                    evalStatement(u1.toString(), inside(p1));
                    evalStatement(u2.toString(), inside(p2));
                    Set s1 = FunctionConfig.database.getSetReal(u1.toString());
                    Set s2 = FunctionConfig.database.getSetReal(u2.toString());
                    if (s1 != null && s2 != null) {
                        Set sR = FunctionConfig.database.intersection(s1, s2);
                        UUID u = UUID.randomUUID();
                        FunctionConfig.database.addSet(u.toString(), sR, true);
                        return u.toString();
                    }
                }
            }
            return null;
        }

    }

    private HashMap<String, IStdFunction> stdLib;
    private boolean hasErrors;
    private ArrayList<String> errors;
    private String stm;

    public Parser() {
        this.errors = new ArrayList<>();
        this.stdLib = new HashMap<>();
        this.stdLib.put("union", new FUnion());
        this.stdLib.put("intersection", new FIntersection());
        this.stdLib.put("tape", new FTape());
        this.stdLib.put("isSubsetOf", new FSubset());
        this.stdLib.put("isElementtOf", new FElement());
    }

    public static Iterable<MatchResult> allMatches(
            final Pattern p, final CharSequence input) {
        return new Iterable<MatchResult>() {
            public Iterator<MatchResult> iterator() {
                return new Iterator<MatchResult>() {
                    // Use a matcher internally.
                    final Matcher matcher = p.matcher(input);
                    // Keep a match around that supports any interleaving of hasNext/next calls.
                    MatchResult pending;

                    public boolean hasNext() {
                        // Lazily fill pending, and avoid calling find() multiple times if the
                        // clients call hasNext() repeatedly before sampling via next().
                        if (pending == null && matcher.find()) {
                            pending = matcher.toMatchResult();
                        }
                        return pending != null;
                    }

                    public MatchResult next() {
                        // Fill pending if necessary (as when clients call next() without
                        // checking hasNext()), throw if not possible.
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        // Consume pending so next call to hasNext() does a find().
                        MatchResult next = pending;
                        pending = null;
                        return next;
                    }

                    /**
                     * Required to satisfy the interface, but unsupported.
                     */
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    private String[] splitWithDelimiters(String str, String regex) {
        List<String> parts = new ArrayList<String>();

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);

        int lastEnd = 0;
        while (m.find()) {
            int start = m.start();
            if (lastEnd != start) {
                String nonDelim = str.substring(lastEnd, start);
                parts.add(nonDelim);
            }
            String delim = m.group();
            parts.add(delim);

            int end = m.end();
            lastEnd = end;
        }

        if (lastEnd != str.length()) {
            String nonDelim = str.substring(lastEnd);
            parts.add(nonDelim);
        }

        String[] res = parts.toArray(new String[]{});
        //////System.out.println("result: " + Arrays.toString(res));

        return res;
    }

    private String inside(String input) {
        return "(" + input + ")";
    }

    private void addError(String error) {
        errors.add(error);
    }

    private int statementType(String input) {
        Pattern pattern1 = Pattern.compile("[#%\\\\]+");
        Matcher matcher1 = pattern1.matcher(input);

        Pattern pattern2 = Pattern.compile("(\\+|\\*|/|--)+");
        Matcher matcher2 = pattern2.matcher(input);

        Pattern pattern3 = Pattern.compile("[\\|=!<>&]+");
        Matcher matcher3 = pattern3.matcher(input);

        boolean b1 = matcher1.find();
        boolean b2 = matcher2.find();
        boolean b3 = matcher3.find();

        if (b1 && !b2 && !b3) {
            return 1;
        }
        if (!b1 && b2 && !b3) {
            return 2;
        }
        if (!b1 && !b2 && b3) {
            return 3;
        }
        return -1;
    }

    public int singleStatementType(String input) {
        if (input.matches("^[{]{1}.*[}]{1}$")) {
            return 1;
        } else if (input.matches("^[0-9]*[a-zA-Z]+[a-zA-Z\\-0-9]*[\\[]{1}[^\\[\\]]*[\\]]{1}$")) {
            return 2;
        } else if (input.contains(",")) {
            return 4;
        } else {
            return 3;
        }
    }

    private Set evalSetDef(String input) {
        String[] splitted = this.splitWithDelimiters(input, "[#%\\\\]{1}");
        if (splitted.length > 1) {
            UUID setUUID = UUID.randomUUID();
            evalStatement(setUUID.toString(), inside(splitted[0]));
            Set tal = FunctionConfig.database.getSetReal(setUUID.toString());
            for (int i = 1; i < splitted.length; i += 2) {
                UUID talUUID = UUID.randomUUID();
                if (splitted[i].equals("#")) {
                    evalStatement(talUUID.toString(), inside(splitted[i + 1]));
                    Set t = FunctionConfig.database.getSetReal(talUUID.toString());
                    tal = FunctionConfig.database.intersection(tal, t);
                } else if (splitted[i].equals("%")) {
                    evalStatement(talUUID.toString(), inside(splitted[i + 1]));
                    Set t = FunctionConfig.database.getSetReal(talUUID.toString());
                    tal = FunctionConfig.database.union(tal, t);
                } else if (splitted[i].equals("\\")) {
                    evalStatement(talUUID.toString(), inside(splitted[i + 1]));
                    Set t = FunctionConfig.database.getSetReal(talUUID.toString());
                    tal = FunctionConfig.database.not(tal, t);
                }
            }
            return tal;
        } else {
            //////System.out.println("EvalSetDef(): No operations");
            UUID setUUID = UUID.randomUUID();
            evalStatement(setUUID.toString(), inside(input));
            return FunctionConfig.database.getSets().get(setUUID.toString());
        }
    }

    public void evalSet(String uuid, String statement) {
        Set set = evalSetDef(statement);
        if (set == null) {
            addError("Set operation returns null.");
            hasErrors = true;
        }
        FunctionConfig.database.addSet(uuid.toString(), set, true);
    }

    private String evalValueDef(String input) {
        ////System.out.println(input);
        String[] splitted = this.splitWithDelimiters(input, "(\\+|\\*|/|--)");
        if (splitted.length > 2) {
            Double tal = 0d;
            if (splitted[0].matches("-?\\d+")) {
                tal = FunctionConfig.database.toValueInt(splitted[0]);
                if (tal == null) {
                    addError("Value is not numeric.");
                    hasErrors = true;
                }
            } else {
                ////System.out.println("Exists: " + database.exists(splitted[0]));
                if (FunctionConfig.database.exists(splitted[0])) {
                    UUID u = UUID.randomUUID();
                    evalStatement(u.toString(), inside(splitted[0]));
                    tal = FunctionConfig.database.toValueInt(u.toString());
                } else {
                    return null;
                }
            }
            for (int i = 1; i < splitted.length; i += 2) {
                Double e = 0d;
                if (splitted[i + 1].matches("-?\\d+")) {
                    e = FunctionConfig.database.toValueInt(splitted[i + 1]);
                    if (e == null) {
                        addError("Value is not numeric.");
                        hasErrors = true;
                    }
                } else {
                    if (FunctionConfig.database.exists(splitted[i + 1])) {
                        UUID u = UUID.randomUUID();
                        evalStatement(u.toString(), inside(splitted[i + 1]));
                        e = FunctionConfig.database.toValueInt(u.toString());
                    } else {
                        return null;
                    }
                }
                if (splitted[i].equals("+")) {
                    tal = tal + e;
                }
                if (splitted[i].equals("--")) {
                    tal = tal - e;
                }
                if (splitted[i].equals("*")) {
                    tal = tal * e;
                }
                if (splitted[i].equals("/")) {
                    tal = tal / e;
                }
            }
            //System.out.println("Value operation result: " + tal);
            return "" + tal;
        } else if (splitted.length == 1) {
            return "" + FunctionConfig.database.toValueInt(input);
        }
        return null;
    }

    public void evalValue(String uuid, String statement) {
        String value = this.evalValueDef(statement);
        if (value == null) {
            addError("Value operation returns null.");
            hasErrors = true;
        }
        FunctionConfig.database.addVar(uuid.toString(), value, true);
    }

    private boolean toBoolean(String input) {
        String[] splitted = this.splitWithDelimiters(input, "(==|!=|<=|>=|<|>)");
        if (splitted.length == 3) {
            Double e1 = FunctionConfig.database.toValueInt(splitted[0]);
            Double e2 = FunctionConfig.database.toValueInt(splitted[2]);
            if (e1 == null || e2 == null) {
                addError("Value in boolean operation is not numeric.");
                hasErrors = true;
            }
            String d = splitted[1];
            if (d.equals("==")) {
                return e1 == e2;
            }
            if (d.equals("!=")) {
                return e1 != e2;
            }
            if (d.equals("<=")) {
                return e1 <= e2;
            }
            if (d.equals(">=")) {
                return e1 >= e2;
            }
            if (d.equals("<")) {
                return e1 < e2;
            }
            if (d.equals(">")) {
                return e1 > e2;
            }
        }
        if (splitted.length == 1) {
            return FunctionConfig.database.toValueBoolean(input);
        }
        return false;
    }

    private String evalBooleanDef(String input) {
        String[] splitted = this.splitWithDelimiters(input, "(&&|\\|\\|)");
        if (splitted.length > 1) {
            boolean tal = toBoolean(String.valueOf(splitted[0]));
            for (int i = 1; i < splitted.length; i += 2) {
                if (splitted[i].equals("&&")) {
                    tal = toBoolean(String.valueOf(tal)) && toBoolean(splitted[i + 1]);
                } else if (splitted[i].equals("||")) {
                    tal = toBoolean(String.valueOf(tal)) || toBoolean(splitted[i + 1]);
                }
            }
            return String.valueOf(tal);
        } else {
            return String.valueOf(toBoolean("" + splitted[0]));
        }
    }

    public void evalBoolean(String uuid, String statement) {
        ////System.out.println("BOOLEAN OPERATION");
        String bool = this.evalBooleanDef(statement);
        if (bool == null) {
            addError("Boolean operation returns null.");
            hasErrors = true;
        }
        FunctionConfig.database.addVar(uuid.toString(), bool, true);
    }

    private Set constructSet(String input) {
        String nInput = input;
        String[] splitted = nInput.split(",");
        Set mSetT = new Set();
        if (splitted.length == 0) {
            splitted = new String[]{nInput};
        }
        for (String s : splitted) {
            //////System.out.println("Set element: " + s);
            if (s.matches("-?\\d+")) {
                UUID setUUID = UUID.randomUUID();
                FunctionConfig.database.addVar(setUUID.toString(), s, true);
                mSetT.add(setUUID.toString());
            } else if (s.matches("[0-9]*[a-zA-Z]+[a-zA-Z\\-0-9]*[\\[]{1}[^\\[\\]]+[\\]]{1}")) {
                UUID setUUID = UUID.randomUUID();
                FunctionConfig.database.addVar(setUUID.toString(), s, true);
                mSetT.add(setUUID.toString());
            } else {
                mSetT.add(s);
            }
        }
        return mSetT;
    }

    private void parseSet(String uuid, String input) {
        for (MatchResult match : allMatches(Pattern.compile("[{]{1}[^{}]+[}]{1}"), input)) {
            String set = match.group();
            String setNoBrackets = set.substring(1, set.length() - 1);
            int s = match.start();
            Set mSet = this.constructSet(setNoBrackets);
            UUID setUUID = UUID.randomUUID();
            FunctionConfig.database.addSet(setUUID.toString(), mSet, true);
            if (s + set.length() < input.length()) {
                String newSet = input.substring(0, s);
                newSet += setUUID + input.substring(s + set.length(), input.length());
                parseSet(uuid, newSet);
            } else {
                parseSet(uuid, setUUID.toString());
            }
        }
        if (!input.matches("^[{]{1}.+[}]{1}$")) {
            HashMap<String, Set> sets = FunctionConfig.database.getSets();
            if (sets.containsKey(input)) {
                FunctionConfig.database.addSet(uuid, sets.get(input), true);
            }
        }
    }

    private void addSet(String uuid, String statement) {
        //System.out.println("SET");
        UUID u = UUID.randomUUID();
        parseSet(u.toString(), statement);
        Set set = FunctionConfig.database.getSetReal(u.toString());
        if (set == null) {
            addError("Set construction FAILED.");
            hasErrors = true;
        }
        FunctionConfig.database.addSet(uuid.toString(), set, true);
    }

    private void addFunction(String uuid, String statement) {
        //System.out.println("FUNCTION VALUE");
        String[] splitted = statement.split("[\\[]{1}");
        String paramsStr = splitted[1].substring(0, splitted[1].length() - 1);
        String[] params = paramsStr.split(",");
        if (params.length == 0) {
            params = new String[]{paramsStr};
        }
        /*for (int i = 0; i < params.length; i++) {
         params[i] = params[i];
         }*/
        if (paramsStr.length() == 0) {
            params = new String[]{};
        }
        String fvalue = this.functionValue(splitted[0], params);
        //System.out.println("Function value: " + fvalue);
        if (fvalue == null) {
            addError("Function returns null.");
            hasErrors = true;
        }
        FunctionConfig.database.addVar(uuid.toString(), fvalue, true);
    }

    private void addSymbol(String uuid, String statement) {
        //System.out.println("SYMBOL");
        if (!statement.matches("-?\\d+")) {
            if (!FunctionConfig.database.exists(statement)) {
                addError("Statement: " + statement);
                addError("Pointing to a non-existent symbol: " + statement);
                hasErrors = true;
            }
        }
        FunctionConfig.database.addVar(uuid.toString(), statement, true);
    }

    private Vector constructVector(String uuid, String input) {
        String nInput = input;
        String[] splitted = nInput.split(",");
        Vector mVectorT = new Vector();
        if (splitted.length == 0) {
            splitted = new String[]{nInput};
        }
        for (String s : splitted) {
            //////System.out.println("Vector element: " + s);
            if (s.matches("-?\\d+")) {
                UUID setUUID = UUID.randomUUID();
                FunctionConfig.database.addVar(setUUID.toString(), s, true);
                mVectorT.add(setUUID.toString());
            } else if (s.matches("[0-9]*[a-zA-Z]+[a-zA-Z\\-0-9]*[\\[]{1}[^\\[\\]]+[\\]]{1}")) {
                UUID setUUID = UUID.randomUUID();
                FunctionConfig.database.addVar(setUUID.toString(), s, true);
                mVectorT.add(setUUID.toString());
            } else {
                mVectorT.add(s);
            }
        }
        return mVectorT;
    }

    private void addVector(String uuid, String statement) {
        //System.out.println("VECTOR");
        Vector v = constructVector(uuid.toString(), statement);
        if (v == null) {
            addError("Vector construction FAILED.");
            hasErrors = true;
        }
        FunctionConfig.database.addVector(uuid.toString(), v, true);
    }

    private void pointToSet(String key, String uuid) {
        if (FunctionConfig.database.getSets().containsKey(uuid)) {
            ////System.out.println("Saving a set");
            Set s = FunctionConfig.database.getSetReal(uuid);
            if (s == null) {
                addError("Set construction FAILED.");
                hasErrors = true;
                //////System.out.println("Set is null");
            }
            FunctionConfig.database.addSet(key, s, false);
        }
    }

    private void pointToFunction(String key, String uuid) {
        if (FunctionConfig.database.getFunctions().containsKey(uuid)) {
            ////System.out.println("Saving a function: " + input);
            Function f = FunctionConfig.database.getFunctions().get(uuid);
            if (f == null) {
                hasErrors = true;
            }
            FunctionConfig.database.addFunction(key, f, false);
        }
    }

    private void pointToVariable(String key, String uuid) {
        if (FunctionConfig.database.getVariables().containsKey(uuid)) {
            ////System.out.println("Saving a variable: " + input);
            FunctionConfig.database.addVar(key, uuid, false);
        }
    }

    private void pointToVector(String key, String uuid) {
        if (FunctionConfig.database.getVectors().containsKey(uuid)) {
            ////System.out.println("Saving a vector");
            Vector v = FunctionConfig.database.getVectorReal(uuid);
            if (v == null) {
                hasErrors = true;
                //////System.out.println("Vector is null");
            }
            FunctionConfig.database.addVector(key, v, false);
        }
    }

    private void evalStatement(String key, String input) {
        //System.out.println("Eval statement: " + input);
        for (MatchResult match : allMatches(Pattern.compile("[(]{1}[^()]+[)]{1}"), input)) {
            String statement = match.group();
            String statementNoBrackets = statement.substring(1, statement.length() - 1);
            if (statementNoBrackets.startsWith("-")) {
                statementNoBrackets = "0" + statementNoBrackets;
            }
            //System.out.println("Substatement: " + statementNoBrackets);
            int s = match.start();
            int statementType = statementType(statementNoBrackets);
            //System.out.println("Statement type: " + statementType);
            UUID uuid = UUID.randomUUID();
            //System.out.println("To be saved: " + uuid.toString());
            if (statementType == 1) {
                evalSet(uuid.toString(), statementNoBrackets);
            }
            if (statementType == 2) {
                evalValue(uuid.toString(), statementNoBrackets);
            }
            if (statementType == 3) {
                evalBoolean(uuid.toString(), statementNoBrackets);
            }
            if (statementType == -1) {
                //System.out.println("SINGLE");
                int singleStatementType = singleStatementType(statementNoBrackets);
                if (singleStatementType == 1) {
                    addSet(uuid.toString(), statementNoBrackets);
                }
                if (singleStatementType == 2) {
                    //System.out.println("FUNCTION VALUE");
                    addFunction(uuid.toString(), statementNoBrackets);
                }
                if (singleStatementType == 3) {
                    //System.out.println("SYMBOL");
                    addSymbol(uuid.toString(), statementNoBrackets);
                }
                if (singleStatementType == 4) {
                    //System.out.println("VECTOR");
                    addVector(uuid.toString(), statementNoBrackets);
                }
            }
            if (s + statement.length() < input.length()) {
                String newStatement = input.substring(0, s);
                newStatement += uuid + input.substring(s + statement.length(), input.length());
                evalStatement(key, newStatement);
            } else {
                evalStatement(key, uuid.toString());
            }
        }
        if (!input.matches("^[(]{1}.+[)]{1}$")) {

            if (input.matches("-?\\d+")) {

            } else {
                int elementType = FunctionConfig.database.getType(input);
                if (elementType == 1) {
                    if (FunctionConfig.database.getSets().containsKey(input)) {
                        //System.out.println("Saving a set");
                        pointToSet(key, input);
                    }
                }
                if (elementType == 2) {
                    if (FunctionConfig.database.getVariables().containsKey(input)) {
                        //System.out.println("Saving a variable: " + input + " " + key);
                        pointToVariable(key, input);
                    }
                }
                if (elementType == 3) {
                    if (FunctionConfig.database.getFunctions().containsKey(input)) {
                        //System.out.println("Saving a function: " + input);
                        pointToFunction(key, input);
                    }
                }
                if (elementType == 4) {
                    if (FunctionConfig.database.getVectors().containsKey(input)) {
                        //System.out.println("Saving a vector");
                        pointToVector(key, input);
                    }
                }

            }
        }
    }

    public boolean isTapeAdd(String input) {
        if (input.matches("^[0-9]*[a-zA-Z]+[a-zA-Z\\-0-9]*(<-){1}.+$")) {
            return true;
        }
        return false;
    }

    public boolean isTapeRight(String input) {
        if (input.matches("^[0-9]*[a-zA-Z]+[a-zA-Z\\-0-9]*(>>){1}$")) {
            return true;
        }
        return false;
    }

    public boolean isTapeLeft(String input) {
        if (input.matches("^[0-9]*[a-zA-Z]+[a-zA-Z\\-0-9]*(<<){1}$")) {
            return true;
        }
        return false;
    }

    public boolean isDefinition(String input) {
        if (input.matches("^[a-zA-Z]+[0-9]*[=]{1}.+$")) {
            return true;
        }
        return false;
    }

    public boolean isFunctionDefinition(String input) {
        if (input.matches("^[0-9]*[a-zA-Z]+[a-zA-Z\\-0-9]*[\\(]{1}[^\\[\\]]+[\\)]{1}[=]{1}(.+[|]*)+$")) {
            return true;
        }
        return false;
    }

    private void printErrors() {
        for (String e : errors) {
            System.out.println("ERROR: " + e);
        }
    }

    public void handleFunctions(String input) {
        stm = input;
        for (MatchResult match : allMatches(Pattern.compile("[0-9]*[a-zA-Z]+[a-zA-Z\\-0-9]*[\\(]{1}"), input)) {
            String statement = match.group();
            int s = match.start();
            ////System.out.println(statement);
            replaceInFunctionDef(s, statement.length());
        }
    }

    private void replaceInFunctionDef(int from, int to) {
        char[] inputChars = stm.toCharArray();
        Stack s = new Stack();
        boolean first = false;
        for (int i = from; i < inputChars.length; i++) {
            char c = inputChars[i];
            if (c == '(') {
                if (first == false) {
                    inputChars[i] = '[';
                    first = true;
                }
                s.push(c);
            }
            if (c == ')') {
                if (!s.empty()) {
                    s.pop();
                    if (s.empty()) {
                        inputChars[i] = ']';
                        stm = String.valueOf(inputChars);
                        return;
                    }
                }
            }
        }
    }

    public String functionValue(String f, String[] params) {
        //System.out.println("Evaluating function: " + f + " " + params.length);
        if (this.stdLib.containsKey(f)) {
            String ret = this.stdLib.get(f).eval(params);
            //System.out.println("Using standard library: " + f);
            return ret;
        }
        int ci = 0;
        for (String p : params) {
            UUID u = UUID.randomUUID();
            evalStatement(u.toString(), "(" + p + ")");
            params[ci] = u.toString();
            ci++;
        }
        Function func = FunctionConfig.database.getFunctionReal(f);
        if (func != null) {
            java.util.Set<String> str = func.getDefinitions().keySet();
            String correctDef = "";
            for (String s : str) {
                if (!s.equals("otherwise")) {
                    String d = s;
                    int c = 0;
                    ////System.out.println(s);
                    for (String j : func.getParameters()) {
                        UUID u1 = UUID.randomUUID();
                        ////System.out.println("Function parameter " + c + ": " + params[c]);
                        FunctionConfig.database.addVar(u1.toString(), params[c], true);
                        //////System.out.println("Testing database variable: " + this.database.toValueInt(u1.toString()));
                        d = d.replaceAll("(" + j + "){1}", u1.toString());
                        c++;
                    }
                    UUID u = UUID.randomUUID();
                    ////System.out.println(d);
                    evalStatement(u.toString(), "(" + d + ")");
                    //ONGELMA?
                    String b = u.toString();
                    ////System.out.println("Evaluated boolean: " + database.toValueBoolean(b));
                    if (FunctionConfig.database.toValueBoolean(b) == true) {
                        correctDef = func.getDefinitions().get(s);
                    }
                }
            }
            if (correctDef.length() > 0) {
                int c = 0;
                for (String j : func.getParameters()) {
                    UUID u1 = UUID.randomUUID();
                    FunctionConfig.database.addVar(u1.toString(), params[c], true);
                    correctDef = correctDef.replaceAll("(" + j + "){1}", u1.toString());
                    c++;
                }
                UUID u = UUID.randomUUID();
                evalStatement(u.toString(), "(" + correctDef + ")");
                String b = u.toString();
                return b;
            } else {
                correctDef = func.getDefinitions().get("otherwise");
                int c = 0;
                for (String j : func.getParameters()) {
                    ////System.out.println("Parameter: " + j);
                    UUID u1 = UUID.randomUUID();
                    FunctionConfig.database.addVar(u1.toString(), params[c], true);
                    correctDef = correctDef.replaceAll("(" + j + "){1}", u1.toString());
                    c++;
                }
                UUID u = UUID.randomUUID();
                evalStatement(u.toString(), "(" + correctDef + ")");
                String b = u.toString();
                ////System.out.println(b);
                return b;
            }
        } else {
            Vector v = FunctionConfig.database.getVectorReal(f);
            if (v != null) {
                //////System.out.println("Getting an element of a vector");
                if (params.length == 1) {
                    if (FunctionConfig.database.getRealType(params[0])==4) {
                        Double pv = FunctionConfig.database.toValueInt(params[0]);
                        if (v.getSymbols().size() > pv) {
                            String element = v.getSymbols().get(FunctionConfig.database.toValueInt(params[0]).intValue());
                            //////System.out.println("Vector element: " + element);
                            return element;
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean eval(String input) {
        hasErrors = false;
        errors = new ArrayList<>();
        FunctionConfig.database.emptyRollbacks();
        if (input.startsWith("?- ")) {
            handleFunctions(input);
            String statement = inside(stm.substring(3).replaceAll("-", "--"));
            UUID u = UUID.randomUUID();
            evalStatement(u.toString(), statement);
            if (!hasErrors) {
                int realType = FunctionConfig.database.getRealType(u.toString());
                if (realType == 1) {
                    System.out.println(FunctionConfig.database.outputSet(FunctionConfig.database.getSetReal(u.toString())));
                }
                if (realType == 2) {
                    System.out.println(FunctionConfig.database.outputVector(FunctionConfig.database.getVectorReal(u.toString())));
                }
                if (realType == 3) {
                    System.out.println(FunctionConfig.database.outputFunction(FunctionConfig.database.getFunctionReal(u.toString())));
                }
                if (realType == 4 || realType == 5) {
                    System.out.println(FunctionConfig.database.toValueInt(u.toString()));
                }
                return true;
            } else {
                this.printErrors();
            }
            return false;
        }
        if (isDefinition(input)) {
            String[] splitted = input.replaceAll("-", "--").split("=");
            handleFunctions(splitted[1]);
            String statement = inside(stm);
            evalStatement(splitted[0], statement);
            if (hasErrors) {
                this.printErrors();
                FunctionConfig.database.rollback();
                return false;
            }
            return true;
        }
        if (isFunctionDefinition(input)) {
            //////System.out.println("IsFunctionDefinition=true");
            String[] splittedDef = input.replaceAll("-", "--").split("=", 2);
            String[] splittedSymbol = splittedDef[0].split("[\\(]{1}", 2);
            String parameters = splittedSymbol[1].substring(0, splittedSymbol[1].length() - 1);
            Function f = new Function();
            f.setDesc(input);
            FunctionConfig.database.addFunction(splittedSymbol[0], f, false);
            if (f != null) {
                f.setDefinition(splittedDef[0]);
                f.setParameters(Arrays.asList(parameters.split(",")));
                f.setDefinitions(new HashMap<String, String>());
                //////System.out.println("Function parameter count: " + f.getParameters().size());
                String[] defs = splittedDef[1].split("[|]");
                if (defs.length > 1) {
                    for (String def : defs) {
                        //////System.out.println(":" + def);
                        String[] defSingle = def.split("[;]");
                        if (defSingle.length == 2) {
                            f.addDefinition(defSingle[1], defSingle[0]);
                        } else {
                            f.addDefinition("otherwise", def);
                        }
                    }
                } else {
                    String[] defSingle = splittedDef[1].split("[;]");
                    if (defSingle.length == 2) {
                        f.addDefinition(defSingle[1], defSingle[0]);
                    } else {
                        f.addDefinition("otherwise", splittedDef[1]);
                    }
                }
            }

            return true;
        }
        if (isTapeAdd(input)) {
            String[] splittedDef = input.split("(<-){1}", 2);
            Tape t = FunctionConfig.database.getTapeReal(splittedDef[0]);
            if (t != null) {
                t.set(splittedDef[1]);
                return true;
            } else {
                return false;
            }
        }
        if (isTapeRight(input)) {
            String[] splittedDef = input.split("(>>){1}", 2);
            Tape t = FunctionConfig.database.getTapeReal(splittedDef[0]);
            if (t != null) {
                try {
                    t.right();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                return false;
            }
        }
        if (isTapeLeft(input)) {
            String[] splittedDef = input.split("(<<){1}", 2);
            Tape t = FunctionConfig.database.getTapeReal(splittedDef[0]);
            if (t != null) {
                try {
                    t.left();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

}
