import java.util.function.Function;

public enum ConstantEntryType{
    INT(Integer::parseInt),
    
    DOUBLE(Double::parseDouble);

    private final Function<String, Object> function;

    private ConstantEntryType(Function<String, Object> func){
        function = func;
    }
    
    public Object read(String in){
        return function.apply(in);
    }
}