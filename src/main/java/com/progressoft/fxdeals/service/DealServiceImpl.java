package com.progressoft.fxdeals.service;

import com.progressoft.fxdeals.exception.DuplicateRecordException;
import com.progressoft.fxdeals.model.Deal;
import com.progressoft.fxdeals.repository.DealRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            // Check for duplicates BEFORE attempting to save
            if (dealRepository.existsById(deal.getDealId())) {
                log.warn("Duplicate deal ID detected: {}", deal.getDealId());
                return false;
            }

            dealRepository.save(deal);
            dealRepository.flush();
            log.info("Saved deal: {}", deal.getDealId());
            return true;

        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate deal ID (constraint violation): {}", deal.getDealId());
            return false;
        }
    }

    @Override
    @Transactional
    public Deal createDeal(Deal deal) {
        if (dealRepository.existsById(deal.getDealId())) {
            throw new DuplicateRecordException(
                    String.format("Deal with ID '%s' already exists", deal.getDealId()));
        }

        try {
            Deal savedDeal = dealRepository.save(deal);
            dealRepository.flush(); // Force immediate write to DB
            log.info("Created new deal: {}", savedDeal.getDealId());
            return savedDeal;

        } catch (DataIntegrityViolationException e) {
            throw new DuplicateRecordException(
                    String.format("Deal with ID '%s' already exists", deal.getDealId()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Deal getDealById(String dealId) {
        return dealRepository.findById(dealId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Deal with ID '%s' not found", dealId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Deal> getAllDeals(Pageable pageable) {
        return dealRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalDealsCount() {
        return dealRepository.count();
    }
}