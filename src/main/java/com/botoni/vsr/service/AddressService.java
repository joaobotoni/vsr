package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Address;
import com.botoni.vsr.database.repository.AddressRepository;
import com.botoni.vsr.dto.request.AddressRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    @Transactional
    public Address locate(AddressRequest request) {
        Integer address = register(request);
        return reference(address);
    }

    private Integer register(AddressRequest request) {
        return addressRepository.register(request.state(), request.city(), request.district(), request.cep().value(),
                request.street(), request.number(), request.complement());
    }

    private Address reference(Integer address) {
        return addressRepository.getReferenceById(address);
    }
}
