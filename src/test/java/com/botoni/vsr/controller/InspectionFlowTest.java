package com.botoni.vsr.controller;

import com.botoni.vsr.support.ApplicationTest;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Vistorias pela API, com banco de verdade")
class InspectionFlowTest extends ApplicationTest {

    private static final String PROPERTY = """
            {"type":"casa","category":"residencial",
             "address":{"cep":"30130-010","state":"MG","city":"Belo Horizonte","district":"Centro",
                        "street":"Avenida Afonso Pena","number":"%s"}}""";

    @Test
    @Controle
    @DisplayName("o catálogo traz os ambientes padrão com os itens típicos")
    void catalogListsStandardTypes() throws Exception {
        String token = register("Eva Prado", "52601815906", "eva.vistoria@vsr.com");

        mockMvc.perform(authorized(get("/api/1/catalog/rooms"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[?(@.name == 'Banheiro')].items[*]", hasSize(19)));
        int items = Integer.parseInt(text("select count(*) from catalogo.tipo_item where padrao"));
        mockMvc.perform(authorized(get("/api/1/catalog/items"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(items)));
    }

    @Test
    @Controle
    @DisplayName("entrada com ambientes e itens, finalização, saída copiada da entrada e bloqueio depois de finalizada")
    void entryAndExitLifecycle() throws Exception {
        String token = register("Fabio Melo", "08301661305", "fabio.vistoria@vsr.com");
        String property = read(authorized(json(post("/api/1/properties"), PROPERTY.formatted("10")), token), 201, "$.id");
        String bedroom = read(authorized(get("/api/1/catalog/rooms"), token), 200, "$[?(@.name == 'Quarto')].id")
                .replaceAll("[\\[\\]\"]", "");

        String entry = read(authorized(json(post("/api/1/inspections"),
                "{\"property\":\"" + property + "\",\"type\":\"entrada\",\"notes\":\"Chaves com o porteiro\"}"), token), 201, "$.id");
        mockMvc.perform(authorized(json(post("/api/1/inspections"), "{\"property\":\"" + property + "\",\"type\":\"entrada\"}"), token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("UniqueConstraint.UQ_VISTORIA_ANDAMENTO"));

        mockMvc.perform(authorized(json(post("/api/1/inspections/" + entry + "/rooms"),
                        "{\"name\":\"Quarto\",\"type\":\"" + bedroom + "\"}"), token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items", hasSize(13)));
        String porch = read(authorized(json(post("/api/1/inspections/" + entry + "/rooms"), "{\"name\":\"Varanda\"}"), token),
                201, "$.id");
        mockMvc.perform(authorized(json(post("/api/1/inspections/" + entry + "/rooms"), "{\"name\":\"quarto\"}"), token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("UniqueConstraint.UQ_AMBIENTE_NOME"));

        String floor = read(authorized(json(post("/api/1/rooms/" + porch + "/items"),
                "{\"name\":\"Piso\",\"condition\":\"bom\",\"material\":\"Cerâmica\"}"), token), 201, "$.id");
        String railing = read(authorized(json(post("/api/1/rooms/" + porch + "/items"), "{\"name\":\"Guarda-corpo\"}"), token),
                201, "$.id");
        mockMvc.perform(authorized(json(patch("/api/1/items/" + floor), "{\"condition\":\"regular\"}"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.condition").value("regular"))
                .andExpect(jsonPath("$.material").value("Cerâmica"));
        mockMvc.perform(authorized(json(patch("/api/1/rooms/" + porch), "{\"notes\":\"Linha 1\\r\\nLinha 2\"}"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Linha 1\nLinha 2"));
        mockMvc.perform(authorized(delete("/api/1/items/" + railing), token))
                .andExpect(status().isNoContent());

        mockMvc.perform(authorized(json(post("/api/1/inspections"),
                        "{\"property\":\"" + property + "\",\"type\":\"saida\",\"entry\":\"" + entry + "\"}"), token))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("RuleConstraint.RN_VISTORIA_ENTRADA"));

        mockMvc.perform(authorized(post("/api/1/inspections/" + entry + "/finish"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("finalizada"))
                .andExpect(jsonPath("$.finishedAt", notNullValue()));
        mockMvc.perform(authorized(json(patch("/api/1/rooms/" + porch), "{\"name\":\"Sacada\"}"), token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RuleConstraint.RN_VISTORIA_EM_ANDAMENTO"));
        mockMvc.perform(authorized(json(post("/api/1/rooms/" + porch + "/items"), "{\"name\":\"Teto\"}"), token))
                .andExpect(status().isConflict());
        mockMvc.perform(authorized(post("/api/1/inspections/" + entry + "/finish"), token))
                .andExpect(status().isConflict());
        mockMvc.perform(authorized(delete("/api/1/inspections/" + entry), token))
                .andExpect(status().isConflict());

        String exit = read(authorized(json(post("/api/1/inspections"),
                "{\"property\":\"" + property + "\",\"type\":\"saida\",\"entry\":\"" + entry + "\"}"), token), 201, "$.id");
        mockMvc.perform(authorized(get("/api/1/inspections/" + exit), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entry").value(entry))
                .andExpect(jsonPath("$.rooms", hasSize(2)))
                .andExpect(jsonPath("$.rooms[1].name").value("Varanda"))
                .andExpect(jsonPath("$.rooms[1].origin").value(porch))
                .andExpect(jsonPath("$.rooms[1].notes", nullValue()))
                .andExpect(jsonPath("$.rooms[1].items", hasSize(1)))
                .andExpect(jsonPath("$.rooms[1].items[0].origin").value(floor))
                .andExpect(jsonPath("$.rooms[1].items[0].material").value("Cerâmica"))
                .andExpect(jsonPath("$.rooms[1].items[0].condition", nullValue()))
                .andExpect(jsonPath("$.rooms[0].items", hasSize(13)));

        mockMvc.perform(authorized(get("/api/1/inspections").param("status", "em_andamento"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].id").value(exit));
        mockMvc.perform(authorized(get("/api/1/inspections").param("property", property), token))
                .andExpect(jsonPath("$.total").value(2));
        mockMvc.perform(authorized(get("/api/1/inspections").param("status", "qualquer"), token))
                .andExpect(status().isBadRequest());

        mockMvc.perform(authorized(delete("/api/1/inspections/" + exit), token))
                .andExpect(status().isNoContent());
        mockMvc.perform(authorized(get("/api/1/inspections/" + exit), token))
                .andExpect(status().isNotFound());
        mockMvc.perform(authorized(delete("/api/1/properties/" + property), token))
                .andExpect(status().isNoContent());
        mockMvc.perform(authorized(get("/api/1/inspections/" + entry), token))
                .andExpect(status().isOk());
    }

    @Test
    @Controle
    @DisplayName("vistoria, ambiente e item de outro usuário respondem 404")
    void othersSeeNothing() throws Exception {
        String owner = register("Gil Nunes", "18609139034", "gil.vistoria@vsr.com");
        String other = register("Hana Reis", "71428793860", "hana.vistoria@vsr.com");
        String property = read(authorized(json(post("/api/1/properties"), PROPERTY.formatted("20")), owner), 201, "$.id");
        String inspection = read(authorized(json(post("/api/1/inspections"),
                "{\"property\":\"" + property + "\",\"type\":\"entrada\"}"), owner), 201, "$.id");
        String room = read(authorized(json(post("/api/1/inspections/" + inspection + "/rooms"), "{\"name\":\"Sala\"}"), owner),
                201, "$.id");
        String item = read(authorized(json(post("/api/1/rooms/" + room + "/items"), "{\"name\":\"Piso\"}"), owner), 201, "$.id");

        mockMvc.perform(authorized(json(post("/api/1/inspections"), "{\"property\":\"" + property + "\",\"type\":\"entrada\"}"), other))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PropertyProblem.NOT_FOUND"));
        mockMvc.perform(authorized(get("/api/1/inspections/" + inspection), other))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("InspectionProblem.NOT_FOUND"));
        mockMvc.perform(authorized(post("/api/1/inspections/" + inspection + "/finish"), other))
                .andExpect(status().isNotFound());
        mockMvc.perform(authorized(json(post("/api/1/inspections/" + inspection + "/rooms"), "{\"name\":\"Cozinha\"}"), other))
                .andExpect(status().isNotFound());
        mockMvc.perform(authorized(json(patch("/api/1/rooms/" + room), "{\"name\":\"Invasão\"}"), other))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("InspectionProblem.ROOM_NOT_FOUND"));
        mockMvc.perform(authorized(json(post("/api/1/rooms/" + room + "/items"), "{\"name\":\"Teto\"}"), other))
                .andExpect(status().isNotFound());
        mockMvc.perform(authorized(delete("/api/1/items/" + item), other))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("InspectionProblem.ITEM_NOT_FOUND"));
        mockMvc.perform(authorized(get("/api/1/inspections"), other))
                .andExpect(jsonPath("$.total").value(0));

        mockMvc.perform(authorized(get("/api/1/inspections/" + inspection), owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rooms[0].name").value("Sala"))
                .andExpect(jsonPath("$.rooms[0].items[0].name").value("Piso"));
    }
}
