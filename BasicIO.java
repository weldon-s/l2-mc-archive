import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Formatter;
import java.util.Scanner;

public final class BasicIO {
    private static final int MINIMUM_COLUMN_WIDTH = 2;
    private static final int PADDING = 2;

    private BasicIO() {
        throw new AssertionError("objects of class BasicIO should not be able to be instantiated");
    }

    public static String read(File file) {
        String temp = "";

        try (Scanner scan = new Scanner(file)) {
            while (scan.hasNext()) {
                temp += scan.nextLine() + "\n";
            }
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("bad file: " + file);
        }

        return temp;
    }

    public static void write(File file, String string) {
        try (PrintWriter pw = new PrintWriter(file, "UTF-8")) {
            pw.print(string);
            pw.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("bad file: " + file);
        }
    }

    public static String getFormattedMeaningSpaceGrid(String[][] ary) {
        if (ary.length != ConstantManager.getNumValues()) {
            throw new IllegalArgumentException("bad array dimension: " + ary.length);
        }

        String[][] pass = new String[ConstantManager.getNumValues() + 1][ConstantManager.getNumValues() + 1];

        pass[0][0] = "";

        for (int i = 0; i < ConstantManager.getNumValues(); i++) {
            pass[0][i + 1] = MeaningComponent.getInstanceFromCoordinates(0, i).getComponentString();
            pass[i + 1][0] = MeaningComponent.getInstanceFromCoordinates(1, i).getComponentString();
        }

        for (int i = 0; i < ConstantManager.getNumValues(); i++) {
            if (ary[i].length != ConstantManager.getNumValues()) {
                throw new IllegalArgumentException("bad array dimension: " + ary[i].length);
            }

            for (int j = 0; j < ConstantManager.getNumValues(); j++) {
                pass[i + 1][j + 1] = ary[i][j];
            }
        }

        return getFormattedGrid(pass);
    }

    public static String getFormattedGrid(String[][] ary) {
        Formatter f = new Formatter();
        int[] widths = widths(ary);

        for (int j = 0; j < ary[0].length; j++) {
            for (int i = 0; i < ary.length; i++) {
                f.format("%" + (widths[i] + PADDING) + "s", ary[i][j]);
            }

            f.format("%s", "\n");
        }

        String ret = f.toString();
        f.close();

        return ret;
    }

    private static int[] widths(String[][] ary) {
        int[] ret = new int[ary.length];

        for (int i = 0; i < ary.length; i++) {
            ret[i] = MINIMUM_COLUMN_WIDTH;

            for (int j = 0; j < ary[i].length; j++) {
                if (ary[i][j].length() > ret[i]) {
                    ret[i] = ary[i][j].length();
                }
            }
        }

        return ret;
    }
}