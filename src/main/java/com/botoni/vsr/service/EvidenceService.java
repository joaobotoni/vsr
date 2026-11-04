package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Evidence;
import com.botoni.vsr.database.entity.Item;
import com.botoni.vsr.database.entity.Room;
import com.botoni.vsr.database.repository.EvidenceRepository;
import com.botoni.vsr.dto.request.EvidenceRequest;
import com.botoni.vsr.dto.response.EvidenceFile;
import com.botoni.vsr.dto.response.EvidenceResponse;
import com.botoni.vsr.exception.custom.InspectionException;
import com.botoni.vsr.exception.custom.PhotoException;
import com.botoni.vsr.exception.enums.problem.InspectionProblem;
import com.botoni.vsr.exception.enums.problem.PhotoProblem;
import com.botoni.vsr.mapper.EvidenceMapper;
import com.botoni.vsr.vo.Photo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Clock;
import java.util.UUID;

/**
 * Evidências (fotos) dos ambientes das vistorias do usuário. O arquivo é gravado antes do registro; se o registro for
 * recusado (vistoria finalizada, item de outro ambiente), a transação desfeita apaga o arquivo.
 */
@Service
@RequiredArgsConstructor
public class EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final RoomService roomService;
    private final ItemService itemService;
    private final StorageService storageService;
    private final EvidenceMapper evidenceMapper;
    private final Clock clock;

    @Transactional
    public EvidenceResponse upload(UUID user, UUID room, EvidenceRequest request) {
        Room found = room(user, room);
        Item item = item(user, request.item());
        Photo photo = photo(request.file());
        String path = path(photo);
        Evidence evidence = save(found, item, path, photo, request.description());
        return show(evidence);
    }

    @Transactional(readOnly = true)
    public EvidenceFile download(UUID user, UUID evidence) {
        Evidence found = owned(user, evidence);
        byte[] content = load(found);
        return file(found, content);
    }

    @Transactional
    public void remove(UUID user, UUID evidence) {
        Evidence found = owned(user, evidence);
        discard(found);
    }

    private Room room(UUID user, UUID room) {
        return roomService.find(user, room);
    }

    private String path(Photo photo) {
        return storageService.save(photo);
    }

    private Evidence save(Room room, Item item, String path, Photo photo, String description) {
        return evidenceRepository.saveAndFlush(create(room, item, path, photo, description));
    }

    private Evidence create(Room room, Item item, String path, Photo photo, String description) {
        return evidenceMapper.entity(room, item, path, photo, clock.instant(), description);
    }

    private EvidenceResponse show(Evidence evidence) {
        return evidenceMapper.response(evidence);
    }

    private byte[] load(Evidence evidence) {
        return storageService.load(evidence.getPath());
    }

    private static EvidenceFile file(Evidence evidence, byte[] content) {
        return new EvidenceFile(evidence.getContentType(), content);
    }

    private void discard(Evidence evidence) {
        evidenceRepository.remove(evidence.getId());
    }

    private Item item(UUID user, UUID item) {
        if (item == null) {
            return null;
        }
        return itemService.find(user, item);
    }

    private static Photo photo(MultipartFile file) {
        try {
            return Photo.of(file.getBytes());
        } catch (IOException exception) {
            throw new PhotoException(PhotoProblem.UNREADABLE, exception);
        }
    }

    private Evidence owned(UUID user, UUID evidence) {
        return evidenceRepository.findOwned(user, evidence)
                .orElseThrow(() -> new InspectionException(InspectionProblem.EVIDENCE_NOT_FOUND));
    }
}
