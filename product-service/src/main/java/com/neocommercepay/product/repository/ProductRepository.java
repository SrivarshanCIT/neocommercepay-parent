package com.neocommercepay.product.repository;

import com.neocommercepay.product.document.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Page<Product> findByCategoryId(String categoryId, Pageable pageable);
    List<Product> findByNameContainingIgnoreCase(String name);
    Page<Product> findAll(Pageable pageable);
}
