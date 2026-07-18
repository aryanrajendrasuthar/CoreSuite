package com.coresuite.crm.web;

import com.coresuite.crm.dto.CustomerCreateRequest;
import com.coresuite.crm.dto.CustomerResponse;
import com.coresuite.crm.dto.CustomerUpdateRequest;
import com.coresuite.crm.dto.TagRequest;
import com.coresuite.crm.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Long id) {
        return customerService.get(id);
    }

    @GetMapping
    public Page<CustomerResponse> list(@RequestParam(required = false) String tag, Pageable pageable) {
        return customerService.list(tag, pageable);
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerUpdateRequest request) {
        return customerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/tags")
    public CustomerResponse addTag(@PathVariable Long id, @Valid @RequestBody TagRequest request) {
        return customerService.addTag(id, request.tag());
    }

    @DeleteMapping("/{id}/tags/{tag}")
    public CustomerResponse removeTag(@PathVariable Long id, @PathVariable String tag) {
        return customerService.removeTag(id, tag);
    }
}
