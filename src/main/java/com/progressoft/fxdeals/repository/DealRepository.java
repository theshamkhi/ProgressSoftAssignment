package com.progressoft.fxdeals.repository;

import com.progressoft.fxdeals.model.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DealRepository extends JpaRepository<Deal, String> {

    /**
     * Find deals by either from or to currency
     */
    List<Deal> findByFromCurrencyOrToCurrency(String fromCurrency, String toCurrency);

    /**
     * Search deals with multiple optional filters
     */
    @Query("SELECT d FROM Deal d WHERE " +
            "(:fromCurrency IS NULL OR d.fromCurrency = :fromCurrency) AND " +
            "(:toCurrency IS NULL OR d.toCurrency = :toCurrency) AND " +
            "(:startDate IS NULL OR d.dealTimestamp >= :startDate) AND " +
            "(:endDate IS NULL OR d.dealTimestamp <= :endDate)")
    List<Deal> searchDeals(
            @Param("fromCurrency") String fromCurrency,
            @Param("toCurrency") String toCurrency,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}