import java.util.concurrent.LinkedBlockingDeque;

public final class HomogeneousRun extends Run {
    private String status;

    public HomogeneousRun(long s) {
        super(s);
        status = "benchmarking";
    }

    public HomogeneousRun() {
        super();
        status = "benchmarking";
    }

    @Override
    protected String generateRunString() {
        StringBuilder sb = new StringBuilder();
        Agent a = appendUntilIntelligible();

        status = "intelligible";
        sb.append(SimulationCoordinator.IRREGULARITY_STRING + "\n");

        IrregularityHeatMap irregularity = new IrregularityHeatMap();

        for (int i = 0; i < ConstantManager.getNumToAnalyze(); i++) {
            Generation g = new HomogeneousGeneration(a, nextLong());
            g.converse();

            a = g.getListener();

            if (!a.isFilled()) {
                i--;
            } else {
                IrregularityHeatMap cur = a.getIrregularityHeatMap();

                sb.append(cur.getTotalIrregularForms() + "\n");
                irregularity.add(cur);
            }
        }

        appendEnd(sb, a, irregularity);
        return sb.toString();
    }

    @Override
    public String getCurrentStatus() {
        return status;
    }

    private Agent appendUntilIntelligible() {
        Agent a = new Agent(nextLong());
        LinkedBlockingDeque<String[][]> signals = new LinkedBlockingDeque<String[][]>(
                ConstantManager.getIntelligibilityDelay() + 1);

        while (signals.remainingCapacity() > 0) {
            Generation g = new HomogeneousGeneration(a, nextLong());
            g.converse();

            a = g.getListener();
            signals.add(a.getAllSignals());
        }

        status = "benchmarked";

        while (intelligibility(signals.getFirst(), signals.getLast()) < ConstantManager.getIntelligibilityThreshold()) {
            Generation g = new HomogeneousGeneration(a, nextLong());
            g.converse();

            a = g.getListener();
            signals.removeFirst();
            signals.add(a.getAllSignals());
        }

        return a;
    }

    private void appendEnd(StringBuilder sb, Agent a, IrregularityHeatMap ihm) {
        sb.append(SimulationCoordinator.IRREGULARITY_STRING + "\n");

        sb.append(SimulationCoordinator.HEATMAP_STRING + "\n");
        sb.append(ihm);
        sb.append(SimulationCoordinator.HEATMAP_STRING + "\n");

        sb.append(SimulationCoordinator.GRAMMAR_STRING + "\n");
        sb.append(a.toRawString());
        sb.append(SimulationCoordinator.GRAMMAR_STRING + "\n");

        status = "done";
    }

    private static double intelligibility(String[][] ary1, String[][] ary2) {
        double total = 0;

        for (int i = 0; i < ary1.length; i++) {
            for (int j = 0; j < ary1[i].length; j++) {
                if (ary1[i][j].length() == 0 || ary2[i][j].length() == 0) {
                    return 0;
                }

                if (ary1[i][j].equals(ary2[i][j])) {
                    total += Generation.getWeight(i, j);
                }
            }
        }

        return total / Generation.getWeightSum();
    }
}