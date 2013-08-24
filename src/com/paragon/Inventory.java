package com.paragon;

import com.paragon.stock.Offer;

import java.util.List;

public interface Inventory {
    List<Offer> searchFor(String query);
}
