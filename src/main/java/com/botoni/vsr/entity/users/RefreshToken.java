    package com.botoni.vsr.entity.users;

    import jakarta.persistence.*;
    import lombok.AccessLevel;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import org.hibernate.annotations.Generated;
    import org.hibernate.generator.EventType;

    import java.time.Instant;

    @Entity
    @Getter
    @Builder
    @Table(schema = "usuarios", name = "refresh_token")
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class RefreshToken {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id_refresh_token")
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "id_sessao", nullable = false, updatable = false)
        private Session session;

        @Column(name = "token_hash", nullable = false, updatable = false)
        private String hash;

        @Column(name = "usado_em")
        private Instant usedAt;

        @Generated(event = EventType.INSERT)
        @Column(name = "created_at", insertable = false, updatable = false)
        private Instant createdAt;
    }