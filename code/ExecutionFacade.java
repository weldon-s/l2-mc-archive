import java.util.concurrent.TimeUnit;

public final class ExecutionFacade implements Consumable {
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private static final long TIMEOUT = 1;

    private final SimulationCoordinator coordinator;

    private boolean used;

    public ExecutionFacade(long s) {
        coordinator = new SimulationCoordinator(s);
        used = false;
    }

    public ExecutionFacade() {
        coordinator = new SimulationCoordinator();
        used = false;
    }

    @Override
    public void consume() {
        used = true;

        Thread t = new Thread(coordinator);
        t.start();

        while (t.isAlive()) {
            System.out.print("\u000c");
            System.out.println(getCurrentStatus());

            try {
                TIME_UNIT.sleep(TIMEOUT);
            } catch (InterruptedException e) {
                System.out.println("An unexpected error occurred.");
            }
        }

        System.out.print("\u000c");
        System.out.println(getCurrentStatus());
    }

    @Override
    public boolean hasBeenConsumed() {
        return used;
    }

    @Override
    public String getCurrentStatus() {
        return coordinator.getCurrentStatus();
    }
}