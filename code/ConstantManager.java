public final class ConstantManager {
    private static final FileFormat FORMAT = new FileFormat("params", "in");
    private static final String FILE_NAME = "values";
    private static final ConstantEntry[] ENTRIES;

    public static final int NUM_CONSTANTS;

    static {
        String[] ary = BasicIO.read(FORMAT.getFile(FILE_NAME)).split("\\s+");

        if (ary.length % 3 != 0) {
            throw new AssertionError("parameter file not formatted properly");
        }

        String[] types = new String[ary.length / 3];
        String[] labels = new String[ary.length / 3];
        String[] values = new String[ary.length / 3];

        for (int i = 0; i < labels.length; i++) {
            types[i] = ary[3 * i];
            labels[i] = ary[3 * i + 1];
            values[i] = ary[3 * i + 2];
        }

        ENTRIES = ConstantEntry.getArray(types, labels, values);
        NUM_CONSTANTS = ENTRIES.length;
    }

    private ConstantManager() {
        throw new AssertionError("objects of class ConstantManager should not be able to be instantiated");
    }

    public static int getNumValues() {
        return (int) ENTRIES[0].getValue();
    }

    public static int getMeaningsPerGeneration() {
        return (int) ENTRIES[1].getValue();
    }

    public static double getErosionProbability() {
        return (double) ENTRIES[2].getValue();
    }

    public static int getNumToAnalyze() {
        return (int) ENTRIES[3].getValue();
    }

    public static int getIntelligibilityDelay() {
        return (int) ENTRIES[4].getValue();
    }

    public static double getIntelligibilityThreshold() {
        return (double) ENTRIES[5].getValue();
    }

    public static int getNumLanguages() {
        return (int) ENTRIES[6].getValue();
    }

    public static int getPercentChange() {
        return (int) ENTRIES[7].getValue();
    }

    public static ConstantEntry[] getEntries() {
        return ENTRIES;
    }
}