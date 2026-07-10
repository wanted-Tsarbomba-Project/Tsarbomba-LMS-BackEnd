package com.wanted.codebombalms.problems.dataset.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 업로드된 CSV bytes에서 컬럼명 + 예시값을 추출해 metadata JSON을 만든다.
 * 계약: {"columns":[{"name":..,"examples":[..]}]} (dtype 추론 없음)
 * 부가정보이므로 어떤 실패에도 예외를 던지지 않고 null을 반환한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatasetMetadataExtractor {

    private static final int MAX_COLUMNS = 100;
    private static final int MAX_EXAMPLES = 5;
    private static final int MAX_EXAMPLE_LENGTH = 100;
    private static final int MAX_SCAN_ROWS = 200;
    private static final Charset MS949 = Charset.forName("MS949");

    private final ObjectMapper objectMapper;

    public String extract(byte[] content) {
        if (content == null || content.length == 0) {
            return null;
        }

        String text = decode(content);
        if (text == null) {
            log.warn("데이터셋 metadata 추출 실패: UTF-8/MS949 디코딩 불가 (mojibake 차단)");
            return null;
        }

        try {
            return parse(text);
        } catch (Exception e) {
            log.warn("데이터셋 metadata 추출 실패: CSV 파싱 오류", e);
            return null;
        }
    }

    private String parse(String text) throws Exception {
        try (CSVParser parser = CSVParser.parse(new StringReader(text), CSVFormat.DEFAULT)) {
            Iterator<CSVRecord> it = parser.iterator();
            if (!it.hasNext()) {
                return null; // 빈 파일
            }

            CSVRecord header = it.next();
            int columnCount = Math.min(header.size(), MAX_COLUMNS);
            if (columnCount == 0) {
                return null;
            }

            List<String> names = new ArrayList<>(columnCount);
            List<LinkedHashSet<String>> examples = new ArrayList<>(columnCount);
            for (int i = 0; i < columnCount; i++) {
                names.add(header.get(i) == null ? "" : header.get(i).trim());
                examples.add(new LinkedHashSet<>());
            }

            int scanned = 0;
            while (it.hasNext() && scanned < MAX_SCAN_ROWS) {
                CSVRecord row = it.next();
                for (int i = 0; i < columnCount; i++) {
                    if (i >= row.size()) {
                        continue;
                    }
                    String value = row.get(i);
                    if (value == null || value.isBlank()) {
                        continue;
                    }
                    LinkedHashSet<String> bucket = examples.get(i);
                    if (bucket.size() >= MAX_EXAMPLES) {
                        continue;
                    }
                    bucket.add(truncate(value.trim()));
                }
                scanned++;
            }

            List<ColumnMeta> columns = new ArrayList<>(columnCount);
            for (int i = 0; i < columnCount; i++) {
                columns.add(new ColumnMeta(names.get(i), new ArrayList<>(examples.get(i))));
            }

            return objectMapper.writeValueAsString(new DatasetMeta(columns));
        }
    }

    private String decode(byte[] content) {
        String utf8 = decodeStrict(content, StandardCharsets.UTF_8);
        if (utf8 != null) {
            return stripBom(utf8);
        }
        return decodeStrict(content, MS949);
    }

    private String decodeStrict(byte[] content, Charset charset) {
        CharsetDecoder decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            return decoder.decode(ByteBuffer.wrap(content)).toString();
        } catch (CharacterCodingException e) {
            return null;
        }
    }

    private String stripBom(String text) {
        if (!text.isEmpty() && text.charAt(0) == '\uFEFF') {
            return text.substring(1);
        }
        return text;
    }

    private String truncate(String value) {
        return value.length() <= MAX_EXAMPLE_LENGTH
                ? value
                : value.substring(0, MAX_EXAMPLE_LENGTH);
    }

    // 클래스 하단에 nested record 2개 추가 (JSON 계약 shape)
    private record ColumnMeta(String name, List<String> examples) {}
    private record DatasetMeta(List<ColumnMeta> columns) {}
}
