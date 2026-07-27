package com.tripgoapi.infrastructure.adapter.in.web;

import com.tripgoapi.application.port.in.AddToWishlistUseCase;
import com.tripgoapi.application.port.in.GetWishlistUseCase;
import com.tripgoapi.application.port.in.PageResult;
import com.tripgoapi.application.port.in.RemoveFromWishlistUseCase;
import com.tripgoapi.application.port.out.AuthenticatedPrincipal;
import com.tripgoapi.domain.exception.TourNotFoundException;
import com.tripgoapi.domain.model.Role;
import com.tripgoapi.domain.model.Tour;
import com.tripgoapi.domain.model.WishlistItem;
import com.tripgoapi.infrastructure.mapper.TourWebMapper;
import com.tripgoapi.infrastructure.mapper.WishlistWebMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WishlistControllerTest {

    private static final Long USER_ID = 99L;

    @Mock
    private AddToWishlistUseCase addToWishlistUseCase;
    @Mock
    private RemoveFromWishlistUseCase removeFromWishlistUseCase;
    @Mock
    private GetWishlistUseCase getWishlistUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        WishlistWebMapper wishlistWebMapper = Mappers.getMapper(WishlistWebMapper.class);
        // Mappers.getMapper() instantiates the generated impl via reflection, bypassing Spring
        // DI — so the @Autowired TourWebMapper field WishlistWebMapper delegates to via `uses`
        // needs to be wired in by hand here.
        ReflectionTestUtils.setField(wishlistWebMapper, "tourWebMapper", Mappers.getMapper(TourWebMapper.class));
        WishlistController controller = new WishlistController(
                addToWishlistUseCase, removeFromWishlistUseCase, getWishlistUseCase, wishlistWebMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        // Mirrors exactly what JwtAuthenticationFilter does in production — seeds the
        // SecurityContext directly since this standalone MockMvc setup runs no security filters.
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(USER_ID, "jane@example.com", Role.USER);
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, authorities));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private WishlistItem sampleItem() {
        Tour tour = new Tour(2L, "Da Nang Tour", "da-nang-tour", 1L, "Da Nang",
                BigDecimal.valueOf(1000), BigDecimal.valueOf(900), 3, BigDecimal.valueOf(4.5), 12, true);
        return new WishlistItem(tour, OffsetDateTime.parse("2026-07-20T10:00:00Z"));
    }

    @Test
    void addToWishlist_success_returns201_withLocationHeader_andUsesPrincipalUserId() throws Exception {
        mockMvc.perform(post("/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tourId\": 2}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/wishlist/2"));

        verify(addToWishlistUseCase).addToWishlist(USER_ID, 2L);
    }

    @Test
    void addToWishlist_missingTourId_returns422_andNeverCallsUseCase() throws Exception {
        mockMvc.perform(post("/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity());

        verifyNoInteractions(addToWishlistUseCase);
    }

    @Test
    void addToWishlist_tourNotFound_returns404() throws Exception {
        doThrow(new TourNotFoundException(2L)).when(addToWishlistUseCase).addToWishlist(USER_ID, 2L);

        mockMvc.perform(post("/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tourId\": 2}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeFromWishlist_success_returns204_andUsesPrincipalUserId() throws Exception {
        mockMvc.perform(delete("/wishlist/2"))
                .andExpect(status().isNoContent());

        verify(removeFromWishlistUseCase).removeFromWishlist(USER_ID, 2L);
    }

    @Test
    void getWishlist_defaultsToPage1Size12_returnsToursWithSavedAt_forCurrentPrincipal() throws Exception {
        when(getWishlistUseCase.getWishlist(USER_ID, 1, 12))
                .thenReturn(new PageResult<>(List.of(sampleItem()), 1, 1, 12));

        mockMvc.perform(get("/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tour.id").value(2))
                .andExpect(jsonPath("$.data[0].tour.title").value("Da Nang Tour"))
                .andExpect(jsonPath("$.data[0].tour.slug").value("da-nang-tour"))
                .andExpect(jsonPath("$.data[0].savedAt").exists())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(12));

        verify(getWishlistUseCase).getWishlist(USER_ID, 1, 12);
    }

    @Test
    void getWishlist_withPageAndSizeParams_passesThemThrough() throws Exception {
        when(getWishlistUseCase.getWishlist(USER_ID, 2, 5))
                .thenReturn(new PageResult<>(List.of(), 0, 2, 5));

        mockMvc.perform(get("/wishlist").param("page", "2").param("size", "5"))
                .andExpect(status().isOk());

        verify(getWishlistUseCase).getWishlist(USER_ID, 2, 5);
    }

    @Test
    void getWishlist_sizeAboveMax_returns400_andNeverCallsUseCase() throws Exception {
        mockMvc.perform(get("/wishlist").param("size", "51"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(getWishlistUseCase);
    }
}
