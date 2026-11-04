package com.botoni.vsr.controller;

import com.botoni.vsr.support.ApplicationTest;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Evidências pela API, com banco e disco de verdade")
class EvidenceFlowTest extends ApplicationTest {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F', 'I', 'F', 0, 1};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 13, 'I', 'H', 'D', 'R'};
    private static final String PROPERTY = """
            {"type":"apartamento","category":"residencial",
             "address":{"cep":"40020-000","state":"BA","city":"Salvador","district":"Comércio",
                        "street":"Avenida da França","number":"%s"}}""";

    @Test
    @Controle
    @DisplayName("upload, detalhe, download e remoção; o formato vem dos bytes, não do que o cliente declara")
    void uploadAndDownload() throws Exception {
        String token = register("Iris Lopes", "78778932807", "iris.evidencia@vsr.com");
        String room = room(token, "1");

        String jpeg = read(authorized(upload(room, photo("foto.jpg", "image/jpeg", JPEG)).param("description", " Parede  norte "), token),
                201, "$.id");
        mockMvc.perform(authorized(upload(room, photo("disfarce.jpg", "image/jpeg", PNG)), token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contentType").value("image/png"))
                .andExpect(jsonPath("$.size").value(PNG.length));

        String inspection = text("select v.uuid::text from vistorias.vistoria v join vistorias.ambiente a on a.id_vistoria = v.id_vistoria "
                + "where a.uuid = '" + room + "'");
        mockMvc.perform(authorized(get("/api/1/inspections/" + inspection), token))
                .andExpect(jsonPath("$.rooms[0].evidences", hasSize(2)))
                .andExpect(jsonPath("$.rooms[0].evidences[0].description").value("Parede norte"));

        mockMvc.perform(authorized(get("/api/1/evidences/" + jpeg), token))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(content().bytes(JPEG));

        mockMvc.perform(authorized(delete("/api/1/evidences/" + jpeg), token))
                .andExpect(status().isNoContent());
        mockMvc.perform(authorized(get("/api/1/evidences/" + jpeg), token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("InspectionProblem.EVIDENCE_NOT_FOUND"));
    }

    @Test
    @Controle
    @DisplayName("arquivo que não é JPEG nem PNG, vazio ou ausente é recusado")
    void invalidFilesAreRejected() throws Exception {
        String token = register("Joel Dutra", "79217421812", "joel.evidencia@vsr.com");
        String room = room(token, "2");

        mockMvc.perform(authorized(upload(room, photo("foto.jpg", "image/jpeg", "<script>alert(1)</script>".getBytes())), token))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("PhotoProblem.UNSUPPORTED"));
        mockMvc.perform(authorized(upload(room, photo("vazio.jpg", "image/jpeg", new byte[0])), token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PhotoProblem.EMPTY"));
        mockMvc.perform(authorized(multipart("/api/1/rooms/" + room + "/evidences").param("description", "sem arquivo"), token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Controle
    @DisplayName("evidência recusada pelo banco não deixa arquivo órfão no disco")
    void rejectedEvidenceLeavesNoFile() throws Exception {
        String token = register("Kai Moura", "09679290832", "kai.evidencia@vsr.com");
        String room = room(token, "3");
        String inspection = text("select v.uuid::text from vistorias.vistoria v join vistorias.ambiente a on a.id_vistoria = v.id_vistoria "
                + "where a.uuid = '" + room + "'");
        String other = read(authorized(json(post("/api/1/inspections/" + inspection + "/rooms"), "{\"name\":\"Cozinha\"}"), token),
                201, "$.id");
        String item = read(authorized(json(post("/api/1/rooms/" + other + "/items"), "{\"name\":\"Pia\"}"), token), 201, "$.id");
        long before = files();

        mockMvc.perform(authorized(upload(room, photo("foto.jpg", "image/jpeg", JPEG)).param("item", item), token))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("ForeignKeyConstraint.FK_EVIDENCIA_ITEM"));
        assertThat(files()).isEqualTo(before);

        String kept = read(authorized(upload(room, photo("foto.jpg", "image/jpeg", JPEG)), token), 201, "$.id");
        mockMvc.perform(authorized(post("/api/1/inspections/" + inspection + "/finish"), token))
                .andExpect(status().isOk());
        long finished = files();

        mockMvc.perform(authorized(upload(room, photo("foto.jpg", "image/jpeg", JPEG)), token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RuleConstraint.RN_VISTORIA_EM_ANDAMENTO"));
        mockMvc.perform(authorized(delete("/api/1/evidences/" + kept), token))
                .andExpect(status().isConflict());
        assertThat(files()).isEqualTo(finished);
        mockMvc.perform(authorized(get("/api/1/evidences/" + kept), token))
                .andExpect(status().isOk());
    }

    @Test
    @Controle
    @DisplayName("evidência e ambiente de outro usuário respondem 404")
    void othersSeeNothing() throws Exception {
        String owner = register("Lia Campos", "10033907579", "lia.evidencia@vsr.com");
        String other = register("Mauro Pinto", "79383470194", "mauro.evidencia@vsr.com");
        String room = room(owner, "4");
        String evidence = read(authorized(upload(room, photo("foto.jpg", "image/jpeg", JPEG)), owner), 201, "$.id");

        mockMvc.perform(authorized(get("/api/1/evidences/" + evidence), other))
                .andExpect(status().isNotFound());
        mockMvc.perform(authorized(delete("/api/1/evidences/" + evidence), other))
                .andExpect(status().isNotFound());
        mockMvc.perform(authorized(upload(room, photo("foto.jpg", "image/jpeg", JPEG)), other))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("InspectionProblem.ROOM_NOT_FOUND"));
        mockMvc.perform(get("/api/1/evidences/" + evidence))
                .andExpect(status().isUnauthorized());
    }

    private String room(String token, String number) throws Exception {
        String property = read(authorized(json(post("/api/1/properties"), PROPERTY.formatted(number)), token), 201, "$.id");
        String inspection = read(authorized(json(post("/api/1/inspections"),
                "{\"property\":\"" + property + "\",\"type\":\"entrada\"}"), token), 201, "$.id");
        return read(authorized(json(post("/api/1/inspections/" + inspection + "/rooms"), "{\"name\":\"Sala\"}"), token), 201, "$.id");
    }

    private static MockMultipartHttpServletRequestBuilder upload(String room, MockMultipartFile file) {
        return multipart("/api/1/rooms/" + room + "/evidences").file(file);
    }

    private static MockMultipartFile photo(String name, String type, byte[] content) {
        return new MockMultipartFile("file", name, type, content);
    }

    private static long files() throws IOException {
        try (Stream<java.nio.file.Path> files = Files.list(EVIDENCES)) {
            return files.count();
        }
    }
}
