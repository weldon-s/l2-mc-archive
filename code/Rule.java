import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class Rule implements Comparable<Rule> {
    // This is mostly for cosmetic reasons
    private static final String ARROW = "→";

    private final Meaning mu;
    private final String lambda;

    /**
     * Constructs a rule from a string representation. This is mostly used to load
     * in agent grammars from a string representation.
     * 
     * @param in a string with meaning component(s) and a signal string, all
     *           space-separated
     */
    public Rule(String in) {
        System.out.println(in);
        String[] split = in.split("\\s+");

        mu = new Meaning(Arrays.copyOf(split, split.length - 1));
        lambda = split[split.length - 1];
    }

    /**
     * Constructs a rule from a meaning and a signal, indicating that the given
     * meaning is mapped to the given signal
     * 
     * @param meaning the meaning
     * @param string  the signal
     */
    public Rule(Meaning meaning, String string) {
        mu = meaning;
        lambda = string;
    }

    /**
     * Returns the meaning of this rule
     * 
     * @return the meaning of this rule
     */
    public Meaning getMeaning() {
        return mu;
    }

    /**
     * Returns the signal of this rule
     * 
     * @return the signal of this rule
     */
    public String getString() {
        return lambda;
    }

    /**
     * Returns true if and only if this rule contains the given rule
     * This occurs when the meaning of this rule contains the meaning of the given
     * rule and the signal of this rule contains (but does not equal) the signal of
     * the given rule.
     * 
     * @param r the rule to check
     * @return true if and only if this rule contains the given rule
     */
    public boolean contains(Rule r) {
        return mu.contains(r.mu) && lambda.contains(r.lambda) && !lambda.equals(r.lambda);
    }

    /**
     * Returns the result of applying this rule to the given string, or an empty
     * string if the meaning in the given string is not generalizable to the meaning
     * of this rule. This basically means that this method returns an empty string
     * when this rule doesn't apply.
     * 
     * @param in the string to apply this rule to
     * @return as described above
     */
    public String apply(String in) {
        Meaning m = new Meaning(in);

        if (!m.isGeneralizableTo(mu)) {
            return "";
        }

        String l2 = lambda;
        List<MeaningComponent> include = new ArrayList<MeaningComponent>();
        for (int i = 0; i < m.getComponents().length; i++) {
            MeaningComponent toReplace = mu.getComponentWithCategory(m.getComponents()[i].getCategoryString());

            if (toReplace != null) {
                l2 = l2.replace(toReplace.toString(), m.getComponents()[i].toString());
                include.add(m.getComponents()[i]);
            }
        }

        return in.replace(new Meaning(include.toArray(new MeaningComponent[0])).toString(), l2);
    }

    @Override
    public boolean equals(Object o) {
        try {
            Rule r = (Rule) o;
            return mu.equals(r.mu) && lambda.equals(r.lambda);
        } catch (ClassCastException | NullPointerException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(mu, lambda);
    }

    @Override
    public String toString() {
        return mu + ARROW + lambda;
    }

    @Override
    public int compareTo(Rule r) {
        boolean b1 = mu.getCategoryString().equals(Meaning.START_CATEGORY);
        boolean b2 = r.mu.getCategoryString().equals(Meaning.START_CATEGORY);

        // If both rules are starting rules, compare the signals
        if (b1 == b2) {
            return mu.compareTo(r.mu);
        }

        // Otherwise, if one is a starting rule, put that one first

        if (b1) {
            return -1;
        }

        return 1;
    }
}