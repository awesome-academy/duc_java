package com.tripgoapi.infrastructure.adapter.in.admin.form;

import com.tripgoapi.domain.model.Destination;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class DestinationForm {

    private Long id;

    @NotBlank(message = "Tên điểm đến không được để trống")
    @Size(max = 255, message = "Tên điểm đến tối đa 255 ký tự")
    private String name;

    @Size(max = 5000, message = "Mô tả tối đa 5000 ký tự")
    private String description;

    private String keptImageUrl;

    private MultipartFile newImage;

    public static DestinationForm from(Destination destination) {
        DestinationForm form = new DestinationForm();
        form.id = destination.id();
        form.name = destination.name();
        form.description = destination.description();
        form.keptImageUrl = destination.imageUrl();
        return form;
    }
}
