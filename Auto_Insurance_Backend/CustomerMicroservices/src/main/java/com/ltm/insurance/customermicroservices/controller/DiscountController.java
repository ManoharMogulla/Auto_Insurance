package com.ltm.insurance.customermicroservices.controller;

import com.ltm.insurance.customermicroservices.model.Discounts;
import com.ltm.insurance.customermicroservices.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Discounts create(@RequestBody Discounts discounts) {
        return discountService.create(discounts);
    }

    @GetMapping("/{discountId}")
    public Discounts get(@PathVariable Long discountId) {
        return discountService.get(discountId);
    }

    @PutMapping("/{discountId}")
    public Discounts update(@PathVariable Long discountId, @RequestBody Discounts discounts) {
        return discountService.update(discountId, discounts);
    }

    @DeleteMapping("/{discountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long discountId) {
        discountService.delete(discountId);
    }

    @PostMapping("/calculate")
    public BigDecimal calculate(@RequestBody Discounts discounts) {
        return discountService.calculatePercentage(discounts);
    }
}
