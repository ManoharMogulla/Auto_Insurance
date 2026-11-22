//package com.ltm.insurance.customermicroservices.service;
//
//import com.ltm.insurance.customermicroservices.DTO.PolicyDetails;
//import com.ltm.insurance.customermicroservices.client.PolicyCilent;
//import com.ltm.insurance.customermicroservices.model.Driver;
//import com.ltm.insurance.customermicroservices.repo.DriverRepository;
//import com.ltm.insurance.customermicroservices.repo.Vehiclerepo;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//class CustomerServiceTests {
//
//    private DriverRepository driverRepository;
//    private CustomerService customerService;
//
//    @BeforeEach
//    void setUp() {
//        driverRepository = mock(DriverRepository.class);
//        customerService = new CustomerService(
//                mock(PolicyCilent.class), driverRepository, mock(Vehiclerepo.class));
//    }
//
//    @Test
//    void returnsDriverInExistingResponseFormat() {
//        Driver driver = new Driver();
//        driver.setMemberId(1L);
//        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
//
//        PolicyDetails result = customerService.getPoliciesByMemberId(1L);
//
//        assertEquals(List.of(driver), result.getDrivers());
//        verify(driverRepository).findById(1L);
//    }
//
//    @Test
//    void returnsNotFoundWhenDriverDoesNotExist() {
//        when(driverRepository.findById(1L)).thenReturn(Optional.empty());
//
//        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
//                () -> customerService.getPoliciesByMemberId(1L));
//
//        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
//        assertEquals("Driver not found for memberId: 1", exception.getReason());
//    }
//}
