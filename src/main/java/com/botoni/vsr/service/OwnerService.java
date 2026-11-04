package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Ownership;
import com.botoni.vsr.database.entity.Person;
import com.botoni.vsr.database.entity.Property;
import com.botoni.vsr.database.repository.OwnershipRepository;
import com.botoni.vsr.dto.request.OwnerRequest;
import com.botoni.vsr.dto.response.PersonSummaryResponse;
import com.botoni.vsr.mapper.PersonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Proprietários dos imóveis do usuário, escolhidos no cadastro de pessoas dele.
 */
@Service
@RequiredArgsConstructor
public class OwnerService {

    /** Teto das listas curtas, que não são paginadas. */
    private static final Limit LIST = Limit.of(200);

    private final OwnershipRepository ownershipRepository;
    private final PropertyService propertyService;
    private final PersonService personService;
    private final PersonMapper personMapper;

    @Transactional(readOnly = true)
    public List<PersonSummaryResponse> list(UUID user, UUID property) {
        Property found = property(user, property);
        List<Ownership> owners = owners(found);
        return show(owners);
    }

    @Transactional
    public void link(UUID user, UUID property, OwnerRequest request) {
        Property found = property(user, property);
        Person person = person(user, request.person());
        bind(found, person);
    }

    @Transactional
    public void unlink(UUID user, UUID property, UUID person) {
        Property found = property(user, property);
        Person owner = person(user, person);
        release(found, owner);
    }

    private Property property(UUID user, UUID property) {
        return propertyService.find(user, property);
    }

    private List<Ownership> owners(Property property) {
        return ownershipRepository.findActive(property.getId(), LIST);
    }

    private List<PersonSummaryResponse> show(List<Ownership> owners) {
        return owners.stream().map(owner -> personMapper.summary(owner.getPerson())).toList();
    }

    private Person person(UUID user, UUID person) {
        return personService.find(user, person);
    }

    private void bind(Property property, Person person) {
        ownershipRepository.link(property.getId(), person.getId());
    }

    private void release(Property property, Person person) {
        ownershipRepository.unlink(property.getId(), person.getId());
    }
}
