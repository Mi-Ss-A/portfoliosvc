package com.wibeechat.missa.service.portfolio;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KibanaService {

  @Value("${elasticsearch.host}")
  private String elasticsearchHost; // Elasticsearch URL

  @Value("${elasticsearch.username}")
  private String elasticsearchUsername; // Elasticsearch 계정
  @Value("${elasticsearch.password}")
  private String elasticsearchPassword; // Elasticsearch 패스워드

  private final RestTemplate restTemplate;

  public KibanaService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Map<String, Object> getTransactionData(String userNo, String duration, String dataType) {
    // Duration 변환
    String timeRange = switch (duration) {
      case "3m" -> "now-3M/M";
      case "6m" -> "now-6M/M";
      case "1y" -> "now-1y/M";
      default -> "now-15y/M";
    };

    // 데이터 타입별 Elasticsearch Index 설정
    String dataIndex = switch (dataType) {
      case "card" -> "card_trx";
      case "bank" -> "bank_trx";
      case "loan" -> "loan_trx";
      case "fund" -> "fund_trx";
      default -> throw new IllegalArgumentException("Invalid data type: " + dataType);
    };

    // Elasticsearch URL
    String url = elasticsearchHost + "/" + dataIndex + "/_search";

    // 요청 JSON 본문 생성
    String query = createQuery(userNo, timeRange, dataType);

    // HTTP 헤더 설정
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    headers.setBasicAuth(elasticsearchUsername, elasticsearchPassword); // Elasticsearch 인증

    HttpEntity<String> requestEntity = new HttpEntity<>(query, headers);

    try {
      log.info("Elasticsearch 요청 URL: {}", url);
      log.info("Elasticsearch 요청 본문: {}", query);

      // POST 요청
      ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
      log.info("Elasticsearch 응답 데이터: {}", response.getBody());
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error("HTTP 에러 발생: 상태 코드 {}, 응답 본문: {}", e.getStatusCode(), e.getResponseBodyAsString());
      throw new RuntimeException("Elasticsearch API 호출 중 HTTP 오류 발생: " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("기타 오류 발생: {}", e.getMessage());
      throw new RuntimeException("Elasticsearch API 호출 중 기타 오류 발생: " + e.getMessage(), e);
    }
  }

  private String createQuery(String userNo, String timeRange, String dataType) {
    if (("loan".equals(dataType)) || ("fund".equals(dataType))) {
      // Loan의 경우 transaction_date 조건 없음
      return """
          {
            "query": {
              "bool": {
                "must": [
                  { "match": { "user_no": "%s" } }
                ]
              }
            },
          "size": 1000
          }
          """.formatted(userNo);
    } else {
      // Card 및 Bank의 경우 transaction_date 조건 포함
      return """
          {
            "query": {
              "bool": {
                "must": [
                  { "match": { "user_no": "%s" } },
                  { "range": { "transaction_date": { "gte": "%s", "lte": "now/M" } } }
                ]
              }
            },
          "size": 1000
          }
          """.formatted(userNo, timeRange);
    }
  }
}
