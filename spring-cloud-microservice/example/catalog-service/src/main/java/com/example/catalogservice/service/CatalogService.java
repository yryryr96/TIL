package com.example.catalogservice.service;

import com.example.catalogservice.entity.CatalogEntity;

import java.util.List;

public interface CatalogService {

    Iterable<CatalogEntity> getAllCatalogs();

}
