package com.progressoft.fxdeals.service;

import com.progressoft.fxdeals.model.Deal;
import com.progressoft.fxdeals.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealServiceImpl implements DealService {

    private final DealRepository dealRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean saveDeal(Deal deal) {
        try {
            if (dealRepository.existsById(deal.getDealId())) {
                log.warn("Duplicate deal ID: {}", deal.getDealId());
                return false;
            }

            dealRepository.save(deal);
            log.info("Saved deal: {}", deal.getDealId());
            return true;

        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate key violation: {}", deal.getDealId());
            return false;
        }
    }
}