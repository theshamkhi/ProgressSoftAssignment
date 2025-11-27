package com.progressoft.fxdeals.repository;

import com.progressoft.fxdeals.model.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DealRepository extends JpaRepository<Deal, String> {

    boolean existsByDealId(String dealId);
}