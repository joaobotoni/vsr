package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Company;
import com.botoni.vsr.database.entity.Contact;
import com.botoni.vsr.database.entity.Individual;
import com.botoni.vsr.database.entity.Person;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.PersonRepository;
import com.botoni.vsr.dto.request.CompanyRequest;
import com.botoni.vsr.dto.request.IndividualRequest;
import com.botoni.vsr.dto.request.PageQuery;
import com.botoni.vsr.dto.request.PersonEditRequest;
import com.botoni.vsr.dto.response.PageResponse;
import com.botoni.vsr.dto.response.PersonResponse;
import com.botoni.vsr.exception.custom.PersonException;
import com.botoni.vsr.exception.enums.problem.PersonProblem;
import com.botoni.vsr.mapper.PersonMapper;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Phone;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Cadastro privado de pessoas do usuário (proprietários, inquilinos). Cada usuário só vê e altera o próprio
 * cadastro; a pessoa de outro usuário responde 404, e o CPF ou CNPJ cadastrado por outro não é revelado.
 */
@Service
@RequiredArgsConstructor
public class PersonService {

    private static final Sort BY_NAME = Sort.by(Sort.Direction.ASC, "name");

    private final PersonRepository personRepository;
    private final UserService userService;
    private final ContactService contactService;
    private final PersonMapper personMapper;

    @Transactional
    public PersonResponse individual(UUID user, IndividualRequest request) {
        User owner = owner(user);
        Person person = save(natural(owner, request));
        Contact contact = contact(person, request.phone(), request.email());
        return show(person, contact);
    }

    @Transactional
    public PersonResponse company(UUID user, CompanyRequest request) {
        User owner = owner(user);
        Person person = save(legal(owner, request));
        Contact contact = contact(person, request.phone(), request.email());
        return show(person, contact);
    }

    @Transactional(readOnly = true)
    public PersonResponse view(UUID user, UUID person) {
        Person found = owned(user, person);
        Contact contact = lookup(found);
        return show(found, contact);
    }

    @Transactional(readOnly = true)
    public PageResponse<PersonResponse> list(UUID user, PageQuery query) {
        Page<Person> page = page(user, query);
        Map<Integer, Contact> contacts = index(page);
        return results(page, contacts);
    }

    @Transactional
    public PersonResponse edit(UUID user, UUID person, PersonEditRequest request) {
        Person found = owned(user, person);
        amend(found, request);
        store(found);
        Contact contact = contact(found, request.phone(), request.email());
        return show(found, contact);
    }

    @Transactional(readOnly = true)
    public Person find(UUID user, UUID person) {
        return owned(user, person);
    }

    private User owner(UUID user) {
        return userService.find(user);
    }

    private Individual natural(User owner, IndividualRequest request) {
        return personMapper.individual(owner, request);
    }

    private Company legal(User owner, CompanyRequest request) {
        return personMapper.company(owner, request);
    }

    private Person save(Person person) {
        return personRepository.saveAndFlush(person);
    }

    private Contact contact(Person person, Phone phone, Email email) {
        return contactService.save(person, phone, email);
    }

    private PersonResponse show(Person person, Contact contact) {
        return personMapper.response(person, contact);
    }

    private Contact lookup(Person person) {
        return contactService.lookup(person);
    }

    private Page<Person> page(UUID user, PageQuery query) {
        return personRepository.findAllOwned(user, pageable(query));
    }

    private static Pageable pageable(PageQuery query) {
        return PageRequest.of(query.page(), query.size(), BY_NAME);
    }

    private Map<Integer, Contact> index(Page<Person> page) {
        return contactService.index(page.getContent());
    }

    private PageResponse<PersonResponse> results(Page<Person> page, Map<Integer, Contact> contacts) {
        return PageResponse.of(page.map(person -> personMapper.response(person, contacts.get(person.getId()))));
    }

    private static void amend(Person person, PersonEditRequest request) {
        person.rename(request.name());
        person.retitle(request.tradeName());
    }

    private void store(Person person) {
        personRepository.saveAndFlush(person);
    }

    private Person owned(UUID user, UUID person) {
        return personRepository.findOwned(user, person)
                .orElseThrow(() -> new PersonException(PersonProblem.NOT_FOUND));
    }
}
