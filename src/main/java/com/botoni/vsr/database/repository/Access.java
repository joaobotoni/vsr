package com.botoni.vsr.database.repository;

/**
 * Quem acessa um imóvel ou uma vistoria: sem empresa, o dono (ou o responsável); com empresa, os membros ativos dela.
 * Trechos de JPQL com o parâmetro {@code :user}.
 */
final class Access {

    static final String MEMBER = "exists (select 1 from Membership m, User mu where mu.uuid = :user "
            + "and m.individual = mu.person and m.removedAt is null and m.company = ";

    /** Imóvel com alias {@code p} e o dono em {@code left join p.user pu} (o imóvel de empresa não tem dono). */
    static final String PROPERTY = "(pu.uuid = :user or " + MEMBER + "p.company)) ";

    /**
     * Vistoria com alias {@code i}: sem empresa, do responsável; com empresa, só dos membros ativos (quem sai da empresa
     * perde o acesso, mesmo às vistorias em que foi responsável).
     */
    static final String INSPECTION = "((i.company is null and i.responsible.uuid = :user) or " + MEMBER + "i.company)) ";

    private Access() {
    }
}
