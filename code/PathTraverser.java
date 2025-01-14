import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class PathTraverser implements Iterable<PathTraverser.PathEntry> {
    public static final class PathEntry {
        private String name;
        private boolean directory;

        private PathEntry(String n, boolean d) {
            name = n;
            directory = d;
        }

        public String getName() {
            return name;
        }

        public boolean isDirectory() {
            return directory;
        }
    }

    private final String rootPath;
    private final Deque<String> fileStack;
    private String[] currentList;

    public PathTraverser(String rp) {
        rootPath = rp;
        fileStack = new ArrayDeque<String>();

        fileStack.addLast(rootPath);
        updateCurrentList();
    }

    public boolean canRetract() {
        return fileStack.size() > 1;
    }

    public void retract() {
        fileStack.pollLast();
        updateCurrentList();
    }

    public boolean isDirectory() {
        return currentList != null;
    }

    public void advanceTo(String next) {
        fileStack.addLast(fileStack.peekLast() + "\\" + next);
        updateCurrentList();
    }

    public String getCurrentPath() {
        return fileStack.peekLast();
    }

    @Override
    public Iterator<PathEntry> iterator() {
        return new Iterator<PathEntry>() {
            int index = 0;

            @Override
            public PathEntry next() {
                if (index < currentList.length) {
                    index++;
                    return new PathEntry(currentList[index - 1],
                            new File(fileStack.peekLast() + "\\" + currentList[index - 1]).list() != null);
                }

                throw new NoSuchElementException("no more entries");
            }

            @Override
            public boolean hasNext() {
                return index < currentList.length;
            }
        };
    }

    private void updateCurrentList() {
        currentList = new File(fileStack.peekLast()).list();

        if (currentList != null) {
            Arrays.sort(currentList, PathTraverser::compareStrings);
        }
    }

    private static int compareStrings(String s1, String s2) {
        if (s1.contains(".")) {
            if (s2.contains(".")) {
                return compareNumericSubstrings(s1, s2);
            }

            return 1;
        }

        if (s2.contains(".")) {
            return -1;
        }

        return compareNumericSubstrings(s1, s2);
    }

    private static int compareNumericSubstrings(String s1, String s2) {
        String ns1 = numericSubstring(s1);
        String ns2 = numericSubstring(s2);

        if (ns1.length() == 0) {
            if (ns2.length() == 0) {
                return s1.compareTo(s2);
            }

            return 1;
        }

        if (ns2.length() == 0) {
            return -1;
        }

        return Integer.parseInt(ns1) - Integer.parseInt(ns2);
    }

    private static String numericSubstring(String s) {
        int lastIndex = 0;

        while (lastIndex < s.length() && Character.isDigit(s.charAt(lastIndex))) {
            lastIndex++;
        }

        return s.substring(0, lastIndex);
    }
}