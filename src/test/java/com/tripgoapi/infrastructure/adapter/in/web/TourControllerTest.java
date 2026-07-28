package com.tripgoapi.infrastructure.adapter.in.web;

import com.tripgoapi.application.port.in.GetTourAvailabilityUseCase;
import com.tripgoapi.application.port.in.GetTourDetailUseCase;
import com.tripgoapi.application.port.in.GetToursUseCase;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.in.TourSearchQuery;
import com.tripgoapi.infrastructure.mapper.TourAvailabilityWebMapper;
import com.tripgoapi.infrastructure.mapper.TourDetailWebMapper;
import com.tripgoapi.infrastructure.mapper.TourWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TourControllerTest {

    @Mock
    private GetToursUseCase getToursUseCase;
    @Mock
    private GetTourDetailUseCase getTourDetailUseCase;
    @Mock
    private GetTourAvailabilityUseCase getTourAvailabilityUseCase;
    @Mock
    private TourWebMapper tourWebMapper;
    @Mock
    private TourDetailWebMapper tourDetailWebMapper;
    @Mock
    private TourAvailabilityWebMapper tourAvailabilityWebMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TourController controller = new TourController(
                getToursUseCase, getTourDetailUseCase, getTourAvailabilityUseCase,
                tourWebMapper, tourDetailWebMapper, tourAvailabilityWebMapper
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void nonPositivePage_isRejectedWith400_insteadOfCausing500() throws Exception {
        // Before Bean Validation was wired in, page<=0 relied on TourSearchQuery silently
        // clamping it to 1. Now the controller rejects it outright with 400, and the use
        // case must never even be invoked.
        mockMvc.perform(get("/tours").param("page", "-5").param("limit", "12"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(getToursUseCase);
    }

    @Test
    void oversizedLimit_isRejectedWith400_insteadOfLoadingUnboundedRows() throws Exception {
        mockMvc.perform(get("/tours").param("limit", "100000"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(getToursUseCase);
    }

    @Test
    void negativeMinPrice_isRejectedWith400() throws Exception {
        mockMvc.perform(get("/tours").param("minPrice", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(getToursUseCase);
    }

    @Test
    void ratingAboveFive_isRejectedWith400() throws Exception {
        mockMvc.perform(get("/tours").param("rating", "5.1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(getToursUseCase);
    }

    @Test
    void validPageAndLimit_arePassedThroughUnchanged() throws Exception {
        when(getToursUseCase.searchTours(any())).thenReturn(new PageResult<>(List.of(), 0, 2, 50));

        mockMvc.perform(get("/tours").param("page", "2").param("limit", "50"))
                .andExpect(status().isOk());

        ArgumentCaptor<TourSearchQuery> captor = ArgumentCaptor.forClass(TourSearchQuery.class);
        verify(getToursUseCase).searchTours(captor.capture());
        assertThat(captor.getValue().page()).isEqualTo(2);
        assertThat(captor.getValue().size()).isEqualTo(50);
    }

    @Test
    void nonNumericPage_returns400_notInternalServerError() throws Exception {
        mockMvc.perform(get("/tours").param("page", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonPositiveTourId_isRejectedWith400_beforeHittingUseCase() throws Exception {
        mockMvc.perform(get("/tours/0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(getTourDetailUseCase);
    }
}
