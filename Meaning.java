import java.util.Arrays;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Meanings represent elements of the meaning space
 */
public final class Meaning extends NonTerminal implements Comparable<Meaning> {
    private final MeaningComponent[] components;
    private final int numVariables;

    public static final String START_CATEGORY = "S";

    public Meaning(String in) {
        String[] ary = Pattern.compile(MeaningComponent.CONSTANT_PATTERN + "|" + MeaningComponent.VARIABLE_PATTERN)
                .matcher(in)
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);

        components = new MeaningComponent[ary.length];

        for (int i = 0; i < components.length; i++) {
            components[i] = MeaningComponent.getInstanceFromString(ary[i]);
        }

        numVariables = countVariables(components);
        verify();
    }

    public Meaning(String[] ary) {
        components = new MeaningComponent[ary.length];

        for (int i = 0; i < components.length; i++) {
            components[i] = MeaningComponent.getInstanceFromString(ary[i]);
        }

        numVariables = countVariables(components);
        verify();
    }

    public Meaning(MeaningComponent... comps) {
        components = comps;
        numVariables = countVariables(components);
        verify();
    }

    public Meaning(Integer... ints) {
        if (ints.length != 2) {
            throw new IllegalArgumentException("bad array length: " + ints.length);
        }

        components = new MeaningComponent[ints.length];

        for (int i = 0; i < ints.length; i++) {
            components[i] = MeaningComponent.getInstanceFromCoordinates(i, ints[i]);
        }

        numVariables = countVariables(components);
    }

    private static int countVariables(MeaningComponent[] mcs) {
        int ret = 0;

        for (MeaningComponent mc : mcs) {
            if (mc.isVariable()) {
                ret++;
            }
        }

        return ret;
    }

    private void verify() {
        if (components.length == 0 || components.length > 2) {
            throw new IllegalArgumentException("bad array length: " + components.length);
        }
    }

    public MeaningComponent[] getComponents() {
        return components;
    }

    public Meaning replace(MeaningComponent m1, MeaningComponent m2) {
        MeaningComponent[] ret = new MeaningComponent[components.length];

        for (int i = 0; i < components.length; i++) {
            ret[i] = components[i].equals(m1) ? m2 : components[i];
        }

        return new Meaning(ret);
    }

    public boolean contains(Meaning m) {
        if (components.length <= m.components.length) {
            return false;
        }

        for (MeaningComponent mc : m.components) {
            if (!arrayContains(components, mc)) {
                return false;
            }
        }

        return true;
    }

    public boolean isGeneralizableTo(Meaning m) {
        if (contains(m)) {
            return true;
        }

        if (components.length != m.components.length) {
            return false;
        }

        for (int i = 0; i < components.length; i++) {
            if (!components[i].isGeneralizableTo(m.components[i])) {
                return false;
            }
        }

        return true;
    }

    public MeaningComponent getComponentWithCategory(String s) {
        for (MeaningComponent mc : components) {
            if (mc.getCategoryString().equals(s)) {
                return mc;
            }
        }

        return null;
    }

    @Override
    public boolean isVariable() {
        return components.length != 2 || numVariables != 0;
    }

    @Override
    public String getCategoryString() {
        if (components.length == 1) {
            return components[0].getCategoryString();
        }

        return START_CATEGORY;
    }

    @Override
    public String getComponentString() {
        // if we have one component only, just return that one's string
        if (components.length == 1) {
            return components[0].getComponentString();
        }

        // otherwise, list them out separated by commas and then surround with brackets
        String ret = "";

        for (MeaningComponent c : components) {
            ret += c.getComponentString() + ",";
        }

        return "(" + ret.substring(0, ret.length() - 1) + ")";
    }

    @Override
    public boolean equals(Object o) {
        try {
            Meaning m = (Meaning) o;
            return Arrays.equals(components, m.components);
        } catch (ClassCastException | NullPointerException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object[]) components);
    }

    @Override
    public int compareTo(Meaning m) {
        int varDiff = m.numVariables - numVariables;

        if (varDiff == 0) {
            return toString().compareTo(m.toString());
        }

        return varDiff;
    }

    private static boolean arrayContains(Object[] ary, Object o) {
        for (Object ob : ary) {
            if (o.equals(ob)) {
                return true;
            }
        }

        return false;
    }
}