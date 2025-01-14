/**
 * Abstract class for nonterminal elements, laying out some common methods.
 */
public abstract class NonTerminal {
    /**
     * Returns the category string for this nonterminal (S for a and b, A for a, B
     * for b)
     * 
     * @return as discussed above
     */
    public abstract String getCategoryString();

    /**
     * Returns the string for the value of this nonterminal (e.g. A0, B1, and
     * (A0,B1))
     * 
     * @return
     */
    public abstract String getComponentString();

    public abstract boolean isVariable();

    @Override
    public final String toString() {
        return getCategoryString() + ":" + getComponentString();
    }
}