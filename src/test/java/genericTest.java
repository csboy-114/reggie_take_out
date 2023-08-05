import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class genericTest {
    @Test
    public void test(){
        Gen<String> gen = new Gen<>("baisha");
        System.out.println(gen.getKey());

    }
}

class Gen<T>{
    private T key;
    public Gen(T key){
        this.key = key;
    }
    T getKey(){
        return key;
    }
}
