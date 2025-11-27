package com.progressoft.fxdeals.service;

import com.progressoft.fxdeals.model.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface DealService {

    /**
     * Save a deal (returns false if duplicate)
     */
    boolean saveDeal(Deal deal);

    /**
     * Create a new deal (throws exception if duplicate)
     */
    Deal createDeal(Deal deal);

    /**
     * Get a deal by ID (throws exception if not found)
     */
    Deal getDealById(String dealId);

    /**
     * Get all deals with pagination
     */
    Page<Deal> getAllDeals(Pageable pageable);

    /**
     * Get total count of deals
     */
    long getTotalDealsCount();
}