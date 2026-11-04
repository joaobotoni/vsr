package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Company;
import com.botoni.vsr.database.entity.Individual;
import com.botoni.vsr.database.entity.Membership;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.enums.MembershipRole;
import com.botoni.vsr.database.repository.MembershipRepository;
import com.botoni.vsr.database.repository.UserRepository;
import com.botoni.vsr.dto.request.MemberRequest;
import com.botoni.vsr.dto.response.MemberResponse;
import com.botoni.vsr.exception.custom.CompanyException;
import com.botoni.vsr.exception.enums.problem.CompanyProblem;
import com.botoni.vsr.vo.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Membros das empresas. Quem não é membro não vê a empresa (404); membro que não administra não gerencia membros
 * (403). A inclusão por e-mail responde igual exista a conta ou não, para não revelar quais e-mails têm conta.
 */
@Service
@RequiredArgsConstructor
public class MembershipService {

    /** Teto das listas curtas, que não são paginadas. */
    private static final Limit LIST = Limit.of(200);

    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Membership verify(UUID user, UUID company) {
        return active(user, company);
    }

    @Transactional(readOnly = true)
    public List<Membership> companies(UUID user) {
        return membershipRepository.findAllActive(user, LIST);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> list(UUID user, UUID company) {
        Membership membership = active(user, company);
        return members(membership);
    }

    @Transactional
    public void open(User user, Company company, MembershipRole role) {
        bind(user.getPerson(), company, role);
    }

    @Transactional
    public void link(UUID user, UUID company, MemberRequest request) {
        Membership membership = active(user, company);
        admin(membership);
        bind(member(request.email()), membership.getCompany(), request.role());
    }

    @Transactional
    public void unlink(UUID user, UUID company, UUID member) {
        Membership membership = active(user, company);
        removable(user, member, membership);
        release(person(member), membership.getCompany());
    }

    private List<MemberResponse> members(Membership membership) {
        return membershipRepository.findMembers(membership.getCompany().getId(), LIST);
    }

    private Individual member(Email email) {
        return userRepository.findWithPersonByEmail(email).map(User::getPerson).orElse(null);
    }

    private Individual person(UUID member) {
        return userRepository.findWithPersonByUuid(member).map(User::getPerson).orElse(null);
    }

    private Membership active(UUID user, UUID company) {
        return membershipRepository.findActive(user, company)
                .orElseThrow(() -> new CompanyException(CompanyProblem.NOT_FOUND));
    }

    private static void admin(Membership membership) {
        if (!membership.getRole().isAdmin()) {
            throw new CompanyException(CompanyProblem.ADMIN_REQUIRED);
        }
    }

    /** O membro sai sozinho; para remover outro, é preciso administrar. */
    private static void removable(UUID user, UUID member, Membership membership) {
        if (user.equals(member)) {
            return;
        }
        admin(membership);
    }

    /** Usuário inexistente ou que não é membro: nada muda, e a resposta é a mesma. */
    private void release(Individual person, Company company) {
        if (person == null) {
            return;
        }
        membershipRepository.unlink(person.getId(), company.getId());
    }

    private void bind(Individual person, Company company, MembershipRole role) {
        if (person == null) {
            return;
        }
        membershipRepository.link(person.getId(), company.getId(), role.getValue());
    }
}
