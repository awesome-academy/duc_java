package com.tripgoapi.infrastructure.adapter.out.persistence;

import com.tripgoapi.application.port.out.CategoryRepositoryInterface;
import com.tripgoapi.domain.model.Category;
import com.tripgoapi.infrastructure.adapter.out.persistence.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryPersistenceAdapter implements CategoryRepositoryInterface {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public List<Category> findAll() {
        return categoryJpaRepository.findAllByOrderByNameAsc().stream()
                .map(e -> new Category(e.getId(), e.getName(), e.getSlug()))
                .toList();
    }
}
