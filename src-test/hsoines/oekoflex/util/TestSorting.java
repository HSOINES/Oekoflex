package hsoines.oekoflex.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class TestSorting {
    @Test
    public void testSort(){
        List<Float> list = new ArrayList<>();
        list.add(3f);
        list.add(2f);
        list.add(4f);
        System.out.println(list.toString());
        list.sort(Float::compare);
        for (Float aFloat : list) {
            System.out.println(aFloat);
        }
        
        System.out.println(list);
    }
}
