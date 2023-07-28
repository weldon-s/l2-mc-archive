public class Main {
    public static void main(String[] args) {
        try {
            new ExecutionFacade().consume();
        } catch (RuntimeException | Error e) {
            System.out.println("An unexpected problem occurred:\n" + e);

            for (StackTraceElement st : e.getStackTrace()) {
                System.out.println(st);
            }
        }
    }
}