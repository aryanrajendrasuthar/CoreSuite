package com.coresuite.product.service;

import com.coresuite.product.domain.Product;
import com.coresuite.product.domain.ProductStatus;
import com.coresuite.product.domain.ProductVariant;
import com.coresuite.product.dto.ProductCreateRequest;
import com.coresuite.product.dto.ProductResponse;
import com.coresuite.product.dto.ProductUpdateRequest;
import com.coresuite.product.dto.VariantRequest;
import com.coresuite.product.dto.VariantResponse;
import com.coresuite.product.repository.ProductRepository;
import com.coresuite.product.repository.ProductVariantRepository;
import com.coresuite.shared.error.ConflictException;
import com.coresuite.shared.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(findProductOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(String category, ProductStatus status, Pageable pageable) {
        Page<Product> page;
        if (category != null && status != null) {
            page = productRepository.findByCategoryAndStatus(category, status, pageable);
        } else if (category != null) {
            page = productRepository.findByCategory(category, pageable);
        } else if (status != null) {
            page = productRepository.findByStatus(status, pageable);
        } else {
            page = productRepository.findAll(pageable);
        }
        return page.map(ProductResponse::from);
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = findProductOrThrow(id);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setStatus(request.status());
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findProductOrThrow(id);
        productRepository.delete(product);
    }

    @Transactional
    public VariantResponse addVariant(Long productId, VariantRequest request) {
        Product product = findProductOrThrow(productId);
        if (variantRepository.existsBySku(request.sku())) {
            throw new ConflictException("SKU already in use: " + request.sku());
        }
        ProductVariant variant = new ProductVariant();
        variant.setSku(request.sku());
        variant.setPrice(request.price());
        if (request.attributes() != null) {
            variant.setAttributes(request.attributes());
        }
        if (request.currency() != null) {
            variant.setCurrency(request.currency());
        }
        product.addVariant(variant);
        return VariantResponse.from(variantRepository.save(variant));
    }

    @Transactional
    public VariantResponse updateVariant(Long variantId, VariantRequest request) {
        ProductVariant variant = findVariantOrThrow(variantId);
        if (!variant.getSku().equals(request.sku()) && variantRepository.existsBySku(request.sku())) {
            throw new ConflictException("SKU already in use: " + request.sku());
        }
        variant.setSku(request.sku());
        variant.setPrice(request.price());
        if (request.attributes() != null) {
            variant.setAttributes(request.attributes());
        }
        if (request.currency() != null) {
            variant.setCurrency(request.currency());
        }
        return VariantResponse.from(variant);
    }

    @Transactional
    public void deleteVariant(Long variantId) {
        ProductVariant variant = findVariantOrThrow(variantId);
        variantRepository.delete(variant);
    }

    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private ProductVariant findVariantOrThrow(Long variantId) {
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));
    }
}
