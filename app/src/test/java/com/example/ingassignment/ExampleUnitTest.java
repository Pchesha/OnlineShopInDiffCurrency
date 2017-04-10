package com.example.ingassignment;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void checkAddRepeatedItemToCart(){
        ProductItem item1 = new ProductItem(1001, "Name1", "1.5");
        ProductItem item2 = new ProductItem(1002, "Name2", "1.5");
        ArrayList<ProductItem> list = new ArrayList<ProductItem>();
        list.add(item1);
        list.add(item2);
        ArrayList<ProductItem> list2 = new ArrayList<ProductItem>();
        ProductItem item;
        if(list2.contains(list.get(0))){
            item = list2.get(list2.indexOf(list.get(0)));
            item.setQuantity(item.getQuantity()+1);
        } else{
            item = list.get(0).clone();// Avoid these attributes of two list be affected.
            list2.add(item);
        }
        assertEquals(1, list2.size());
        //assertEquals(true,list2.contains(list.get(0)));
    }
}