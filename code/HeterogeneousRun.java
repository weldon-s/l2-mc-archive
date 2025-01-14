public final class HeterogeneousRun extends Run{
    private final double probability;
    private final Agent nativeSpeaker;
    private final Agent secondLanguageSpeaker;
    
    private String status;

    public HeterogeneousRun(long s, double prob, Agent l1, Agent l2){
        super(s);

        probability = prob;
        nativeSpeaker = l1;
        secondLanguageSpeaker = l2;
        
        status = "0";
    }    

    public HeterogeneousRun(double prob, Agent l1, Agent l2){
        super();

        probability = prob;
        nativeSpeaker = l1;
        secondLanguageSpeaker = l2;
        
        status = "0";
    }

    @Override
    protected String generateRunString(){
        StringBuilder sb = new StringBuilder();
        
        Generation init = new HeterogeneousGeneration(nativeSpeaker, secondLanguageSpeaker, nextLong(), probability);
        init.converse();

        Agent cur = init.getListener();

        sb.append(SimulationCoordinator.IRREGULARITY_STRING + "\n");
        sb.append(cur.getIrregularityHeatMap().getTotalIrregularForms() + "\n");

        int i = 1;

        while(i < ConstantManager.getNumToAnalyze()){
            Generation g = new HeterogeneousGeneration(cur, secondLanguageSpeaker, nextLong(), probability);
            g.converse();

            cur = g.getListener();

            if(cur.isFilled()){
                sb.append(cur.getIrregularityHeatMap().getTotalIrregularForms() + "\n");   
                i++;
                status = i + "";
            }
        }

        sb.append(SimulationCoordinator.IRREGULARITY_STRING + "\n");

        return sb.toString();
    }
    
    @Override
    public String getCurrentStatus(){
        return status;
    }
}