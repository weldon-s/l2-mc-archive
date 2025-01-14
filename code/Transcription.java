import java.io.File;
import java.util.Scanner;

public final class Transcription {
    private final File destination;
    private final String flag;
    private final boolean autoPrint;
    private boolean printed;
    private StringBuilder contents;

    public Transcription(File d, String f, boolean ap) {
        destination = d;
        flag = f;
        autoPrint = ap;
        printed = false;
        contents = new StringBuilder();
    }

    public Transcription(File d, String f) {
        destination = d;
        flag = f;
        autoPrint = false;
        printed = false;
        contents = new StringBuilder();
    }

    public String getFlag() {
        return flag;
    }

    public void appendFromFile(File in) {
        verifyNotPrinted();

        String str = BasicIO.read(in);
        Scanner scan = new Scanner(str);

        while (!scan.nextLine().equals(flag)) {
        }

        String cur = scan.nextLine();

        while (!cur.equals(flag)) {
            contents.append(cur + "\n");
            cur = scan.nextLine();
        }

        scan.close();

        if (autoPrint) {
            print();
        }
    }

    public void print() {
        verifyNotPrinted();

        BasicIO.write(destination, contents.toString());
        printed = true;
    }

    private void verifyNotPrinted() {
        if (printed) {
            throw new IllegalStateException("transcription already printed");
        }
    }
}