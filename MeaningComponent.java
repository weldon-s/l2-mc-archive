import java.util.Objects;

/**
 * Represents a component of a meaning (e.g. a0, b1, X)
 */
public abstract class MeaningComponent extends NonTerminal {
    private static final String[] CONSTANTS = { "A", "B" };
    private static final String[] VARIABLES = { "X", "Y" };

    private final String string;

    public static final String VARIABLE_PATTERN = "[X-Z]";
    public static final String CONSTANT_PATTERN = "[AB](\\-|\\d)+";

    // We have a private constructor to control the creation of MeaningComponents
    private MeaningComponent(String s) {
        string = s;
    }

    /**
     * Returns a meaning component based on the inputted string
     * 
     * @param in a string representation of the meaning component
     * @return either a variable or constant meaning component, depending on the
     *         string
     * @throws IllegalArgumentException if the string does not match any known
     *                                  format
     */
    public static final MeaningComponent getInstanceFromString(String in) {
        if (in.matches(CONSTANT_PATTERN)) {
            return new ConstantMeaningComponent(in);
        }

        if (in.matches(VARIABLE_PATTERN)) {
            return new VariableMeaningComponent(in);
        }

        throw new IllegalArgumentException("no suitable constructor found for string " + in);
    }

    /**
     * Returns a meaning component from the given coordinates
     * 
     * @param dimension 0 for "a" components and 1 for "b" components
     * @param value     the integer for the value of the component
     * @return a constant meaning component with the specified values
     */
    public static final MeaningComponent getInstanceFromCoordinates(int dimension, int value) {
        return new ConstantMeaningComponent(dimension, value);
    }

    /**
     * Returns true if and only if this component can be generalized to the provided
     * component. In essence, this method returns true if both meaning components
     * belong to the same category (a or b), and the other component could be
     * replaced with this one in a rule (e.g. A0 is generalizable to X)
     * 
     * @param mc the other meaning component
     * @return as described above
     */
    public final boolean isGeneralizableTo(MeaningComponent mc) {
        if (isVariable()) {
            if (mc.isVariable()) {
                return equals(mc);
            }

            return false;
        }

        if (mc.isVariable()) {
            return getCategoryString().equals(mc.getCategoryString());
        }

        return equals(mc);
    }

    @Override
    public final String getComponentString() {
        return string;
    }

    @Override
    public final boolean equals(Object o) {
        try {
            MeaningComponent mc = (MeaningComponent) o;
            return string.equals(mc.string);
        } catch (ClassCastException | NullPointerException e) {
            return false;
        }
    }

    @Override
    public final int hashCode() {
        return Objects.hash(string);
    }

    /**
     * Returns the variable version of this meaning component given its category
     * 
     * @return as described above
     */
    public abstract MeaningComponent variable();

    /**
     * Class for constant meaning components (i.e. not variables)
     */
    private static final class ConstantMeaningComponent extends MeaningComponent {
        private static final boolean IS_VARIABLE = false;

        private final String dimension;

        private ConstantMeaningComponent(String s) {
            super(s);

            if (!s.matches(CONSTANT_PATTERN)) {
                throw new IllegalArgumentException(s + " does not match expected pattern");
            }

            int index = 0;

            // We sometimes use A-1 and B-1 as placeholders in the manipulation of rules
            // so we need to account for negatives when parsing
            while (!Character.isDigit(s.charAt(index)) && s.charAt(index) != '-') {
                index++;
            }

            dimension = s.substring(0, index);
        }

        private ConstantMeaningComponent(int d, int v) {
            super(CONSTANTS[d] + v);

            dimension = CONSTANTS[d];
        }

        @Override
        public boolean isVariable() {
            return IS_VARIABLE;
        }

        @Override
        public MeaningComponent variable() {
            for (int i = 0; i < CONSTANTS.length; i++) {
                if (CONSTANTS[i].equals(dimension)) {
                    return new VariableMeaningComponent(VARIABLES[i]);
                }
            }

            throw new AssertionError("variable not found for category " + dimension);
        }

        @Override
        public String getCategoryString() {
            return dimension;
        }
    }

    private static final class VariableMeaningComponent extends MeaningComponent {
        private static final boolean IS_VARIABLE = true;

        private VariableMeaningComponent(String s) {
            super(s);

            if (!s.matches(VARIABLE_PATTERN)) {
                throw new IllegalArgumentException(s + " does not match expected pattern");
            }
        }

        @Override
        public boolean isVariable() {
            return IS_VARIABLE;
        }

        @Override
        public MeaningComponent variable() {
            return this;
        }

        @Override
        public String getCategoryString() {
            for (int i = 0; i < VARIABLES.length; i++) {
                if (VARIABLES[i].equals(getComponentString())) {
                    return CONSTANTS[i];
                }
            }

            throw new AssertionError("category not found for variable " + toString());
        }
    }
}