import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

public abstract class Generation implements Iterable<Meaning>{
    private static final double[] RANGES;

    private final Random random;
    
    private final Agent speaker;
    private final Agent listener;

    private int count;

    static{
        double[][] mags = magnitudes();

        RANGES = new double[mags.length * mags[0].length];

        RANGES[0] = mags[0][0];

        for(int i = 1; i < RANGES.length; i++){
            RANGES[i] = RANGES[i - 1] + mags[i / mags[0].length][i % mags[0].length];
        }
    }

    public Generation(Agent old, long seed){
        random = new Random(seed);
        
        speaker = old;
        listener = new Agent(random.nextLong());
        count = 0;
    }

    public final Agent getSpeaker(){
        return speaker;
    }

    public final Agent getListener(){
        return listener;
    }
    
    
    public final double nextDouble(){
        return random.nextDouble();
    }

    public final String erode(String in){
        String ret = "";

        for(int i = 0; i < in.length(); i++){
            if(random.nextDouble() > ConstantManager.getErosionProbability()){
                ret += in.charAt(i);
            }
        }
        
        return ret;
    }

    @Override
    public final Iterator<Meaning> iterator(){
        return new Iterator<Meaning>(){
            @Override
            public Meaning next(){
                if(count >= ConstantManager.getMeaningsPerGeneration()){
                    throw new NoSuchElementException("meanings for this generation have been exhausted");
                }

                count++;

                double value = random.nextDouble() * RANGES[RANGES.length - 1];
                int index = 0;

                while(RANGES[index] < value){
                    index++;
                }

                return new Meaning(index / ConstantManager.getNumValues(), index % ConstantManager.getNumValues());
            }

            @Override
            public boolean hasNext(){
                return count < ConstantManager.getMeaningsPerGeneration();
            }
        };
    }

    public abstract void converse();

    public static final double getWeight(int i, int j){
        return (double) 1 / (i + 1) / (j + 1);
    }

    public static final double getWeightSum(){
        return RANGES[RANGES.length - 1];
    }

    private static double[][] magnitudes(){
        double[][] ret = new double[ConstantManager.getNumValues()][ConstantManager.getNumValues()];

        for(int i = 0; i < ret.length; i++){
            for(int j = 0; j < ret[i].length; j++){
                ret[i][j] = getWeight(i, j);
            }
        }

        return ret;
    }
}