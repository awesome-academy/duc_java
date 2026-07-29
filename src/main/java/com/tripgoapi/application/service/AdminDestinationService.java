package com.tripgoapi.application.service;

import com.tripgoapi.application.port.in.CreateDestinationUseCase;
import com.tripgoapi.application.port.in.DeleteDestinationUseCase;
import com.tripgoapi.application.port.in.GetAdminDestinationsUseCase;
import com.tripgoapi.application.port.in.SaveDestinationCommand;
import com.tripgoapi.application.port.in.UpdateDestinationUseCase;
import com.tripgoapi.application.port.in.UploadedImage;
import com.tripgoapi.application.port.out.AdminDestinationRepositoryInterface;
import com.tripgoapi.domain.exception.DestinationInUseException;
import com.tripgoapi.domain.exception.DestinationNotFoundException;
import com.tripgoapi.domain.model.Destination;
import com.tripgoapi.domain.model.Slug;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDestinationService implements
        GetAdminDestinationsUseCase,
        CreateDestinationUseCase,
        UpdateDestinationUseCase,
        DeleteDestinationUseCase {

    private static final String IMAGE_FOLDER = "destinations";
    private static final int MAX_SLUG_ATTEMPTS = 50;

    private final AdminDestinationRepositoryInterface adminDestinationRepository;
    private final ImageUploader imageUploader;

    @Override
    @Transactional(readOnly = true)
    public List<Destination> getDestinations() {
        return adminDestinationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Destination getDestination(Long id) {
        return adminDestinationRepository.findById(id)
                .orElseThrow(() -> new DestinationNotFoundException(id));
    }

    @Override
    @Transactional
    public Long createDestination(SaveDestinationCommand command) {
        return adminDestinationRepository.createDestination(new Destination(
                null, command.name(), uniqueSlug(command.name(), null), command.description(), resolveImageUrl(command)));
    }

    @Override
    @Transactional
    public void updateDestination(Long id, SaveDestinationCommand command) {
        Destination existing = adminDestinationRepository.findById(id)
                .orElseThrow(() -> new DestinationNotFoundException(id));

        String imageUrl = resolveImageUrl(command);

        if (!adminDestinationRepository.updateDestination(new Destination(
                id, command.name(), uniqueSlug(command.name(), id), command.description(), imageUrl))) {
            throw new DestinationNotFoundException(id);
        }

        // ImageUploader.deleteAll defers to after this transaction commits, so a later rollback
        // can never leave the destination pointing at an image file that was already deleted.
        if (existing.imageUrl() != null && !existing.imageUrl().equals(imageUrl)) {
            imageUploader.deleteAll(List.of(existing.imageUrl()));
        }
    }

    @Override
    @Transactional
    public void deleteDestination(Long id) {
        Destination destination = adminDestinationRepository.findById(id)
                .orElseThrow(() -> new DestinationNotFoundException(id));

        // tours.destination_id is nullable, so the FK would not stop this delete — it would just
        // silently strand every tour of that destination with an empty "Điểm đến" column.
        long tourCount = adminDestinationRepository.countTours(id);
        if (tourCount > 0) {
            throw new DestinationInUseException(tourCount);
        }

        if (!adminDestinationRepository.deleteDestination(id)) {
            throw new DestinationNotFoundException(id);
        }

        // Deferred to after commit, same reasoning as updateDestination above.
        if (destination.imageUrl() != null) {
            imageUploader.deleteAll(List.of(destination.imageUrl()));
        }
    }

    private String resolveImageUrl(SaveDestinationCommand command) {
        UploadedImage newImage = command.newImage();
        if (newImage != null && !newImage.isEmpty()) {
            return imageUploader.upload(newImage, IMAGE_FOLDER);
        }
        return command.keptImageUrl() == null || command.keptImageUrl().isBlank() ? null : command.keptImageUrl();
    }

    private String uniqueSlug(String name, Long excludeDestinationId) {
        String base = Slug.from(name);
        if (base == null) {
            base = "diem-den";
        }

        String candidate = base;
        for (int suffix = 2; suffix < MAX_SLUG_ATTEMPTS; suffix++) {
            if (!adminDestinationRepository.existsBySlug(candidate, excludeDestinationId)) {
                return candidate;
            }
            candidate = base + "-" + suffix;
        }
        return base + "-" + System.currentTimeMillis();
    }
}
