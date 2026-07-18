package com.coresuite.crm.service;

import com.coresuite.crm.domain.Customer;
import com.coresuite.crm.domain.Segment;
import com.coresuite.crm.dto.CustomerCreateRequest;
import com.coresuite.crm.dto.CustomerResponse;
import com.coresuite.crm.dto.CustomerUpdateRequest;
import com.coresuite.crm.repository.CustomerRepository;
import com.coresuite.crm.repository.SegmentRepository;
import com.coresuite.shared.error.ConflictException;
import com.coresuite.shared.error.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final SegmentRepository segmentRepository;

    @Transactional
    public CustomerResponse create(CustomerCreateRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already in use: " + request.email());
        }
        Customer customer = new Customer();
        customer.setFullName(request.fullName());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setCompany(request.company());
        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long id) {
        return CustomerResponse.from(findCustomerOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> list(String tag, Pageable pageable) {
        Page<Customer> page = tag != null
                ? customerRepository.findByTagsContaining(tag, pageable)
                : customerRepository.findAll(pageable);
        return page.map(CustomerResponse::from);
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerUpdateRequest request) {
        Customer customer = findCustomerOrThrow(id);
        if (!customer.getEmail().equals(request.email()) && customerRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already in use: " + request.email());
        }
        customer.setFullName(request.fullName());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setCompany(request.company());
        return CustomerResponse.from(customer);
    }

    @Transactional
    public void delete(Long id) {
        customerRepository.delete(findCustomerOrThrow(id));
    }

    @Transactional
    public CustomerResponse addTag(Long id, String tag) {
        Customer customer = findCustomerOrThrow(id);
        customer.getTags().add(tag);
        return CustomerResponse.from(customer);
    }

    @Transactional
    public CustomerResponse removeTag(Long id, String tag) {
        Customer customer = findCustomerOrThrow(id);
        customer.getTags().remove(tag);
        return CustomerResponse.from(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> customersInSegment(Long segmentId) {
        Segment segment = segmentRepository.findById(segmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Segment not found: " + segmentId));
        return customerRepository
                .findMatchingAllTags(segment.getRequiredTags(), segment.getRequiredTags().size())
                .stream()
                .map(CustomerResponse::from)
                .toList();
    }

    private Customer findCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }
}
