public final class HomogeneousGeneration extends Generation{
    public HomogeneousGeneration(Agent old, long seed){
        super(old, seed);
    }
        
    @Override
    public void converse(){
        for(Meaning m: this){
            String signal = getSpeaker().getSignal(m, true);
            String pass = erode(signal);
            
            if(pass.length() > 0){
                getListener().induce(m, pass);
            }
        }
    }
}