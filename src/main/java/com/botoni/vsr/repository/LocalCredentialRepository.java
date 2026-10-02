    package com.botoni.vsr.repository;

    import com.botoni.vsr.entity.users.LocalCredential;
    import com.botoni.vsr.vo.Email;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;

    import java.util.Optional;

    public interface LocalCredentialRepository extends JpaRepository<LocalCredential, Integer> {
        @Query("select c from LocalCredential c join fetch c.user u where u.email = :email")
        Optional<LocalCredential> findWithUserByEmail(@Param("email") Email email);
    }
