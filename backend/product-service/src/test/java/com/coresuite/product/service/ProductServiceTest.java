package com.coresuite.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.coresuite.product.domain.Product;
import com.coresuite.product.dto.ProductCreateRequest;
import com.coresuite.product.dto.ProductResponse;
import com.coresuite.product.dto.VariantRequest;
import com.coresuite.product.repository.ProductRepository;
import com.coresuite.product.repository.ProductVariantRepository;
import com.coresuite.shared.error.ConflictException;
import com.coresuite.shared.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getThrowsWhenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.get(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createSavesAndReturnsMappedResponse() {
        Product saved = new Product();
        saved.setId(1L);
        saved.setName("Widget");
        when(productRepository.save(any())).thenReturn(saved);

        ProductResponse response = productService.create(new ProductCreateRequest("Widget", null, null));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Widget");
    }

    @Test
    void addVariantThrowsConflictWhenSkuAlreadyExists() {
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(variantRepository.existsBySku("SKU-1")).thenReturn(true);
        VariantRequest request = new VariantRequest("SKU-1", null, new BigDecimal("9.99"), "USD");

        assertThatThrownBy(() -> productService.addVariant(1L, request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void addVariantThrowsNotFoundWhenProductMissing() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        VariantRequest request = new VariantRequest("SKU-1", null, new BigDecimal("9.99"), "USD");

        assertThatThrownBy(() -> productService.addVariant(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
