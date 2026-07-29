package com.tripgoapi.infrastructure.adapter.in.admin.form;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One "Ngày N" row of the itinerary editor. */
@Getter
@Setter
@NoArgsConstructor
public class ItineraryDayForm {

    @Size(max = 255, message = "Tiêu đề ngày tối đa 255 ký tự")
    private String title;

    @Size(max = 2000, message = "Mô tả ngày tối đa 2000 ký tự")
    private String description;
}
