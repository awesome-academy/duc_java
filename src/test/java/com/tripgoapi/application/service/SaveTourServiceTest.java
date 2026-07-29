package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.SaveTourCommand;
import com.tripgoapi.application.port.in.UploadedImage;
import com.tripgoapi.application.port.out.AdminTourRepositoryInterface;
import com.tripgoapi.domain.exception.TourNotFoundException;
import com.tripgoapi.domain.model.TourImage;
import com.tripgoapi.domain.model.TourItineraryDay;
import com.tripgoapi.domain.model.TourStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveTourServiceTest {

    private static final Long TOUR_ID = 7L;

    @Mock
    private AdminTourRepositoryInterface adminTourRepository;
    @Mock
    private ImageUploader imageUploader;

    private SaveTourService service;

    @BeforeEach
    void setUp() {
        service = new SaveTourService(adminTourRepository, imageUploader);
    }

    private SaveTourCommand command(
            String title,
            List<TourItineraryDay> itinerary,
            List<String> keptImageUrls,
            List<UploadedImage> newImages,
            String thumbnailUrl
    ) {
        return new SaveTourCommand(
                title, 1L, 2L, BigDecimal.valueOf(4_990_000), null, 3, 20, "Mô tả",
                false, TourStatus.ACTIVE, itinerary, keptImageUrls, newImages, thumbnailUrl
        );
    }

    @SuppressWarnings("unchecked")
    private List<TourImage> capturedImages() {
        ArgumentCaptor<List<TourImage>> captor = ArgumentCaptor.forClass(List.class);
        verify(adminTourRepository).createTour(any(), any(), anyList(), captor.capture());
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private List<TourItineraryDay> capturedItinerary() {
        ArgumentCaptor<List<TourItineraryDay>> captor = ArgumentCaptor.forClass(List.class);
        verify(adminTourRepository).createTour(any(), any(), captor.capture(), anyList());
        return captor.getValue();
    }

    @Test
    void slugIsSuffixed_whenTheGeneratedOneIsAlreadyTaken() {
        // Two tours can legitimately share a title; the slug column cannot, so the save must not
        // fail — it must pick the next free slug on the admin's behalf.
        when(adminTourRepository.existsBySlug("da-nang-3n2d", null)).thenReturn(true);
        when(adminTourRepository.existsBySlug("da-nang-3n2d-2", null)).thenReturn(false);

        service.createTour(command("Đà Nẵng 3N2Đ", List.of(), List.of(), List.of(), null));

        verify(adminTourRepository).createTour(any(), eq("da-nang-3n2d-2"), anyList(), anyList());
    }

    @Test
    void blankItineraryRowsAreDropped_andRemainingDaysRenumberedContiguously() {
        // The form always submits whatever rows are on screen; deleting "Ngày 2" in the middle
        // must not leave a hole in day_number.
        List<TourItineraryDay> submitted = List.of(
                new TourItineraryDay(1, "Khởi hành", "Bay ra Đà Nẵng"),
                new TourItineraryDay(2, "  ", null),
                new TourItineraryDay(3, "Hội An", "Phố cổ")
        );

        service.createTour(command("Tour A", submitted, List.of(), List.of(), null));

        assertThat(capturedItinerary())
                .extracting(TourItineraryDay::dayNumber, TourItineraryDay::title)
                .containsExactly(
                        tuple(1, "Khởi hành"),
                        tuple(2, "Hội An"));
    }

    @Test
    void newUploadsAreAppendedAfterKeptImages_andDuplicateKeptUrlsCollapse() {
        when(imageUploader.upload(any(), eq("tours"))).thenReturn("/uploads/tours/new.jpg");
        UploadedImage upload = new UploadedImage("photo.jpg", "image/jpeg", new byte[]{1});

        service.createTour(command("Tour A", List.of(),
                // "/a.jpg" twice: a double submit would otherwise insert the same image row twice,
                // and tour_images has no unique constraint to stop it.
                Arrays.asList("/a.jpg", null, "/a.jpg", "  "),
                List.of(upload), null));

        assertThat(capturedImages())
                .extracting(TourImage::imageUrl, TourImage::displayOrder)
                .containsExactly(
                        tuple("/a.jpg", 0),
                        tuple("/uploads/tours/new.jpg", 1));
    }

    @Test
    void thumbnailFallsBackToTheFirstImage_whenThePickedOneWasRemoved() {
        // The admin can delete the image they had marked as thumbnail in the same submit; without
        // a fallback the tour would end up with no thumbnail at all.
        service.createTour(command("Tour A", List.of(), List.of("/a.jpg", "/b.jpg"), List.of(), "/gone.jpg"));

        assertThat(capturedImages())
                .extracting(TourImage::imageUrl, TourImage::thumbnail)
                .containsExactly(
                        tuple("/a.jpg", true),
                        tuple("/b.jpg", false));
    }

    @Test
    void thumbnailHonoursTheAdminsPick_whenItSurvivedTheSubmit() {
        service.createTour(command("Tour A", List.of(), List.of("/a.jpg", "/b.jpg"), List.of(), "/b.jpg"));

        assertThat(capturedImages())
                .extracting(TourImage::imageUrl, TourImage::thumbnail)
                .containsExactly(
                        tuple("/a.jpg", false),
                        tuple("/b.jpg", true));
    }

    @Test
    void updateDeletesOnlyTheFilesThatAreNoLongerReferenced() {
        when(adminTourRepository.findImageUrls(TOUR_ID)).thenReturn(List.of("/a.jpg", "/b.jpg"));
        when(adminTourRepository.updateTour(eq(TOUR_ID), any(), any(), anyList(), anyList())).thenReturn(true);

        service.updateTour(TOUR_ID, command("Tour A", List.of(), List.of("/a.jpg"), List.of(), null));

        verify(imageUploader).deleteAll(List.of("/b.jpg"));
    }

    @Test
    void updateOfAMissingTour_throws_andNeverDeletesTheOldFiles() {
        // A failed save must not take the images with it, or a concurrent delete would leave the
        // surviving tour pointing at files that no longer exist.
        when(adminTourRepository.findImageUrls(TOUR_ID)).thenReturn(List.of("/a.jpg"));
        when(adminTourRepository.updateTour(eq(TOUR_ID), any(), any(), anyList(), anyList())).thenReturn(false);

        assertThatThrownBy(() -> service.updateTour(TOUR_ID,
                command("Tour A", List.of(), List.of(), List.of(), null)))
                .isInstanceOf(TourNotFoundException.class);

        verify(imageUploader, never()).deleteAll(anyList());
    }
}
