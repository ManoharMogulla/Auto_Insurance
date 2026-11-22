package com.ltm.insurance.policyservice.repo;

import com.ltm.insurance.policyservice.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyRepo extends JpaRepository<Policy, Long> {

    Policy findByPolicyId(String policyId);
}
