package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.in.TourSearchQuery;
import com.tripgoapi.application.port.out.TourRepositoryInterface;
import com.tripgoapi.domain.model.Tour;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetToursServiceTest {

    @Mock
    private TourRepositoryInterface tourRepository;

    @Test
    void delegatesSearchToRepository() {
        GetToursService service = new GetToursService(tourRepository);
        TourSearchQuery query = new TourSearchQuery(null, null, null, null, null, null, null, null, null, 1, 12);
        PageResult<Tour> expected = new PageResult<>(List.of(), 0, 1, 12);
        when(tourRepository.searchTours(query)).thenReturn(expected);

        assertThat(service.searchTours(query)).isSameAs(expected);
    }

    @Test
    void searchToursIsAnnotatedReadOnlyTransactional() throws NoSuchMethodException {
        Method method = GetToursService.class.getMethod("searchTours", TourSearchQuery.class);
        Transactional annotation = method.getAnnotation(Transactional.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.readOnly()).isTrue();
    }
}
