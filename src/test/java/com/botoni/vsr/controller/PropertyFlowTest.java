package com.botoni.vsr.controller;

import com.botoni.vsr.support.ApplicationTest;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Imóveis pela API, com banco de verdade")
class PropertyFlowTest extends ApplicationTest {

    private static final String PROPERTY = """
            {"description":"  Apartamento   de frente ","type":"apartamento","category":"residencial",
             "address":{"cep":"01310-100","state":"sp","city":"%s","district":"Bela Vista",
                        "street":"Avenida Paulista","number":"1000","complement":"Apto 12"}}""";

    @Test
    @Controle
    @DisplayName("o dono cadastra, consulta, lista, edita e remove; outro usuário recebe 404 em tudo")
    void ownerManagesPropertyAndOthersSeeNothing() throws Exception {
        String ana = register("Ana Souza", "52998224725", "ana.imovel@vsr.com");
        String bia = register("Bia Lima", "11144477735", "bia.imovel@vsr.com");

        mockMvc.perform(authorized(json(post("/api/1/properties"), PROPERTY.formatted("São Paulo")), ana))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Apartamento de frente"))
                .andExpect(jsonPath("$.address.cep").value("01310100"))
                .andExpect(jsonPath("$.address.state").value("SP"))
                .andExpect(jsonPath("$.address.city").value("São Paulo"));
        String id = read(authorized(get("/api/1/properties"), ana), 200, "$.items[0].id");
        String property = "/api/1/properties/" + id;

        mockMvc.perform(authorized(json(post("/api/1/properties"), PROPERTY.formatted("são paulo")), ana))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("UniqueConstraint.UQ_IMOVEL_ENDERECO_TITULAR"));

        mockMvc.perform(authorized(get("/api/1/properties"), ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].id").value(id));

        mockMvc.perform(authorized(get("/api/1/properties"), bia))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
        mockMvc.perform(authorized(get(property), bia))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PropertyProblem.NOT_FOUND"));
        mockMvc.perform(authorized(json(patch(property), "{\"description\":\"invasão\"}"), bia))
                .andExpect(status().isNotFound());
        mockMvc.perform(authorized(delete(property), bia))
                .andExpect(status().isNotFound());

        mockMvc.perform(authorized(json(patch(property), "{\"description\":\"Cobertura\",\"category\":\"alto_padrao\"}"), ana))
                .andExpect(status().isOk());
        mockMvc.perform(authorized(get(property), ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Cobertura"))
                .andExpect(jsonPath("$.category").value("alto_padrao"))
                .andExpect(jsonPath("$.type").value("apartamento"));

        mockMvc.perform(authorized(delete(property), ana))
                .andExpect(status().isNoContent());
        mockMvc.perform(authorized(get(property), ana))
                .andExpect(status().isNotFound());
        mockMvc.perform(authorized(get("/api/1/properties"), ana))
                .andExpect(jsonPath("$.total").value(0));

        mockMvc.perform(authorized(json(post("/api/1/properties"), PROPERTY.formatted("São Paulo")), ana))
                .andExpect(status().isCreated());
    }

    @Test
    @Controle
    @DisplayName("imóvel com vistoria em andamento não é removido")
    void propertyWithOpenInspectionIsKept() throws Exception {
        String carla = register("Carla Dias", "39053344705", "carla.imovel@vsr.com");
        String id = read(authorized(json(post("/api/1/properties"), PROPERTY.formatted("Campinas")), carla), 201, "$.id");
        execute("insert into vistorias.vistoria (id_usuario_responsavel, id_imovel, tipo) "
                + "select id_usuario, id_imovel, 'entrada' from imoveis.imovel where uuid = '" + id + "'");

        mockMvc.perform(authorized(delete("/api/1/properties/" + id), carla))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RuleConstraint.RN_IMOVEL_VISTORIA_ANDAMENTO"));
        mockMvc.perform(authorized(get("/api/1/properties/" + id), carla))
                .andExpect(status().isOk());
    }

    @Test
    @Controle
    @DisplayName("parâmetros inválidos respondem 400")
    void invalidParametersAreRejected() throws Exception {
        String dani = register("Dani Rocha", "86288366757", "dani.imovel@vsr.com");

        mockMvc.perform(authorized(get("/api/1/properties/nao-e-uuid"), dani))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ValidationProblem.INVALID_PARAMETER"));
        mockMvc.perform(authorized(get("/api/1/properties").param("size", "500"), dani))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ValidationProblem.INVALID_DATA"));
        mockMvc.perform(authorized(json(post("/api/1/properties"), PROPERTY.formatted("São Paulo").replace("01310-100", "123")), dani))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CepProblem.FORMAT"));
        mockMvc.perform(authorized(json(post("/api/1/properties"), PROPERTY.formatted("São Paulo").replace("\"sp\"", "\"XX\"")), dani))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ValidationProblem.INVALID_DATA"));
        mockMvc.perform(authorized(json(post("/api/1/properties"), PROPERTY.formatted("<script>")), dani))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Controle
    @DisplayName("sem token, a rota de imóveis responde 401")
    void anonymousIsRejected() throws Exception {
        mockMvc.perform(get("/api/1/properties"))
                .andExpect(status().isUnauthorized());
    }
}
