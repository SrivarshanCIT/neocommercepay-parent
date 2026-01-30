package com.neocommercepay.product.service;

import com.neocommercepay.common.exception.NotFoundException;
import com.neocommercepay.product.document.Product;
import com.neocommercepay.product.event.ProductEventProducer;
import com.neocommercepay.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEventProducer productEventProducer;

    public Product createProduct(Product product) {
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        Product savedProduct = productRepository.save(product);

        productEventProducer.publishProductCreated(savedProduct);
        log.info("Product created: {}", savedProduct.getName());

        return savedProduct;
    }

    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Product> getProductsByCategory(String categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    public List<Product> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public Product updateProduct(String id, Product productDetails) {
        Product product = getProductById(id);

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setCategoryId(productDetails.getCategoryId());
        product.setCategoryName(productDetails.getCategoryName());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        productEventProducer.publishProductUpdated(updatedProduct);

        return updatedProduct;
    }

    public void deleteProduct(String id) {
        Product product = getProductById(id);
        productRepository.delete(product);
        productEventProducer.publishProductDeleted(id);
        log.info("Product deleted: {}", id);
    }
}
