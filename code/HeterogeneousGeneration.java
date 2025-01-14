public final class HeterogeneousGeneration extends Generation{
    private final Agent secondLanguageSpeaker;
    private final double nonnativeProbability;

    private int nativeSpeakerSignals;
    private int secondLanguageSpeakerSignals;
    
    public HeterogeneousGeneration(Agent l1, Agent l2, long seed, double p){
        super(l1, seed);
        nonnativeProbability = p;
        secondLanguageSpeaker = l2;
        
        nativeSpeakerSignals = 0;
        secondLanguageSpeakerSignals = 0;
    }

    @Override
    public void converse(){
        for(Meaning m: this){
            String signal;

            if(nextDouble() < nonnativeProbability  &&
            secondLanguageSpeakerSignals < maxSecondLanguageSpeakerSignals() ||
            nativeSpeakerSignals > maxNativeSpeakerSignals()){
                signal = secondLanguageSpeaker.getSignal(m, true);
                secondLanguageSpeakerSignals++;
            }
            else{
                signal = getSpeaker().getSignal(m, true);
                nativeSpeakerSignals++;
            }

            String pass = erode(signal);

            if(pass.length() > 0){
                getListener().induce(m, pass);
            }
        }
    }
    
    private int maxNativeSpeakerSignals(){
        return (int) Math.ceil(ConstantManager.getMeaningsPerGeneration() * (1 - nonnativeProbability));
    }
    
    private int maxSecondLanguageSpeakerSignals(){
        return (int) Math.ceil(ConstantManager.getMeaningsPerGeneration() * nonnativeProbability);
    }
}