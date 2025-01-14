public final class SimulationWriter implements Consumable {
    private final String name;
    private final FileFormat fileFormat;
    private final Run simulation;
    private final Transcription[] transcriptions;

    private boolean used;

    public SimulationWriter(String str, FileFormat ff, Run sim, Transcription... trans) {
        name = str;
        fileFormat = ff;
        simulation = sim;
        transcriptions = trans;

        used = false;
    }

    public SimulationWriter(String str, FileFormat ff, Run sim) {
        name = str;
        fileFormat = ff;
        simulation = sim;
        transcriptions = new Transcription[0];

        used = false;
    }

    @Override
    public boolean hasBeenConsumed() {
        return used;
    }

    @Override
    public void consume() {
        used = true;
        write();

        for (Transcription t : transcriptions) {
            t.appendFromFile(fileFormat.getFile(name));
        }
    }

    @Override
    public String getCurrentStatus() {
        return name + ": " + simulation.getCurrentStatus();
    }

    private void write() {
        StringBuilder sb = new StringBuilder();

        start(sb);
        simulation.run();
        sb.append(simulation.getSimulationString() + "\n");
        end(sb);

        BasicIO.write(fileFormat.getFile(name), sb.toString());
    }

    private StringBuilder start(StringBuilder sb) {
        sb.append("Name: " + name + "\n");
        sb.append("Simulation seed: " + simulation.getSeed() + "\n");
        sb.append("Beginning of simulation output...\n\n");

        return sb;
    }

    private StringBuilder end(StringBuilder sb) {
        sb.append("\nEnd of simulation output\n");
        return sb;
    }
}