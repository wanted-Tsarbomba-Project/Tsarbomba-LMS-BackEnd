package com.wanted.codebombalms.problems.dataset.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DatasetMetadataExtractorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DatasetMetadataExtractor extractor = new DatasetMetadataExtractor(objectMapper);

    private JsonNode extractAsJson(byte[] content) throws Exception {
        String json = extractor.extract(content);
        return json == null ? null : objectMapper.readTree(json);
    }

    @Test
    @DisplayName("정상 UTF-8 CSV → 컬럼명과 distinct 예시값이 추출된다")
    void extract_normalCsv() throws Exception {
        byte[] content = ("user_id,country\n"
                + "1,KR\n"
                + "2,US\n"
                + "3,KR\n").getBytes(StandardCharsets.UTF_8);

        JsonNode root = extractAsJson(content);

        assertEquals(2, root.get("columns").size());
        assertEquals("user_id", root.get("columns").get(0).get("name").asText());
        assertEquals("country", root.get("columns").get(1).get("name").asText());
        // country: distinct 유지 (KR, US) — 중복 KR 하나만
        JsonNode countryExamples = root.get("columns").get(1).get("examples");
        assertEquals(2, countryExamples.size());
        assertEquals("KR", countryExamples.get(0).asText());
        assertEquals("US", countryExamples.get(1).asText());
    }

    @Test
    @DisplayName("따옴표 안 콤마/개행이 있어도 컬럼이 밀리지 않는다")
    void extract_quotedFieldWithCommaAndNewline() throws Exception {
        byte[] content = ("name,note\n"
                + "\"Kim, D\",\"line1\nline2\"\n").getBytes(StandardCharsets.UTF_8);

        JsonNode root = extractAsJson(content);

        assertEquals(2, root.get("columns").size());
        assertEquals("Kim, D", root.get("columns").get(0).get("examples").get(0).asText());
        assertEquals("line1\nline2", root.get("columns").get(1).get("examples").get(0).asText());
    }

    @Test
    @DisplayName("MS949(CP949) 한글 CSV도 정상 디코드된다")
    void extract_ms949Korean() throws Exception {
        byte[] content = ("이름,지역\n홍길동,서울\n").getBytes(Charset.forName("MS949"));

        JsonNode root = extractAsJson(content);

        assertEquals("이름", root.get("columns").get(0).get("name").asText());
        assertEquals("홍길동", root.get("columns").get(0).get("examples").get(0).asText());
        assertEquals("서울", root.get("columns").get(1).get("examples").get(0).asText());
    }

    @Test
    @DisplayName("UTF-8/MS949 둘 다 디코드 불가한 바이트 → null")
    void extract_undecodable_returnsNull() throws Exception {
        byte[] content = {(byte) 0xC3, (byte) 0x28, (byte) 0xA0, (byte) 0xA1};

        assertNull(extractAsJson(content));
    }

    @Test
    @DisplayName("빈 파일 → null")
    void extract_empty_returnsNull() throws Exception {
        assertNull(extractAsJson(new byte[0]));
        assertNull(extractAsJson(null));
    }

    @Test
    @DisplayName("헤더만 있는 파일 → 컬럼명은 나오되 examples는 빈 배열")
    void extract_headerOnly() throws Exception {
        byte[] content = "col_a,col_b".getBytes(StandardCharsets.UTF_8);

        JsonNode root = extractAsJson(content);

        assertEquals(2, root.get("columns").size());
        assertTrue(root.get("columns").get(0).get("examples").isEmpty());
    }

    @Test
    @DisplayName("distinct 예시값은 최대 5개로 절단되고 공백값은 스킵된다")
    void extract_distinctCappedAndBlankSkipped() throws Exception {
        StringBuilder sb = new StringBuilder("v\n");
        sb.append("\n");        // 공백 → 스킵
        sb.append("   \n");     // 공백 → 스킵
        for (int i = 1; i <= 8; i++) {
            sb.append(i).append("\n");
        }
        byte[] content = sb.toString().getBytes(StandardCharsets.UTF_8);

        JsonNode root = extractAsJson(content);
        JsonNode examples = root.get("columns").get(0).get("examples");

        assertEquals(5, examples.size());
        assertEquals("1", examples.get(0).asText());
        assertEquals("5", examples.get(4).asText());
    }

    @Test
    @DisplayName("컬럼 수가 상한(100)을 넘으면 앞 100개로 절단된다")
    void extract_columnCap() throws Exception {
        StringBuilder header = new StringBuilder();
        StringBuilder rowValues = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            if (i > 0) {
                header.append(",");
                rowValues.append(",");
            }
            header.append("c").append(i);
            rowValues.append("v").append(i);
        }
        byte[] content = (header + "\n" + rowValues + "\n").getBytes(StandardCharsets.UTF_8);

        JsonNode root = extractAsJson(content);

        assertEquals(100, root.get("columns").size());
    }
}
