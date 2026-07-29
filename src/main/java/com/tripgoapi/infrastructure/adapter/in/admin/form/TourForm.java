package com.tripgoapi.infrastructure.adapter.in.admin.form;

import com.tripgoapi.domain.model.AdminTourDetail;
import com.tripgoapi.domain.model.TourImage;
import com.tripgoapi.domain.model.TourItineraryDay;
import com.tripgoapi.domain.model.TourStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Backing bean for "Thêm / Sửa tour". Kept separate from {@code SaveTourCommand} because it also
 * has to survive a failed submit: on a validation error the same object is handed straight back to
 * the template, raw values and all.
 */
@Getter
@Setter
@NoArgsConstructor
public class TourForm {

    private Long id;

    @NotBlank(message = "Tên tour không được để trống")
    @Size(max = 255, message = "Tên tour tối đa 255 ký tự")
    private String title;

    @NotNull(message = "Vui lòng chọn điểm đến")
    private Long destinationId;

    private Long categoryId;

    @NotNull(message = "Giá tour không được để trống")
    @DecimalMin(value = "0", inclusive = false, message = "Giá tour phải lớn hơn 0")
    private BigDecimal price;

    @DecimalMin(value = "0", inclusive = false, message = "Giá khuyến mãi phải lớn hơn 0")
    private BigDecimal discountPrice;

    @NotNull(message = "Thời lượng không được để trống")
    @Min(value = 1, message = "Thời lượng phải từ 1 ngày")
    @Max(value = 365, message = "Thời lượng tối đa 365 ngày")
    private Integer durationDays;

    @Min(value = 1, message = "Số khách tối đa phải từ 1")
    @Max(value = 1000, message = "Số khách tối đa 1000")
    private Integer maxGuests;

    @Size(max = 5000, message = "Mô tả tối đa 5000 ký tự")
    private String description;

    private boolean featured;

    @NotNull(message = "Vui lòng chọn trạng thái")
    private TourStatus status = TourStatus.ACTIVE;

    @Valid
    private List<ItineraryDayForm> itinerary = new ArrayList<>();

    /** Images already on the tour that the admin did not remove, in display order. */
    private List<String> keptImageUrls = new ArrayList<>();

    private String thumbnailUrl;

    private List<MultipartFile> newImages = new ArrayList<>();

    public static TourForm from(AdminTourDetail tour) {
        TourForm form = new TourForm();
        form.id = tour.id();
        form.title = tour.title();
        form.destinationId = tour.destinationId();
        form.categoryId = tour.categoryId();
        form.price = tour.price();
        form.discountPrice = tour.discountPrice();
        form.durationDays = tour.durationDays();
        form.maxGuests = tour.maxGuests();
        form.description = tour.description();
        form.featured = tour.featured();
        form.status = tour.status();

        form.itinerary = tour.itinerary().stream().map(TourForm::toDayForm).collect(toMutableList());
        form.keptImageUrls = tour.images().stream().map(TourImage::imageUrl).collect(toMutableList());
        form.thumbnailUrl = tour.images().stream()
                .filter(TourImage::thumbnail)
                .map(TourImage::imageUrl)
                .findFirst()
                .orElse(null);
        return form;
    }

    /** Domain view of the itinerary rows; day numbers are assigned by the application layer. */
    public List<TourItineraryDay> toItineraryDays() {
        List<TourItineraryDay> days = new ArrayList<>(itinerary.size());
        for (int i = 0; i < itinerary.size(); i++) {
            ItineraryDayForm day = itinerary.get(i);
            days.add(new TourItineraryDay(i + 1, day.getTitle(), day.getDescription()));
        }
        return days;
    }

    private static ItineraryDayForm toDayForm(TourItineraryDay day) {
        ItineraryDayForm form = new ItineraryDayForm();
        form.setTitle(day.title());
        form.setDescription(day.description());
        return form;
    }

    /**
     * Spring's data binder auto-grows indexed properties, which it can only do on a mutable list —
     * {@code Stream.toList()} returns an immutable one and would break re-binding after an error.
     */
    private static <T> Collector<T, ?, List<T>> toMutableList() {
        return Collectors.toCollection(ArrayList::new);
    }
}
