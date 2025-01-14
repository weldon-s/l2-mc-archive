import java.util.Random;

public abstract class Run implements Consumable{
    private final long seed;
    private final Random random;
    
    private boolean used;
    private String string;
    
    public Run(long s){
        seed = s;
        random = new Random(s);
        used = false;
    }

    public Run(){
        seed = new Random().nextLong();
        random = new Random(seed);
        used = false;
    }
    
    @Override
    public final boolean hasBeenConsumed(){
        return used;
    }
    
    @Override
    public final void consume(){
        string = getSimulationString();
        used = true;
    }
    
    public final long nextLong(){
        return random.nextLong();
    }
    
    public final long getSeed(){
        return seed;
    }
    
    public final String getSimulationString(){
        if(string == null){
            string = generateRunString();
        } 
        
        return string;
    }
    
    protected abstract String generateRunString();
}
