package com.paragon;

import com.paragon.stock.Offer;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    List<Offer> searchForProduct(String query);

    void confirmOrder(UUID id, String userAuthToken);

}
