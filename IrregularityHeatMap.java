import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public final class IrregularityHeatMap {
    private Map<Meaning, Integer> irregularity;
    private int total;

    public IrregularityHeatMap(String in) {
        Scanner scan = new Scanner(in);

        irregularity = new HashMap<Meaning, Integer>();
        total = 0;

        while (scan.hasNext()) {
            String next = scan.next();

            if (next.length() < 1) {
                return;
            }

            Meaning key = new Meaning(next);
            int value = scan.nextInt();

            irregularity.put(key, value);
            total += value;

            scan.nextLine();
        }

        scan.close();
    }

    public IrregularityHeatMap() {
        irregularity = new HashMap<Meaning, Integer>();
        total = 0;
    }

    public void increment(Meaning m) {
        if (m.isVariable()) {
            throw new IllegalArgumentException("meaning " + m + " is variable");
        }

        if (irregularity.containsKey(m)) {
            irregularity.put(m, irregularity.get(m) + 1);
        } else {
            irregularity.put(m, 1);
        }

        total++;
    }

    public void add(IrregularityHeatMap ihm) {
        Set<Map.Entry<Meaning, Integer>> set = ihm.irregularity.entrySet();

        for (Map.Entry<Meaning, Integer> entry : set) {
            if (irregularity.containsKey(entry.getKey())) {
                irregularity.put(entry.getKey(), irregularity.get(entry.getKey()) + entry.getValue());
            } else {
                irregularity.put(entry.getKey(), entry.getValue());
            }
        }

        total += ihm.total;
    }

    @Override
    public String toString() {
        String ret = "";

        Set<Map.Entry<Meaning, Integer>> set = irregularity.entrySet();

        for (Map.Entry<Meaning, Integer> entry : set) {
            ret += entry.getKey().getComponentString() + " " + entry.getValue() + "\n";
        }

        return ret;
    }

    public int getTotalIrregularForms() {
        return total;
    }

    public String getHeatMap() {
        String[][] ary = new String[ConstantManager.getNumValues()][ConstantManager.getNumValues()];

        for (int i = 0; i < ConstantManager.getNumValues(); i++) {
            for (int j = 0; j < ConstantManager.getNumValues(); j++) {
                Integer val = irregularity.get(new Meaning(i, j));

                if (val == null) {
                    ary[i][j] = "0";
                } else {
                    ary[i][j] = val + "";
                }
            }
        }

        return BasicIO.getFormattedMeaningSpaceGrid(ary);
    }
}