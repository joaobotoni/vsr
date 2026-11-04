package com.botoni.vsr.controller;

import com.botoni.vsr.support.ApplicationTest;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Empresas, catálogo personalizado, estados e endereços sem acento, com banco de verdade")
class CompanyFlowTest extends ApplicationTest {

    private static final String COMPANY = """
            {"name":"Prado & Lima Imóveis Ltda.","tradeName":"Prado Imóveis","cnpj":"11.444.777/0001-61","role":"%s"}""";
    private static final String PROPERTY = """
            {"type":"sala_comercial","category":"comercial","company":"%s",
             "address":{"cep":"70040-010","state":"DF","city":"Brasília","district":"Asa Sul",
                        "street":"Setor Comercial Sul","number":"%s"}}""";

    @Test
    @Controle
    @DisplayName("empresa com membros: imóvel e vistoria da empresa valem para todos os membros ativos, e só para eles")
    void companySharesWithMembers() throws Exception {
        String admin = register("Ugo Prado", "71019011580", "ugo.empresa@vsr.com");
        String employee = register("Vivi Lima", "30005979722", "vivi.empresa@vsr.com");
        String outsider = register("Wagner Paz", "21210873117", "wagner.empresa@vsr.com");

        mockMvc.perform(authorized(json(post("/api/1/companies"), COMPANY.formatted("funcionario")), admin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CompanyProblem.ADMIN_ROLE_REQUIRED"));
        String company = read(authorized(json(post("/api/1/companies"), COMPANY.formatted("socio")), admin), 201, "$.id");
        mockMvc.perform(authorized(json(post("/api/1/companies"), COMPANY.formatted("socio")), outsider))
                .andExpect(status().isConflict());

        String members = "/api/1/companies/" + company + "/members";
        mockMvc.perform(authorized(json(post(members), "{\"email\":\"vivi.empresa@vsr.com\",\"role\":\"funcionario\"}"), admin))
                .andExpect(status().isNoContent());
        mockMvc.perform(authorized(json(post(members), "{\"email\":\"ninguem@vsr.com\",\"role\":\"funcionario\"}"), admin))
                .andExpect(status().isNoContent());
        mockMvc.perform(authorized(get(members), employee))
                .andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(authorized(json(post(members), "{\"email\":\"wagner.empresa@vsr.com\",\"role\":\"socio\"}"), employee))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CompanyProblem.ADMIN_REQUIRED"));
        mockMvc.perform(authorized(get("/api/1/companies/" + company), outsider))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CompanyProblem.NOT_FOUND"));

        String property = read(authorized(json(post("/api/1/properties"), PROPERTY.formatted(company, "10")), employee), 201, "$.id");
        mockMvc.perform(authorized(json(post("/api/1/properties"), PROPERTY.formatted(company, "11")), outsider))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CompanyProblem.NOT_FOUND"));
        mockMvc.perform(authorized(get("/api/1/properties/" + property), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company").value(company));
        mockMvc.perform(authorized(get("/api/1/properties/" + property), outsider))
                .andExpect(status().isNotFound());

        String inspection = read(authorized(json(post("/api/1/inspections"),
                "{\"property\":\"" + property + "\",\"type\":\"entrada\"}"), employee), 201, "$.id");
        read(authorized(json(post("/api/1/inspections/" + inspection + "/rooms"), "{\"name\":\"Recepção\"}"), admin), 201, "$.id");
        mockMvc.perform(authorized(get("/api/1/inspections/" + inspection), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rooms", hasSize(1)));
        mockMvc.perform(authorized(get("/api/1/inspections/" + inspection), outsider))
                .andExpect(status().isNotFound());

        String owner = read(authorized(json(post("/api/1/people/companies"),
                "{\"name\":\"Condomínio Central\",\"tradeName\":\"Central\",\"cnpj\":\"11222333000181\"}"), admin), 201, "$.id");
        mockMvc.perform(authorized(json(post("/api/1/properties/" + property + "/owners"), "{\"person\":\"" + owner + "\"}"), admin))
                .andExpect(status().isNoContent());

        String adminId = text("select uuid::text from usuarios.usuario where email = 'ugo.empresa@vsr.com'");
        String employeeId = text("select uuid::text from usuarios.usuario where email = 'vivi.empresa@vsr.com'");
        mockMvc.perform(authorized(delete(members + "/" + adminId), admin))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RuleConstraint.RN_EMPRESA_ADMINISTRADOR"));
        mockMvc.perform(authorized(delete(members + "/" + employeeId), admin))
                .andExpect(status().isNoContent());
        mockMvc.perform(authorized(get("/api/1/properties/" + property), employee))
                .andExpect(status().isNotFound());
        mockMvc.perform(authorized(get("/api/1/inspections/" + inspection), employee))
                .andExpect(status().isNotFound());
        mockMvc.perform(authorized(get("/api/1/companies"), employee))
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(authorized(get("/api/1/companies"), admin))
                .andExpect(jsonPath("$[0].role").value("socio"));

        mockMvc.perform(authorized(delete(members + "/" + java.util.UUID.randomUUID()), admin))
                .andExpect(status().isNoContent());
        org.assertj.core.api.Assertions.assertThat(text("select usuario::text from auditoria.registro "
                + "where tabela = 'vinculos.vinculo_pessoa_empresa' and operacao = 'UPDATE' order by id_registro desc limit 1"))
                .isEqualTo(adminId);
        org.assertj.core.api.Assertions.assertThat(text("select count(*)::text from auditoria.registro "
                + "where tabela = 'vistorias.ambiente' and operacao = 'INSERT' and usuario = '" + adminId + "'"))
                .isEqualTo("1");
    }

    @Test
    @Controle
    @DisplayName("catálogo personalizado: o usuário cria os próprios tipos, que só ele vê e usa")
    void customCatalog() throws Exception {
        String owner = register("Xena Rios", "87191890444", "xena.catalogo@vsr.com");
        String other = register("Yuri Sena", "81475433794", "yuri.catalogo@vsr.com");

        String deck = read(authorized(json(post("/api/1/catalog/items"), "{\"name\":\"Deck de madeira\"}"), owner), 201, "$.id");
        String floor = read(authorized(get("/api/1/catalog/items"), owner), 200, "$[?(@.name == 'Piso')].id").replaceAll("[\\[\\]\"]", "");
        String porch = read(authorized(json(post("/api/1/catalog/rooms"),
                "{\"name\":\"Varanda gourmet\",\"items\":[\"" + deck + "\",\"" + floor + "\"]}"), owner), 201, "$.id");

        mockMvc.perform(authorized(get("/api/1/catalog/rooms"), owner))
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[?(@.name == 'Varanda gourmet')].items[*]", hasSize(2)))
                .andExpect(jsonPath("$[?(@.name == 'Varanda gourmet')].standard").value(false));
        mockMvc.perform(authorized(get("/api/1/catalog/rooms"), other))
                .andExpect(jsonPath("$", hasSize(5)));
        mockMvc.perform(authorized(json(post("/api/1/catalog/rooms"),
                        "{\"name\":\"Copia\",\"items\":[\"" + deck + "\"]}"), other))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CatalogProblem.ITEM_TYPE_NOT_FOUND"));
        mockMvc.perform(authorized(json(post("/api/1/catalog/items"), "{\"name\":\"deck de madeira\"}"), owner))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("UniqueConstraint.UQ_TIPO_ITEM_NOME"));

        String property = read(authorized(json(post("/api/1/properties"),
                PROPERTY.replace("\"company\":\"%s\",", "").formatted("30")), owner), 201, "$.id");
        String inspection = read(authorized(json(post("/api/1/inspections"),
                "{\"property\":\"" + property + "\",\"type\":\"entrada\"}"), owner), 201, "$.id");
        mockMvc.perform(authorized(json(post("/api/1/inspections/" + inspection + "/rooms"),
                        "{\"name\":\"Varanda\",\"type\":\"" + porch + "\"}"), owner))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    @Controle
    @DisplayName("estados e endereços: a lista dos 27 estados e a mesma cidade com ou sem acento")
    void statesAndAccents() throws Exception {
        String token = register("Zeca Melo", "37759458061", "zeca.endereco@vsr.com");

        mockMvc.perform(authorized(get("/api/1/states"), token))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("max-age=86400")))
                .andExpect(jsonPath("$", hasSize(27)))
                .andExpect(jsonPath("$[0].acronym").value("AC"))
                .andExpect(jsonPath("$[25].name").value("São Paulo"));

        String address = """
                {"type":"casa","category":"residencial","address":{"cep":"88015-100","state":"SC","city":"%s",
                 "district":"%s","street":"Rua Esteves Júnior","number":"5"}}""";
        read(authorized(json(post("/api/1/properties"), address.formatted("Florianópolis", "Centro")), token), 201, "$.id");
        mockMvc.perform(authorized(json(post("/api/1/properties"), address.formatted("FLORIANOPOLIS", "centro")), token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("UniqueConstraint.UQ_IMOVEL_ENDERECO_TITULAR"));
        org.assertj.core.api.Assertions.assertThat(
                text("select count(*)::text from enderecos.cidade where documento.sem_acento(nome) = 'florianopolis'")).isEqualTo("1");
    }
}
