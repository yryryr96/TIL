package com.example.catalogservice.repository;

import com.example.catalogservice.entity.CatalogEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface CatalogRepository extends CrudRepository<CatalogEntity, Long> {

    Optional<CatalogEntity> findByProductId(String productId);
}
