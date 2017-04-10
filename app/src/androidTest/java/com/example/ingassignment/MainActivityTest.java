package com.example.ingassignment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.internal.Assignments;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

/**
 * Created by Jean on 2017/4/2.
 */

@RunWith(AndroidJUnit4.class)
@android.support.test.filters.LargeTest
public class MainActivityTest{// extends ActivityInstrumentationTestCase2<MainActivity>

    /*public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getActivity(); //launch HomeActivity
    }*/

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);
    public ActivityTestRule<CartDetailActivity> mActivityRule2 = new ActivityTestRule(CartDetailActivity.class);
    private MainActivity mainActivity;
    private CartDetailActivity cartDetailActivity;

    @Before
    public void setActivity() {
        mainActivity = mActivityRule.getActivity();
        onData(anything()).inAdapterView(withId(R.id.product_listview))
                .atPosition(0).onChildView(withId(R.id.add_to_cart_button)).perform(click()).perform(click());
    }

    @Test
    public void testCartItemAndQuantity() {
        assertEquals(1,mainActivity.cartMap.size());
        assertEquals(2, (long)mainActivity.cartMap.get(mainActivity.product_list.get(0).getProductId()));
    }

    @Test
    public void testViewDisplayInCartActivity(){
        onView(withId(R.id.go_cart_button)).perform(click());
        cartDetailActivity = mActivityRule2.getActivity();
        onData(anything())
                .inAdapterView(withId(R.id.product_listview))
                .atPosition(0)
                .onChildView(withId(R.id.text_quantity))
                .check(matches(withText("2")));
    }
}