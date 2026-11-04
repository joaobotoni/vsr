package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Address;
import com.botoni.vsr.database.entity.Company;
import com.botoni.vsr.database.entity.Property;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.PropertyRepository;
import com.botoni.vsr.dto.request.AddressRequest;
import com.botoni.vsr.dto.request.PageQuery;
import com.botoni.vsr.dto.request.PropertyEditRequest;
import com.botoni.vsr.dto.request.PropertyRequest;
import com.botoni.vsr.dto.response.PageResponse;
import com.botoni.vsr.dto.response.PropertyResponse;
import com.botoni.vsr.exception.custom.PropertyException;
import com.botoni.vsr.exception.enums.problem.PropertyProblem;
import com.botoni.vsr.mapper.PropertyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Imóveis do usuário autenticado. Todo acesso passa pelo dono: imóvel de outro usuário responde 404, como se não existisse.
 */
@Service
@RequiredArgsConstructor
public class PropertyService {

    private static final Sort NEWEST = Sort.by(Sort.Direction.DESC, "id");

    private final PropertyRepository propertyRepository;
    private final UserService userService;
    private final AddressService addressService;
    private final CompanyService companyService;
    private final PropertyMapper propertyMapper;

    @Transactional
    public PropertyResponse add(UUID user, PropertyRequest request) {
        User owner = owner(user);
        Company company = company(user, request.company());
        Address address = locate(request.address());
        Property property = save(owner, company, address, request);
        return show(property);
    }

    @Transactional(readOnly = true)
    public PropertyResponse view(UUID user, UUID property) {
        Property found = owned(user, property);
        return show(found);
    }

    @Transactional(readOnly = true)
    public PageResponse<PropertyResponse> list(UUID user, PageQuery query) {
        Page<Property> page = page(user, query);
        return results(page);
    }

    @Transactional
    public PropertyResponse edit(UUID user, UUID property, PropertyEditRequest request) {
        Property found = owned(user, property);
        amend(found, request);
        store(found);
        return show(found);
    }

    @Transactional
    public void remove(UUID user, UUID property) {
        Property found = owned(user, property);
        discard(found);
    }

    @Transactional(readOnly = true)
    public Property find(UUID user, UUID property) {
        return owned(user, property);
    }

    private User owner(UUID user) {
        return userService.find(user);
    }

    private Address locate(AddressRequest request) {
        return addressService.locate(request);
    }

    private Property save(User user, Company company, Address address, PropertyRequest request) {
        return propertyRepository.save(create(user, company, address, request));
    }

    private Property create(User user, Company company, Address address, PropertyRequest request) {
        return propertyMapper.entity(user, company, address, request);
    }

    private PropertyResponse show(Property property) {
        return propertyMapper.response(property);
    }

    private Page<Property> page(UUID user, PageQuery query) {
        return propertyRepository.findAllOwned(user, pageable(query));
    }

    private static Pageable pageable(PageQuery query) {
        return PageRequest.of(query.page(), query.size(), NEWEST);
    }

    private PageResponse<PropertyResponse> results(Page<Property> page) {
        return PageResponse.of(page.map(propertyMapper::response));
    }

    private void amend(Property property, PropertyEditRequest request) {
        propertyMapper.amend(request, property);
    }

    private void store(Property property) {
        propertyRepository.saveAndFlush(property);
    }

    private void discard(Property property) {
        propertyRepository.remove(property.getId());
    }

    /** Sem empresa, o imóvel é do usuário; com empresa, ele precisa ser membro dela. */
    private Company company(UUID user, UUID company) {
        if (company == null) {
            return null;
        }
        return companyService.find(user, company);
    }

    private Property owned(UUID user, UUID property) {
        return propertyRepository.findOwned(user, property)
                .orElseThrow(() -> new PropertyException(PropertyProblem.NOT_FOUND));
    }
}
