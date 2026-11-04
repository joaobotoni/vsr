package com.botoni.vsr.mapper;

import com.botoni.vsr.database.entity.Company;
import com.botoni.vsr.database.entity.Contact;
import com.botoni.vsr.database.entity.Individual;
import com.botoni.vsr.database.entity.Participant;
import com.botoni.vsr.database.entity.Person;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.request.CompanyRequest;
import com.botoni.vsr.dto.request.IndividualRequest;
import com.botoni.vsr.dto.response.ParticipantResponse;
import com.botoni.vsr.dto.response.PersonResponse;
import com.botoni.vsr.dto.response.PersonSummaryResponse;
import org.mapstruct.Mapper;

/**
 * Pessoa física e jurídica dividem a resposta: o documento e o nome fantasia vêm do subtipo.
 */
@Mapper(config = MapperConfiguration.class)
public interface PersonMapper {

    default Individual individual(User owner, IndividualRequest request) {
        return new Individual(owner, request.name(), request.cpf());
    }

    default Company company(User owner, CompanyRequest request) {
        return new Company(owner, request.name(), request.tradeName(), request.cnpj());
    }

    default PersonResponse response(Person person, Contact contact) {
        return new PersonResponse(person.getUuid(), person.getType(), person.getName(), tradeName(person), document(person),
                contact == null ? null : contact.getPhone(), contact == null ? null : contact.getEmail());
    }

    default PersonSummaryResponse summary(Person person) {
        return new PersonSummaryResponse(person.getUuid(), person.getType(), person.getName(), document(person));
    }

    default ParticipantResponse participant(Participant participant) {
        return new ParticipantResponse(summary(participant.getPerson()), participant.getRole());
    }

    private static String document(Person person) {
        if (person instanceof Individual individual) {
            return individual.getCpf().value();
        }
        return ((Company) person).getCnpj().value();
    }

    private static String tradeName(Person person) {
        return person instanceof Company company ? company.getTradeName() : null;
    }
}
