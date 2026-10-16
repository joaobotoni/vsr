package com.botoni.vsr.network;

import com.botoni.vsr.support.Controle;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@DisplayName("Dependências do pacote de rede")
class NetworkDependencyTest {

    private static final String FACADE = "ClientNetwork";

    private static final JavaClasses NETWORK = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.botoni.vsr.network");

    @Test
    @Controle
    @DisplayName("o núcleo da rede só depende do JDK e de si mesmo: nada de HTTP, exceções da API ou Spring")
    void coreDependsOnlyOnJdk() {
        classes().that().resideInAPackage("com.botoni.vsr.network..").and().doNotHaveSimpleName(FACADE)
                .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "com.botoni.vsr.network..")
                .check(NETWORK);
    }

    @Test
    @Controle
    @DisplayName("a fachada só acrescenta as exceções da API, para recusar endereço inválido com 400")
    void facadeAddsOnlyApiExceptions() {
        classes().that().haveSimpleName(FACADE)
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("java..", "com.botoni.vsr.network..", "com.botoni.vsr.exception..")
                .check(NETWORK);
    }
}
