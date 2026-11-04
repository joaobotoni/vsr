package com.botoni.vsr.controller;

import com.botoni.vsr.support.ApplicationTest;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Pessoas, proprietários e pessoas da vistoria pela API, com banco de verdade")
class PersonFlowTest extends ApplicationTest {

    private static final String SHARED_CPF = "26647378219";
    private static final String PROPERTY = """
            {"type":"casa","category":"residencial",
             "address":{"cep":"60060-000","state":"CE","city":"Fortaleza","district":"Centro",
                        "street":"Rua Major Facundo","number":"%s"}}""";

    @Test
    @Controle
    @DisplayName("cada usuário tem o próprio cadastro: o mesmo CPF convive entre usuários e nada vaza de um para o outro")
    void peopleArePrivate() throws Exception {
        String nina = register("Nina Freitas", "88300596178", "nina.pessoa@vsr.com");
        String otto = register("Otto Vieira", "23306697044", "otto.pessoa@vsr.com");

        String tenant = read(authorized(json(post("/api/1/people/individuals"),
                "{\"name\":\"Rui Barros\",\"cpf\":\"266.473.782-19\",\"phone\":\"(85) 99876-5432\"}"), nina), 201, "$.id");
        mockMvc.perform(authorized(get("/api/1/people/" + tenant), nina))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("pf"))
                .andExpect(jsonPath("$.document").value(SHARED_CPF))
                .andExpect(jsonPath("$.phone").value("85998765432"))
                .andExpect(jsonPath("$.email", nullValue()));
        mockMvc.perform(authorized(json(post("/api/1/people/individuals"),
                        "{\"name\":\"Rui Barros\",\"cpf\":\"" + SHARED_CPF + "\"}"), nina))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("UniqueConstraint.UQ_PESSOA_FISICA_TITULAR_CPF"));

        mockMvc.perform(authorized(json(post("/api/1/people/individuals"),
                        "{\"name\":\"Outro Nome\",\"cpf\":\"" + SHARED_CPF + "\"}"), otto))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Outro Nome"));
        mockMvc.perform(authorized(get("/api/1/people/" + tenant), otto))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PersonProblem.NOT_FOUND"));
        mockMvc.perform(authorized(json(patch("/api/1/people/" + tenant), "{\"name\":\"Invasor\"}"), otto))
                .andExpect(status().isNotFound());
        mockMvc.perform(authorized(get("/api/1/people"), otto))
                .andExpect(jsonPath("$.total").value(1));

        register("Rui Barros", SHARED_CPF, "rui.pessoa@vsr.com");
    }

    @Test
    @Controle
    @DisplayName("empresa com razão social livre; edição parcial de nome e contato, com as regras de cada tipo")
    void companiesAndEdits() throws Exception {
        String pia = register("Pia Rocha", "58715021130", "pia.pessoa@vsr.com");

        String company = read(authorized(json(post("/api/1/people/companies"),
                "{\"name\":\"Silva & Filhos Ltda. 2\",\"tradeName\":\"Silva Imóveis\",\"cnpj\":\"11.222.333/0001-81\","
                        + "\"email\":\"Contato@Silva.com.br\"}"), pia), 201, "$.id");
        mockMvc.perform(authorized(json(patch("/api/1/people/" + company),
                        "{\"name\":\"Silva & Filhos 3 Ltda.\",\"phone\":\"8533221100\"}"), pia))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("pj"))
                .andExpect(jsonPath("$.name").value("Silva & Filhos 3 Ltda."))
                .andExpect(jsonPath("$.tradeName").value("Silva Imóveis"))
                .andExpect(jsonPath("$.email").value("contato@silva.com.br"))
                .andExpect(jsonPath("$.phone").value("8533221100"));

        String person = read(authorized(json(post("/api/1/people/individuals"),
                "{\"name\":\"Lia Souza\",\"cpf\":\"07285672297\"}"), pia), 201, "$.id");
        mockMvc.perform(authorized(json(patch("/api/1/people/" + person), "{\"name\":\"Lia 2\"}"), pia))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NameProblem.INVALID_CHARACTERS"));
        mockMvc.perform(authorized(json(patch("/api/1/people/" + person), "{\"email\":\"lia@vsr.com\"}"), pia))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lia Souza"))
                .andExpect(jsonPath("$.email").value("lia@vsr.com"));
        mockMvc.perform(authorized(json(post("/api/1/people/individuals"),
                        "{\"name\":\"Teo Lima\",\"cpf\":\"07313391188\",\"phone\":\"123\"}"), pia))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PhoneProblem.INVALID"));

        mockMvc.perform(authorized(get("/api/1/people"), pia))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.items[0].name").value("Lia Souza"))
                .andExpect(jsonPath("$.items[0].email").value("lia@vsr.com"));
    }

    @Test
    @Controle
    @DisplayName("proprietários do imóvel e pessoas da vistoria vêm só do próprio cadastro")
    void ownersAndParticipants() throws Exception {
        String rita = register("Rita Gomes", "66392332154", "rita.pessoa@vsr.com");
        String saulo = register("Saulo Reis", "46881973659", "saulo.pessoa@vsr.com");
        String owner = read(authorized(json(post("/api/1/people/individuals"),
                "{\"name\":\"Vera Dias\",\"cpf\":\"52998224725\"}"), rita), 201, "$.id");
        String foreign = read(authorized(json(post("/api/1/people/individuals"),
                "{\"name\":\"Davi Alves\",\"cpf\":\"11144477735\"}"), saulo), 201, "$.id");
        String property = read(authorized(json(post("/api/1/properties"), PROPERTY.formatted("7")), rita), 201, "$.id");
        String owners = "/api/1/properties/" + property + "/owners";

        mockMvc.perform(authorized(json(post(owners), "{\"person\":\"" + owner + "\"}"), rita))
                .andExpect(status().isNoContent());
        mockMvc.perform(authorized(json(post(owners), "{\"person\":\"" + owner + "\"}"), rita))
                .andExpect(status().isNoContent());
        mockMvc.perform(authorized(json(post(owners), "{\"person\":\"" + foreign + "\"}"), rita))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PersonProblem.NOT_FOUND"));
        mockMvc.perform(authorized(json(post(owners), "{\"person\":\"" + foreign + "\"}"), saulo))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PropertyProblem.NOT_FOUND"));
        mockMvc.perform(authorized(get(owners), rita))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Vera Dias"));
        mockMvc.perform(authorized(delete(owners + "/" + owner), rita))
                .andExpect(status().isNoContent());
        mockMvc.perform(authorized(get(owners), rita))
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(authorized(json(post(owners), "{\"person\":\"" + owner + "\"}"), rita))
                .andExpect(status().isNoContent());
        mockMvc.perform(authorized(get(owners), rita))
                .andExpect(jsonPath("$", hasSize(1)));

        String inspection = read(authorized(json(post("/api/1/inspections"),
                "{\"property\":\"" + property + "\",\"type\":\"entrada\"}"), rita), 201, "$.id");
        String people = "/api/1/inspections/" + inspection + "/people";
        mockMvc.perform(authorized(json(post(people), "{\"person\":\"" + owner + "\",\"role\":\"inquilino\"}"), rita))
                .andExpect(status().isNoContent());
        mockMvc.perform(authorized(json(post(people), "{\"person\":\"" + owner + "\",\"role\":\"proprietario\"}"), rita))
                .andExpect(status().isNoContent());
        mockMvc.perform(authorized(get(people), rita))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].role").value("proprietario"))
                .andExpect(jsonPath("$[0].person.document").value("52998224725"));
        mockMvc.perform(authorized(get(people), saulo))
                .andExpect(status().isNotFound());

        mockMvc.perform(authorized(post("/api/1/inspections/" + inspection + "/finish"), rita))
                .andExpect(status().isOk());
        mockMvc.perform(authorized(delete(people + "/" + owner), rita))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RuleConstraint.RN_VISTORIA_EM_ANDAMENTO"));
        mockMvc.perform(authorized(get(people), rita))
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
