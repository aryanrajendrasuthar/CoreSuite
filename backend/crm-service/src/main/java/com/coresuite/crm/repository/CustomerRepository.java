package com.coresuite.crm.repository;

import com.coresuite.crm.domain.Customer;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);

    Page<Customer> findByTagsContaining(String tag, Pageable pageable);

    @Query("""
            SELECT c FROM Customer c JOIN c.tags t
            WHERE t IN :tags
            GROUP BY c
            HAVING COUNT(DISTINCT t) = :tagCount
            """)
    List<Customer> findMatchingAllTags(@Param("tags") Set<String> tags, @Param("tagCount") long tagCount);
}
