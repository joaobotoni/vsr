package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Contact;
import com.botoni.vsr.database.entity.Person;
import com.botoni.vsr.database.repository.ContactRepository;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Phone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Contato das pessoas (telefone, e-mail ou os dois). Pessoa sem nenhum dos dois fica sem linha de contato.
 */
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    @Transactional
    public Contact save(Person person, Phone phone, Email email) {
        Contact contact = current(person);
        Contact updated = contact.update(phone, email);
        return keep(updated);
    }

    @Transactional(readOnly = true)
    public Contact lookup(Person person) {
        return contactRepository.findById(person.getId()).orElse(null);
    }

    @Transactional(readOnly = true)
    public Map<Integer, Contact> index(List<Person> people) {
        return contactRepository.findAllById(people.stream().map(Person::getId).toList()).stream()
                .collect(Collectors.toMap(Contact::getId, Function.identity()));
    }

    private Contact current(Person person) {
        return contactRepository.findById(person.getId()).orElseGet(() -> new Contact(person));
    }

    private Contact keep(Contact contact) {
        if (empty(contact)) {
            return null;
        }
        return contactRepository.saveAndFlush(contact);
    }

    private static boolean empty(Contact contact) {
        return contact.getPhone() == null && contact.getEmail() == null;
    }
}
