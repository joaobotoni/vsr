package com.botoni.vsr.database.repository;

import com.botoni.vsr.database.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddressRepository extends JpaRepository<Address, Integer> {

    @Query(value = "select enderecos.registrar_endereco(cast(:uf as text), cast(:city as text), cast(:district as text), "
            + "cast(:cep as text), cast(:street as text), cast(:number as text), cast(:complement as text))", nativeQuery = true)
    Integer register(@Param("uf") String uf,
                     @Param("city") String city,
                     @Param("district") String district,
                     @Param("cep") String cep,
                     @Param("street") String street,
                     @Param("number") String number,
                     @Param("complement") String complement);
}
