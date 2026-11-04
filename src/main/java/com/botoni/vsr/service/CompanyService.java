package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Company;
import com.botoni.vsr.database.entity.Membership;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.PersonRepository;
import com.botoni.vsr.dto.request.CompanyAccountRequest;
import com.botoni.vsr.dto.response.CompanyResponse;
import com.botoni.vsr.exception.custom.CompanyException;
import com.botoni.vsr.exception.enums.problem.CompanyProblem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Empresas da plataforma: cadastro compartilhado (CNPJ único), acessado só pelos membros. Quem cadastra entra como
 * administrador.
 */
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final PersonRepository personRepository;
    private final UserService userService;
    private final MembershipService membershipService;

    @Transactional
    public CompanyResponse open(UUID user, CompanyAccountRequest request) {
        administrator(request);
        User owner = owner(user);
        Company company = save(request);
        membership(owner, company, request);
        return show(company, request);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> list(UUID user) {
        List<Membership> memberships = memberships(user);
        return results(memberships);
    }

    @Transactional(readOnly = true)
    public CompanyResponse view(UUID user, UUID company) {
        Membership membership = verify(user, company);
        return summary(membership);
    }

    @Transactional(readOnly = true)
    public Company find(UUID user, UUID company) {
        return verify(user, company).getCompany();
    }

    private User owner(UUID user) {
        return userService.find(user);
    }

    private Company save(CompanyAccountRequest request) {
        return personRepository.saveAndFlush(new Company(null, request.name(), request.tradeName(), request.cnpj()));
    }

    private void membership(User owner, Company company, CompanyAccountRequest request) {
        membershipService.open(owner, company, request.role());
    }

    private static CompanyResponse show(Company company, CompanyAccountRequest request) {
        return new CompanyResponse(company.getUuid(), company.getName(), company.getTradeName(), company.getCnpj().value(),
                request.role());
    }

    private List<Membership> memberships(UUID user) {
        return membershipService.companies(user);
    }

    private static List<CompanyResponse> results(List<Membership> memberships) {
        return memberships.stream().map(CompanyService::summary).toList();
    }

    private Membership verify(UUID user, UUID company) {
        return membershipService.verify(user, company);
    }

    private static CompanyResponse summary(Membership membership) {
        Company company = membership.getCompany();
        return new CompanyResponse(company.getUuid(), company.getName(), company.getTradeName(), company.getCnpj().value(),
                membership.getRole());
    }

    private static void administrator(CompanyAccountRequest request) {
        if (!request.role().isAdmin()) {
            throw new CompanyException(CompanyProblem.ADMIN_ROLE_REQUIRED);
        }
    }
}
