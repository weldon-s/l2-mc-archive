import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class SimulationCoordinator implements Consumable {
    public static final String IRREGULARITY_STRING = "irregularity";
    public static final String GRAMMAR_STRING = "grammar";
    public static final String HEATMAP_STRING = "heatmap";

    private static final String BASE_STRING = "data";
    private static final String HOMOGENEOUS_STRING = "homogeneous";
    private static final String HETEROGENEOUS_STRING = "heterogeneous";
    private static final String HIGH_TO_LOW_STRING = "hightolow";
    private static final String LOW_TO_HIGH_STRING = "lowtohigh";
    private static final String ANALYSIS_STRING = "analysis";

    private static final long TIMEOUT = 10;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    private static final FileFormat BASE = new FileFormat(BASE_STRING, "txt");

    private static final FileFormat HOMOGENEOUS = BASE.getFileFormatWithSubFolder(HOMOGENEOUS_STRING, "out");

    private static final FileFormat HOMOGENEOUS_IRREGULARITY = HOMOGENEOUS
            .getFileFormatWithSubFolder(IRREGULARITY_STRING);
    private static final FileFormat HOMOGENEOUS_GRAMMAR = HOMOGENEOUS.getFileFormatWithSubFolder(GRAMMAR_STRING);
    private static final FileFormat HOMOGENEOUS_ANALYSIS = HOMOGENEOUS.getFileFormatWithSubFolder(ANALYSIS_STRING);
    private static final FileFormat HOMOGENEOUS_HEATMAP = HOMOGENEOUS.getFileFormatWithSubFolder(HEATMAP_STRING);

    private static final FileFormat HETEROGENEOUS = BASE.getFileFormatWithSubFolder(HETEROGENEOUS_STRING, "out");

    private static final FileFormat HIGH_TO_LOW = HETEROGENEOUS.getFileFormatWithSubFolder(HIGH_TO_LOW_STRING);
    private static final FileFormat HIGH_TO_LOW_IRREGULARITY = HIGH_TO_LOW
            .getFileFormatWithSubFolder(IRREGULARITY_STRING);
    private static final FileFormat HIGH_TO_LOW_ANALYSIS = HIGH_TO_LOW.getFileFormatWithSubFolder(ANALYSIS_STRING);

    private static final FileFormat LOW_TO_HIGH = HETEROGENEOUS.getFileFormatWithSubFolder(LOW_TO_HIGH_STRING);
    private static final FileFormat LOW_TO_HIGH_IRREGULARITY = LOW_TO_HIGH
            .getFileFormatWithSubFolder(IRREGULARITY_STRING);
    private static final FileFormat LOW_TO_HIGH_ANALYSIS = LOW_TO_HIGH.getFileFormatWithSubFolder(ANALYSIS_STRING);

    private final Random random;
    private final long seed;

    private StringBuilder log;

    private String coordinatorStatus;
    private SimulationWriter[] currentWriters;
    private boolean used;

    public SimulationCoordinator(long s) {
        random = new Random(s);
        seed = s;

        log = new StringBuilder();
        coordinatorStatus = "Starting simulations";
        currentWriters = new SimulationWriter[0];
        used = false;
    }

    public SimulationCoordinator() {
        seed = new Random().nextLong();
        random = new Random(seed);

        log = new StringBuilder();
        coordinatorStatus = "Starting simulations";
        currentWriters = new SimulationWriter[0];
        used = false;
    }

    @Override
    public void consume() {
        used = true;
        long startTime = System.currentTimeMillis();

        log.append("Execution Log\n");
        log.append("Seed for this execution: " + seed + "\n\n");
        log.append("Simulation seeds:\n");

        runSimulationsHomogeneous();
        int[] indices = printAnalysisHomogeneous();
        printHeatMap();

        log.append("Language " + indices[0] + " was the least irregular and language " + indices[1]
                + " was the most irregular\n\n");

        runSimulationsHeterogeneous(indices[0], indices[1]);
        printAnalysisHeterogeneous();

        printConstantValues();

        log.append((System.currentTimeMillis() - startTime) + "ms to execute\n");

        BasicIO.write(BASE.getFile("log"), log.toString());

        currentWriters = new SimulationWriter[0];
        coordinatorStatus = "Simulations done";
    }

    private void runSimulationsHomogeneous() {
        log.append("Homogeneous runs:\n");

        Transcription totalTranscription = new Transcription(HOMOGENEOUS_IRREGULARITY.getFile("total"), "irregularity");
        currentWriters = new SimulationWriter[ConstantManager.getNumLanguages()];
        coordinatorStatus = "Running homogeneous simulations";

        for (int i = 0; i < ConstantManager.getNumLanguages(); i++) {
            long simSeed = random.nextLong();
            log.append("Simulation " + i + " seed: " + simSeed + "\n");

            Run s = new HomogeneousRun(simSeed);
            currentWriters[i] = new SimulationWriter(i + "", HOMOGENEOUS, s, transcriptionArray(i, totalTranscription));
        }

        executeAll(currentWriters);
        totalTranscription.print();
    }

    private static int[] printAnalysisHomogeneous() {
        ListAnalyzer first = constructAndPrintHomogeneous("0");

        double lowest = first.getMean();
        double highest = first.getMean();

        int lowestIndex = 0;
        int highestIndex = 0;

        for (int i = 1; i < ConstantManager.getNumLanguages(); i++) {
            ListAnalyzer la = constructAndPrintHomogeneous(i + "");
            double mean = la.getMean();

            if (mean < lowest) {
                lowest = mean;
                lowestIndex = i;
            } else if (mean > highest) {
                highest = mean;
                highestIndex = i;
            }
        }

        constructAndPrintHomogeneous("total");
        return new int[] { lowestIndex, highestIndex };
    }

    private static void printHeatMap() {
        IrregularityHeatMap ihm = new IrregularityHeatMap();

        for (int i = 0; i < ConstantManager.getNumLanguages(); i++) {
            String str = BasicIO.read(HOMOGENEOUS_HEATMAP.getFile(i + ""));
            ihm.add(new IrregularityHeatMap(str));
        }

        BasicIO.write(HOMOGENEOUS_HEATMAP.getFile("total"), ihm.getHeatMap());
    }

    private void runSimulationsHeterogeneous(int low, int high) {
        runSimulationsHighToLow(low, high);
        runSimulationsLowToHigh(low, high);
    }

    private void runSimulationsHighToLow(int low, int high) {
        log.append("Heterogeneous runs (high to low):\n");
        currentWriters = new SimulationWriter[getLength()];
        coordinatorStatus = "Running high-irregularity to low-irregularity simulations";

        int i = 0;
        int index = 0;

        while (i <= 100) {
            double prob = (double) i * 0.01;

            long simSeed = random.nextLong();
            long mainSeed = random.nextLong();
            long addSeed = random.nextLong();

            log.append("Simulation " + i + " seed: " + simSeed + ", L1 agent seed: " + mainSeed + ", L2 agent seed: "
                    + addSeed + "\n");

            Agent main = new Agent(BasicIO.read(HOMOGENEOUS_GRAMMAR.getFile(high + "")), mainSeed);
            Agent add = new Agent(BasicIO.read(HOMOGENEOUS_GRAMMAR.getFile(low + "")), addSeed);

            Run s = new HeterogeneousRun(simSeed, prob, main, add);
            Transcription trans = new Transcription(HIGH_TO_LOW_IRREGULARITY.getFile(i + ""), "irregularity", true);

            currentWriters[index++] = new SimulationWriter(i + "", HIGH_TO_LOW, s, trans);

            i += ConstantManager.getPercentChange();

            if (i > 100 && i < 100 + ConstantManager.getPercentChange()) {
                i = 100;
            }
        }

        executeAll(currentWriters);
    }

    private void runSimulationsLowToHigh(int low, int high) {
        log.append("Heterogeneous runs (low to high):\n");
        currentWriters = new SimulationWriter[getLength()];
        coordinatorStatus = "Running low-irregularity to high-irregularity simulations";

        int i = 0;
        int index = 0;

        while (i <= 100) {
            double prob = (double) i * 0.01;

            long simSeed = random.nextLong();
            long mainSeed = random.nextLong();
            long addSeed = random.nextLong();

            log.append("Simulation " + i + " seed: " + simSeed + ", L1 agent seed: " + mainSeed + ", L2 agent seed: "
                    + addSeed + "\n");

            Agent main = new Agent(BasicIO.read(HOMOGENEOUS_GRAMMAR.getFile(low + "")), mainSeed);
            Agent add = new Agent(BasicIO.read(HOMOGENEOUS_GRAMMAR.getFile(high + "")), addSeed);

            Run s = new HeterogeneousRun(simSeed, prob, main, add);
            Transcription trans = new Transcription(LOW_TO_HIGH_IRREGULARITY.getFile(i + ""), "irregularity", true);

            currentWriters[index++] = new SimulationWriter(i + "", LOW_TO_HIGH, s, trans);

            i += ConstantManager.getPercentChange();

            if (i > 100 && i < 100 + ConstantManager.getPercentChange()) {
                i = 100;
            }
        }

        executeAll(currentWriters);
    }

    private static void printAnalysisHeterogeneous() {
        int i = 0;

        while (i <= 100) {
            constructAndPrintHighToLow(i + "");
            constructAndPrintLowToHigh(i + "");

            i += ConstantManager.getPercentChange();

            if (i > 100 && i < 100 + ConstantManager.getPercentChange()) {
                i = 100;
            }
        }
    }

    private void printConstantValues() {
        log.append("\nConstant parameters set as follows:\n");

        for (ConstantEntry e : ConstantManager.getEntries()) {
            log.append(e.getLabel() + ": " + e.getValue() + "\n");
        }

        log.append("\n");
    }

    @Override
    public boolean hasBeenConsumed() {
        return used;
    }

    @Override
    public String getCurrentStatus() {
        String ret = coordinatorStatus + "\n";

        for (int i = 0; i < currentWriters.length; i++) {
            try {
                ret += currentWriters[i].getCurrentStatus() + "\n";
            } catch (NullPointerException e) {
            }
        }

        return ret;
    }

    private static void executeAll(Runnable[] ary) {
        ExecutorService es = Executors.newCachedThreadPool();

        try {
            for (Runnable r : ary) {
                es.execute(r);
            }

            es.shutdown();
            while (!es.awaitTermination(TIMEOUT, TIME_UNIT))
                ;
        } catch (InterruptedException e) {
            es.shutdownNow();
        }
    }

    private static Transcription[] transcriptionArray(int index, Transcription common) {
        return new Transcription[] {
                new Transcription(HOMOGENEOUS_IRREGULARITY.getFile(index + ""), "irregularity", true),
                new Transcription(HOMOGENEOUS_GRAMMAR.getFile(index + ""), "grammar", true),
                new Transcription(HOMOGENEOUS_HEATMAP.getFile(index + ""), "heatmap", true),
                common
        };
    }

    private static int getLength() {
        int ret = 100 / ConstantManager.getPercentChange() + 1;

        if (100 % ConstantManager.getPercentChange() != 0) {
            ret++;
        }

        return ret;
    }

    private static ListAnalyzer constructAndPrintHomogeneous(String in) {
        ListAnalyzer la = new ListAnalyzer(BasicIO.read(HOMOGENEOUS_IRREGULARITY.getFile(in)));
        BasicIO.write(HOMOGENEOUS_ANALYSIS.getFile(in), la.getAnalysisString());

        return la;
    }

    private static void constructAndPrintHighToLow(String in) {
        ListAnalyzer la = new ListAnalyzer(BasicIO.read(HIGH_TO_LOW_IRREGULARITY.getFile(in)));
        BasicIO.write(HIGH_TO_LOW_ANALYSIS.getFile(in), la.getAnalysisString());
    }

    private static void constructAndPrintLowToHigh(String in) {
        ListAnalyzer la = new ListAnalyzer(BasicIO.read(LOW_TO_HIGH_IRREGULARITY.getFile(in)));
        BasicIO.write(LOW_TO_HIGH_ANALYSIS.getFile(in), la.getAnalysisString());
    }
}