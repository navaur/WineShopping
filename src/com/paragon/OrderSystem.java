package com.paragon;

import com.paragon.orders.Order;
import com.paragon.orders.OrderLedger;
import com.paragon.stock.Offer;
import com.paragon.stock.Quote;
import com.paragon.stock.Warehouse;

import java.math.BigDecimal;
import java.util.*;

public class OrderSystem implements OrderService {

    private static final long MAX_QUOTE_AGE_MILLIS = 20 * 60 * 1000;
    private static final long TIER1_QUOTE_AGE_MILLIS = 2 * 60 * 1000;
    private static final long TIER2_QUOTE_AGE_MILLIS = 10 * 60 * 1000;
    private static final long TIER3_QUOTE_AGE_MILLIS = 20 * 60 * 1000;

    public static final BigDecimal STANDARD_PROCESSING_CHARGE = new BigDecimal(5);
    public static final BigDecimal TIER1_PROCESSING_CHARGE = new BigDecimal(0);
    public static final BigDecimal TIER2_PROCESSING_CHARGE = new BigDecimal(10);
    public static final BigDecimal TIER2_PROCESSING_CHARGE_PERC = new BigDecimal(0.05);
    public static final BigDecimal TIER3_PROCESSING_CHARGE = new BigDecimal(20);
    public static final BigDecimal CASE_SIZE = new BigDecimal(12);

    private Map<UUID, Quote> quotes = new HashMap<UUID, Quote>();

    @Override
    public List<Offer> searchForProduct(String query) {
        List<Offer> searchResults = Warehouse.getInstance().searchFor(query);
        for (Offer offer : searchResults) {
            quotes.put(offer.id, new Quote(offer, System.currentTimeMillis()));
        }
        return searchResults;
    }

    @Override
    public void confirmOrder(UUID id, String userAuthToken) {
                 this.confirmOrder(id, userAuthToken, 0);
    }

    public Order confirmOrder(UUID id, String userAuthToken, long time) {

        if (!quotes.containsKey(id)) throw new NoSuchElementException("Offer ID is invalid");

        Quote quote = quotes.get(id);

        long timeNow = System.currentTimeMillis();
        if(time != 0) {
            timeNow = time;
        }

        BigDecimal processingCharges = calculateProcessingCharges(quote, timeNow);

        Order completeOrder = new Order(quote.offer.price.multiply(CASE_SIZE).add(processingCharges), quote, timeNow, userAuthToken); //   Refractor-> extract->variable

        OrderLedger.getInstance().placeOrder(completeOrder);
        return completeOrder;
    }


    // Refractored the processing in a new method
    private BigDecimal calculateProcessingCharges(Quote quote, long timeNow) {
        long timeLag = timeNow - quote.timestamp;
        if (timeLag > MAX_QUOTE_AGE_MILLIS) {     // Refractor-> extract->variable
            throw new IllegalStateException("Quote expired, please get a new price");
        }
        // processing for variable processing charges
        BigDecimal processingCharges = STANDARD_PROCESSING_CHARGE;
        if(timeLag <= TIER1_QUOTE_AGE_MILLIS){
            processingCharges  = TIER1_PROCESSING_CHARGE;
        }  else if (timeLag<= TIER2_QUOTE_AGE_MILLIS)   {
            processingCharges  = TIER2_PROCESSING_CHARGE;
            BigDecimal varProcessingCharges = quote.offer.price.multiply(CASE_SIZE).multiply(TIER2_PROCESSING_CHARGE_PERC) ;
            if (varProcessingCharges.compareTo(processingCharges)==-1) {
                processingCharges = varProcessingCharges;
            }
        }     else if (timeLag <=TIER3_QUOTE_AGE_MILLIS)    {
            processingCharges  = TIER3_PROCESSING_CHARGE;
        }

        return processingCharges;
    }

    public static void main (String args[]) {
        OrderSystem os = new OrderSystem() ;
        List<Offer> searchResults = os.searchForProduct("Argentina");
        for (Offer offer : searchResults) {
           System.out.println("<tr><td>" +offer.id +"</td></td>" + offer.description + "</td></tr>");
        }
        System.out.println("Size : " + searchResults.size());
    }

    public Map<UUID, Quote> getQuotes() {
        return quotes;
    }
}