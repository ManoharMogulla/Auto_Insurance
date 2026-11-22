package com.ltm.insurance.customermicroservices.repo;

import com.ltm.insurance.customermicroservices.model.Discounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountsRepository extends JpaRepository<Discounts, Long> {
}
