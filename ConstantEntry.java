public final class ConstantEntry {
    private final String label;
    private final ConstantEntryType type;

    private Object value;

    public ConstantEntry(ConstantEntryType t, String l, String v) {
        label = l;
        type = t;
        value = type.read(v);
    }

    public String getLabel() {
        return label;
    }

    public ConstantEntryType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public void read(String in) {
        value = type.read(in);
    }

    public static ConstantEntry[] getArray(String[] types, String[] labels, String[] values) {
        if (labels.length != types.length || labels.length != values.length) {
            throw new IllegalArgumentException(
                    "bad array lengths: " + labels.length + ", " + types.length + ", and " + values.length);
        }

        ConstantEntry[] ret = new ConstantEntry[labels.length];

        for (int i = 0; i < ret.length; i++) {
            ret[i] = new ConstantEntry(ConstantEntryType.valueOf(types[i]), labels[i], values[i]);
        }

        return ret;
    }
}