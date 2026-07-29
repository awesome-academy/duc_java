package com.tripgoapi.application.port.out;

import com.tripgoapi.domain.model.Category;

import java.util.List;

public interface CategoryRepositoryInterface {

    List<Category> findAll();
}
