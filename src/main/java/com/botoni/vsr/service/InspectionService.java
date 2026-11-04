package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Inspection;
import com.botoni.vsr.database.entity.Property;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.InspectionRepository;
import com.botoni.vsr.dto.request.InspectionEditRequest;
import com.botoni.vsr.dto.request.InspectionQuery;
import com.botoni.vsr.dto.request.InspectionRequest;
import com.botoni.vsr.dto.request.PageQuery;
import com.botoni.vsr.dto.response.InspectionDetailResponse;
import com.botoni.vsr.dto.response.InspectionResponse;
import com.botoni.vsr.dto.response.PageResponse;
import com.botoni.vsr.exception.custom.InspectionException;
import com.botoni.vsr.exception.enums.problem.InspectionProblem;
import com.botoni.vsr.mapper.InspectionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Vistorias do usuário responsável. Vistoria de outro usuário, ou removida, responde 404. As regras de estado
 * (finalizada não muda, entrada válida, uma em andamento por imóvel) ficam no banco.
 */
@Service
@RequiredArgsConstructor
public class InspectionService {

    private static final Sort NEWEST = Sort.by(Sort.Direction.DESC, "id");

    private final InspectionRepository inspectionRepository;
    private final PropertyService propertyService;
    private final UserService userService;
    private final InspectionMapper inspectionMapper;

    @Transactional
    public InspectionResponse start(UUID user, InspectionRequest request) {
        User responsible = responsible(user);
        Property property = property(user, request.property());
        Inspection entry = entry(user, request.entry());
        Inspection inspection = save(responsible, property, entry, request);
        copy(inspection);
        return show(inspection);
    }

    @Transactional(readOnly = true)
    public InspectionDetailResponse view(UUID user, UUID inspection) {
        Inspection found = owned(user, inspection);
        return detail(found);
    }

    @Transactional(readOnly = true)
    public PageResponse<InspectionResponse> list(UUID user, InspectionQuery query, PageQuery page) {
        Page<Inspection> found = page(user, query, page);
        return results(found);
    }

    @Transactional
    public InspectionResponse edit(UUID user, UUID inspection, InspectionEditRequest request) {
        Inspection found = owned(user, inspection);
        amend(found, request);
        store(found);
        return show(found);
    }

    @Transactional
    public InspectionResponse finish(UUID user, UUID inspection) {
        Inspection found = owned(user, inspection);
        conclude(found);
        Inspection finished = owned(user, inspection);
        return show(finished);
    }

    @Transactional
    public void remove(UUID user, UUID inspection) {
        Inspection found = owned(user, inspection);
        discard(found);
    }

    @Transactional(readOnly = true)
    public Inspection find(UUID user, UUID inspection) {
        return owned(user, inspection);
    }

    private User responsible(UUID user) {
        return userService.find(user);
    }

    private Property property(UUID user, UUID property) {
        return propertyService.find(user, property);
    }

    private Inspection save(User user, Property property, Inspection entry, InspectionRequest request) {
        return inspectionRepository.save(create(user, property, entry, request));
    }

    private Inspection create(User user, Property property, Inspection entry, InspectionRequest request) {
        return inspectionMapper.entity(user, property, entry, request);
    }

    private void copy(Inspection inspection) {
        inspectionRepository.copy(inspection.getId());
    }

    private InspectionResponse show(Inspection inspection) {
        return inspectionMapper.response(inspection);
    }

    private InspectionDetailResponse detail(Inspection inspection) {
        return inspectionMapper.detail(inspection);
    }

    private Page<Inspection> page(UUID user, InspectionQuery query, PageQuery page) {
        return inspectionRepository.findAllOwned(user, query.property(), query.state(), pageable(page));
    }

    private static Pageable pageable(PageQuery page) {
        return PageRequest.of(page.page(), page.size(), NEWEST);
    }

    private PageResponse<InspectionResponse> results(Page<Inspection> page) {
        return PageResponse.of(page.map(inspectionMapper::response));
    }

    private void amend(Inspection inspection, InspectionEditRequest request) {
        inspectionMapper.amend(request, inspection);
    }

    private void store(Inspection inspection) {
        inspectionRepository.saveAndFlush(inspection);
    }

    private void conclude(Inspection inspection) {
        inspectionRepository.finish(inspection.getId());
    }

    private void discard(Inspection inspection) {
        inspectionRepository.remove(inspection.getId());
    }

    private Inspection entry(UUID user, UUID entry) {
        if (entry == null) {
            return null;
        }
        return inspectionRepository.findOwned(user, entry)
                .orElseThrow(() -> new InspectionException(InspectionProblem.ENTRY_NOT_FOUND));
    }

    private Inspection owned(UUID user, UUID inspection) {
        return inspectionRepository.findOwned(user, inspection)
                .orElseThrow(() -> new InspectionException(InspectionProblem.NOT_FOUND));
    }
}
