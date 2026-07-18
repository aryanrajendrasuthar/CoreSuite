package com.coresuite.product.repository;

import com.coresuite.product.domain.Product;
import com.coresuite.product.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategoryAndStatus(String category, ProductStatus status, Pageable pageable);

    Page<Product> findByCategory(String category, Pageable pageable);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
}
