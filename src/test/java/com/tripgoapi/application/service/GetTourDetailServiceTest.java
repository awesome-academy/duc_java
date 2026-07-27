package com.tripgoapi.application.service;

import com.tripgoapi.application.port.out.TourDetailRepositoryInterface;
import com.tripgoapi.domain.exception.TourNotFoundException;
import com.tripgoapi.domain.model.TourDetail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTourDetailServiceTest {

    @Mock
    private TourDetailRepositoryInterface tourDetailRepository;

    @Test
    void returnsTourDetail_whenFound() {
        GetTourDetailService service = new GetTourDetailService(tourDetailRepository);
        TourDetail detail = new TourDetail(1L, "Title", "slug", "desc", null, null,
                3, 10, null, null, null, 0, null, null, null, null, null);
        when(tourDetailRepository.findById(1L)).thenReturn(Optional.of(detail));

        assertThat(service.getTourDetail(1L)).isSameAs(detail);
    }

    @Test
    void throwsTourNotFound_whenMissing() {
        GetTourDetailService service = new GetTourDetailService(tourDetailRepository);
        when(tourDetailRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTourDetail(2L))
                .isInstanceOf(TourNotFoundException.class);
    }

    @Test
    void getTourDetailIsAnnotatedReadOnlyTransactional() throws NoSuchMethodException {
        // Regression guard: TourDetailPersistenceAdapter.toDomain fires several separate
        // repository queries (images, itinerary, highlights, includes, destination). Without a
        // surrounding read-only transaction each call opens its own session, risking
        // LazyInitializationException and inconsistent reads.
        Method method = GetTourDetailService.class.getMethod("getTourDetail", Long.class);
        Transactional annotation = method.getAnnotation(Transactional.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.readOnly()).isTrue();
    }
}
