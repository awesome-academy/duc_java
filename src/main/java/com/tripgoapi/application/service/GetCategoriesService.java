package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.GetCategoriesUseCase;
import com.tripgoapi.application.port.out.CategoryRepositoryInterface;
import com.tripgoapi.domain.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetCategoriesService implements GetCategoriesUseCase {

    private final CategoryRepositoryInterface categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }
}
