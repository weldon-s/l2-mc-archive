import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public final class Agent {
    private static final char MINIMUM_RANDOM_CHARACTER_VALUE = 'a';
    private static final char MAXIMUM_RANDOM_CHARACTER_VALUE = 'z';

    private static final int MINIMUM_RANDOM_STRING_LENGTH = 1;
    private static final int MAXIMUM_RANDOM_STRING_LENGTH = 10;

    private static final String TERMINAL_PATTERN = "[" + MINIMUM_RANDOM_CHARACTER_VALUE + "-"
            + MAXIMUM_RANDOM_CHARACTER_VALUE + "]+";

    private final Random random;
    private final List<Rule> grammar;

    public Agent(String in, long seed) {
        random = new Random(seed);

        grammar = new ArrayList<Rule>();
        Scanner stringScan = new Scanner(in);

        while (stringScan.hasNext()) {
            String line = stringScan.nextLine();
            grammar.add(new Rule(line));
        }

        stringScan.close();
    }

    public Agent(long seed) {
        random = new Random(seed);
        grammar = new ArrayList<Rule>();
    }

    public String getSignal(Meaning m, boolean update) {
        String sig = trySignal(m.toString());

        if (update) {
            if (sig == null) {
                invent(m);
                sig = trySignal(m.toString());
            }
        }

        if (sig == null) {
            return "";
        }

        return sig;
    }

    private String trySignal(String input) {
        if (input.matches(TERMINAL_PATTERN)) {
            return input;
        }

        String cur = input;
        String ret = null;

        for (int i = 0; i < grammar.size(); i++) {
            String next = grammar.get(i).apply(cur);

            if (!next.equals("") && !cur.equals(next)) {
                String sig = trySignal(next);

                if (sig != null &&
                        sig.matches(TERMINAL_PATTERN) &&
                        (ret == null || sig.length() < ret.length())) {
                    ret = sig;
                }
            }
        }

        return ret;
    }

    public void induce(Meaning m, String lambda) {
        if (containsSignal(lambda)) {
            return;
        }

        grammar.add(new Rule(m, lambda));
        interpolate();
    }

    private void interpolate() {
        boolean repeat = true;

        outer: while (repeat) {
            repeat = false;

            for (int i = 0; i < grammar.size(); i++) {
                for (int j = i + 1; j < grammar.size(); j++) {
                    if (apply(grammar.get(i), grammar.get(j))) {
                        repeat = true;
                        removeDuplicates();
                        continue outer;
                    }
                }
            }
        }
    }

    private void invent(Meaning m) {
        int shortestIndex = -1;

        for (int i = 0; i < grammar.size(); i++) {
            Rule r = grammar.get(i);
            int da = differenceAt(m, r.getMeaning());

            if (da != -1 && r.getMeaning().getComponents()[da].isVariable() &&
                    (shortestIndex == -1 || r.getString().length() < grammar.get(shortestIndex).getString().length())) {
                shortestIndex = i;
            }
        }

        if (shortestIndex == -1) {
            induce(m, randomString());
        } else {
            int da = differenceAt(m, grammar.get(shortestIndex).getMeaning());
            MeaningComponent mc = MeaningComponent.getInstanceFromCoordinates(da, -1);

            Rule temp = new Rule(new Meaning(mc), randomString());
            grammar.add(temp);

            String ret = getSignal(m.replace(m.getComponents()[da], mc), false);
            grammar.remove(temp);

            induce(m, ret);
        }
    }

    public String[][] getAllSignals() {
        String[][] ret = new String[ConstantManager.getNumValues()][ConstantManager.getNumValues()];

        for (int i = 0; i < ret.length; i++) {
            for (int j = 0; j < ret[i].length; j++) {
                ret[i][j] = getSignal(new Meaning(i, j), false);
            }
        }

        return ret;
    }

    public String getAllFormattedSignals() {
        return BasicIO.getFormattedMeaningSpaceGrid(getAllSignals());
    }

    public IrregularityHeatMap getIrregularityHeatMap() {
        removeDuplicates();
        IrregularityHeatMap ihm = new IrregularityHeatMap();

        for (int i = 0; i < grammar.size(); i++) {
            if (!grammar.get(i).getMeaning().isVariable()) {
                ihm.increment(grammar.get(i).getMeaning());
            }
        }

        return ihm;
    }

    public String toRawString() {
        removeDuplicates();
        Collections.sort(grammar);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < grammar.size(); i++) {
            Rule r = grammar.get(i);

            for (int j = 0; j < r.getMeaning().getComponents().length; j++) {
                sb.append(r.getMeaning().getComponents()[j].getComponentString() + " ");
            }

            sb.append(r.getString() + "\n");
        }

        return sb.toString();
    }

    public boolean isFilled() {
        for (String[] ary : getAllSignals()) {
            for (String s : ary) {
                if (s.length() == 0) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public String toString() {
        removeDuplicates();
        Collections.sort(grammar);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < grammar.size(); i++) {
            sb.append(grammar.get(i).toString() + "\n");
        }

        return sb.toString();
    }

    private boolean apply(Rule r1, Rule r2) {
        int da = differenceAt(r1.getMeaning(), r2.getMeaning());
        int diffBounds[] = maxDifferenceBounds(r1.getString(), r2.getString());

        if (chunkable(r1, r2, da, diffBounds)) {
            chunk(r1, r2, da, diffBounds);
            return true;
        }

        if (r1.contains(r2)) {
            writeInTerms(r1, r2);
            return true;
        }

        if (r2.contains(r1)) {
            writeInTerms(r2, r1);
            return true;
        }

        return false;
    }

    private void chunk(Rule r1, Rule r2, int da, int[] diffBounds) {
        boolean b1 = r1.getMeaning().getComponents()[da].isVariable();
        boolean b2 = r2.getMeaning().getComponents()[da].isVariable();

        if (b1 ^ b2) {
            singleChunk(r1, r2, da, diffBounds, b1);
        } else if (!b1 && !b2) {
            doubleChunk(r1, r2, da, diffBounds);
        }
    }

    private void singleChunk(Rule r1, Rule r2, int da, int[] diffBounds, boolean b1) {
        Rule delete = b1 ? r2 : r1;

        Meaning mu = new Meaning(delete.getMeaning().getComponents()[da]);
        String lambda = substring(delete.getString(), diffBounds);

        grammar.add(new Rule(mu, lambda));
        grammar.remove(delete);
    }

    private void doubleChunk(Rule r1, Rule r2, int da, int[] diffBounds) {
        MeaningComponent m1 = r1.getMeaning().getComponents()[da];
        MeaningComponent m2 = r2.getMeaning().getComponents()[da];

        String l1 = substring(r1.getString(), diffBounds);
        String l2 = substring(r2.getString(), diffBounds);

        MeaningComponent var = m1.variable();

        String lNew = r1.getString().substring(0, diffBounds[0]);
        lNew += var.toString();
        lNew += r1.getString().substring(r1.getString().length() - diffBounds[1]);

        grammar.add(new Rule(new Meaning(m1), l1));
        grammar.add(new Rule(new Meaning(m2), l2));
        grammar.add(new Rule(r1.getMeaning().replace(m1, var), lNew));

        grammar.remove(r1);
        grammar.remove(r2);
    }

    private void writeInTerms(Rule outside, Rule inside) {
        MeaningComponent var = inside.getMeaning().getComponents()[0].variable();

        Meaning meaning = outside.getMeaning().replace(inside.getMeaning().getComponents()[0], var);
        String string = outside.getString().replaceFirst(inside.getString(), var.toString());

        grammar.add(new Rule(meaning, string));
        grammar.remove(outside);
    }

    private void removeDuplicates() {
        boolean[] remove = new boolean[grammar.size()];

        for (int i = 0; i < grammar.size(); i++) {
            Rule r1 = grammar.get(i);

            for (int j = i + 1; j < grammar.size(); j++) {
                Rule r2 = grammar.get(j);

                if (r1.getMeaning().equals(r2.getMeaning())) {
                    if (r1.getString().length() < r2.getString().length()) {
                        remove[j] = true;
                    } else {
                        remove[i] = true;
                    }
                }
            }
        }

        int index = 0;

        for (int i = 0; i < remove.length; i++, index++) {
            if (remove[i]) {
                grammar.remove(index);
                index--;
            }
        }
    }

    private String randomString() {
        int length = randomBetween(MINIMUM_RANDOM_STRING_LENGTH, MAXIMUM_RANDOM_STRING_LENGTH);

        String ret = "";

        for (int i = 0; i < length; i++) {
            ret += (char) randomBetween(MINIMUM_RANDOM_CHARACTER_VALUE, MAXIMUM_RANDOM_CHARACTER_VALUE);
        }

        return ret;
    }

    private int randomBetween(int lower, int upper) {
        return random.nextInt(upper - lower + 1) + lower;
    }

    private boolean containsSignal(String in) {
        for (int i = 0; i < ConstantManager.getNumValues(); i++) {
            for (int j = 0; j < ConstantManager.getNumValues(); j++) {
                String signal = getSignal(new Meaning(i, j), false);

                if (in.equals(signal)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static int differenceAt(Meaning m1, Meaning m2) {
        MeaningComponent[] a1 = m1.getComponents();
        MeaningComponent[] a2 = m2.getComponents();

        if (a1.length != a2.length || a1.length == 1) {
            return -1;
        }

        int ret = -1;

        for (int i = 0; i < a1.length; i++) {
            if (!a1[i].equals(a2[i])) {
                if (ret == -1) {
                    ret = i;
                } else {
                    return -1;
                }
            }
        }

        return ret;
    }

    private static int[] maxDifferenceBounds(String s1, String s2) {
        int beg = 0;
        int end = 0;

        int stop = s1.length() < s2.length() ? s1.length() : s2.length();

        while (beg < stop && s1.charAt(beg) == s2.charAt(beg)) {
            beg++;
        }

        if (beg == stop) {
            return null;
        }

        while (end < stop && s1.charAt(s1.length() - 1 - end) == s2.charAt(s2.length() - 1 - end)) {
            end++;
        }

        int[] ret = { beg, end };

        if (end == stop || beg == 0 && end == 0 || beg + end >= stop) {
            return null;
        }

        return ret;
    }

    private static String substring(String in, int[] diffBounds) {
        return in.substring(diffBounds[0], in.length() - diffBounds[1]);
    }

    private static boolean chunkable(Rule r1, Rule r2, int da, int[] diffBounds) {
        return da != -1 && diffBounds != null &&
                (substring(r1.getString(), diffBounds).matches(TERMINAL_PATTERN) ||
                        substring(r2.getString(), diffBounds).matches(TERMINAL_PATTERN));
    }
}