package com.coresuite.crm.repository;

import com.coresuite.crm.domain.Segment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SegmentRepository extends JpaRepository<Segment, Long> {

    boolean existsByName(String name);
}
