public interface Consumable extends Runnable{
    public abstract boolean hasBeenConsumed();
    
    public abstract void consume();
    
    public abstract String getCurrentStatus();
    
    @Override
    public default void run(){
        if(hasBeenConsumed()){
            throw new IllegalStateException("object " + toString() + "already consumed");
        }
        
        consume();
    }
}