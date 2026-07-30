package com.tripgoapi.application.port.in;

import com.tripgoapi.domain.model.Category;

import java.util.List;

public interface GetCategoriesUseCase {

    List<Category> getCategories();
}
