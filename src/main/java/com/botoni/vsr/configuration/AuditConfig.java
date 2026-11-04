package com.botoni.vsr.configuration;

import com.botoni.vsr.principal.Principal;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Informa ao banco, em toda transação de escrita, quem é o usuário autenticado ({@code vsr.usuario}, local à
 * transação). Os triggers de auditoria gravam esse usuário em {@code auditoria.registro}. Sem usuário autenticado
 * (cadastro, login, tarefas periódicas), o registro fica sem usuário.
 */
@Configuration
public class AuditConfig {

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new AuditingTransactionManager(entityManagerFactory);
    }

    static final class AuditingTransactionManager extends JpaTransactionManager {

        private static final String USER = "select set_config('vsr.usuario', :user, true)";

        AuditingTransactionManager(EntityManagerFactory entityManagerFactory) {
            super(entityManagerFactory);
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            super.doBegin(transaction, definition);
            if (definition.isReadOnly()) {
                return;
            }
            String user = user();
            if (user == null) {
                return;
            }
            EntityManagerHolder holder = (EntityManagerHolder) TransactionSynchronizationManager.getResource(obtainEntityManagerFactory());
            holder.getEntityManager().createNativeQuery(USER).setParameter("user", user).getSingleResult();
        }

        private static String user() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Principal principal) {
                return principal.user().toString();
            }
            return null;
        }
    }
}
