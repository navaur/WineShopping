package com.paragon;

import com.paragon.orders.Order;
import com.paragon.stock.Offer;
import com.paragon.stock.Quote;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RunWith(value = JUnit4.class)
public class TestOrderSystem {

    OrderSystem orderSystem = new OrderSystem();


    @Test
    public void searchForProductTest() {

        List<Offer> offers =
                orderSystem.searchForProduct("South Australia");
        String str;
        int size;
        str = offers.get(0).description;
        size =   offers.size();

        Assert.assertEquals(str, "Red Australia ‐ South Australia Dandelion Vineyards Pride of the Fleurieu Cabernet 2010");
        Assert.assertEquals(size, 1);

        offers = orderSystem.searchForProduct("Argentina");
        size =   offers.size();

        Assert.assertEquals(size, 8);

    }



    @Test
    public void confirmOrderLessThanTwentyMinutesTest() {


        List<Offer> offers =
                orderSystem.searchForProduct("South Australia");


        Map<UUID, Quote> quotes = orderSystem.getQuotes();

        UUID id = offers.get(0).id;
        String userAuthToken = "test";
        long time = quotes.get(id).timestamp + (2 * 60 * 1000);

        Order order = orderSystem.confirmOrder(id,userAuthToken,time);

        BigDecimal charge = order.totalPrice.subtract(offers.get(0).price.multiply(OrderSystem.CASE_SIZE));

        BigDecimal actual = new BigDecimal(5);

        Assert.assertTrue(charge.compareTo(actual) == 0);


    }

    @Test
    public void confirmOrderMOreThanTwentyMinutesThrowsIllegalStateExceptionTest() {

        List<Offer> offers =
                orderSystem.searchForProduct("South Australia");
        Exception exception = null;

        Map<UUID, Quote> quotes = orderSystem.getQuotes();

        UUID id = offers.get(0).id;
        String userAuthToken = "test";
        long time = quotes.get(id).timestamp + (30 * 60 * 1000);

        try{
        Order order = orderSystem.confirmOrder(id,userAuthToken,time);
        }
         catch (IllegalStateException ex){ exception = ex;}

        shouldValidateThrowsExceptionWithMessage(exception, "Quote expired, please get a new price");

    }

    private void shouldValidateThrowsExceptionWithMessage(final Exception e, final String message) {
        Assert.assertNotNull(e);
        Assert.assertTrue(e.getMessage().contains(message));
    }


}
