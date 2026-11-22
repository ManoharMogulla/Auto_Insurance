package com.ltm.insurance.customermicroservices.repo;

import com.ltm.insurance.customermicroservices.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Vehiclerepo extends JpaRepository<Vehicle, Long> {


}
