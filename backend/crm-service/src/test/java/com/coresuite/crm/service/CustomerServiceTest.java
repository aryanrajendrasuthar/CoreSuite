package com.coresuite.crm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.coresuite.crm.domain.Customer;
import com.coresuite.crm.dto.CustomerCreateRequest;
import com.coresuite.crm.dto.CustomerResponse;
import com.coresuite.crm.repository.CustomerRepository;
import com.coresuite.crm.repository.SegmentRepository;
import com.coresuite.shared.error.ConflictException;
import com.coresuite.shared.error.ResourceNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SegmentRepository segmentRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void createThrowsConflictWhenEmailAlreadyExists() {
        when(customerRepository.existsByEmail("a@b.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(
                        new CustomerCreateRequest("Ada", "a@b.com", null, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createSavesAndReturnsMappedResponse() {
        Customer saved = new Customer();
        saved.setId(1L);
        saved.setFullName("Ada");
        saved.setEmail("a@b.com");
        when(customerRepository.save(any())).thenReturn(saved);

        CustomerResponse response = customerService.create(new CustomerCreateRequest("Ada", "a@b.com", null, null));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("a@b.com");
    }

    @Test
    void getThrowsWhenCustomerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.get(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void customersInSegmentThrowsWhenSegmentNotFound() {
        when(segmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.customersInSegment(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
