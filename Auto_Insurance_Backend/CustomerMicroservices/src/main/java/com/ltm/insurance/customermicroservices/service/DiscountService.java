package com.ltm.insurance.customermicroservices.service;

import com.ltm.insurance.customermicroservices.model.Discounts;
import com.ltm.insurance.customermicroservices.repo.DiscountsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private static final BigDecimal SAFE_PILOT_PERCENT = BigDecimal.valueOf(10);
    private static final BigDecimal GOOD_STUDENT_PERCENT = BigDecimal.valueOf(5);
    private static final BigDecimal ANTI_THEFT_PERCENT = BigDecimal.valueOf(5);
    private static final BigDecimal LOW_MILEAGE_PERCENT = BigDecimal.valueOf(5);

    private final DiscountsRepository discountsRepository;

    @Transactional
    public Discounts create(Discounts discounts) {
        validate(discounts);
        discounts.setDiscountId(null);
        return discountsRepository.save(discounts);
    }

    @Transactional(readOnly = true)
    public Discounts get(Long discountId) {
        return discountsRepository.findById(discountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Discount not found: " + discountId));
    }

    @Transactional
    public Discounts update(Long discountId, Discounts discounts) {
        validate(discounts);
        Discounts existing = get(discountId);
        existing.setSafePilotDiscount(discounts.getSafePilotDiscount());
        existing.setGoodStudentDiscount(discounts.getGoodStudentDiscount());
        existing.setAntiTheftDiscount(discounts.getAntiTheftDiscount());
        existing.setLowMileageDiscount(discounts.getLowMileageDiscount());
        return discountsRepository.save(existing);
    }

    @Transactional
    public void delete(Long discountId) {
        discountsRepository.delete(get(discountId));
    }

    public BigDecimal calculatePercentage(Discounts discounts) {
        validate(discounts);
        BigDecimal total = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(discounts.getSafePilotDiscount())) total = total.add(SAFE_PILOT_PERCENT);
        if (Boolean.TRUE.equals(discounts.getGoodStudentDiscount())) total = total.add(GOOD_STUDENT_PERCENT);
        if (Boolean.TRUE.equals(discounts.getAntiTheftDiscount())) total = total.add(ANTI_THEFT_PERCENT);
        if (Boolean.TRUE.equals(discounts.getLowMileageDiscount())) total = total.add(LOW_MILEAGE_PERCENT);
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private void validate(Discounts discounts) {
        if (discounts == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount details are required");
        }
    }
}
