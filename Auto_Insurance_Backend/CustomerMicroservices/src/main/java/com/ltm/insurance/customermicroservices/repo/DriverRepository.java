package com.ltm.insurance.customermicroservices.repo;



import com.ltm.insurance.customermicroservices.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {


    List<Driver> findByMemberId(Long memberId);

    List<Driver> findByPolicyId(String policyId);

    List<Driver> findByCustomerIdAndNamedInsuredTrue(Long customerId);
}
