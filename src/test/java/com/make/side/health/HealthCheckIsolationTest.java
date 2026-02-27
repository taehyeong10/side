package com.make.side.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 메인 스레드풀(8080)이 고갈되어도 헬스체크 포트(8081)는 독립적으로 응답하는지 검증.
 *
 * 전제: 로컬 개발환경(PostgreSQL, Elasticsearch)이 실행 중이어야 합니다.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "server.tomcat.threads.max=2",
                "server.tomcat.threads.min-spare=1"
        }
)
@ActiveProfiles("test")
class HealthCheckIsolationTest {

    private static final String MAIN_URL = "http://localhost:8080";
    private static final String HEALTH_URL = "http://localhost:8081";

    @Test
    @Timeout(30)
    void healthCheck_shouldRespondImmediately_whenMainThreadPoolExhausted() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // maxThreads=2 인 메인 커넥터 스레드 모두 점유
        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                try {
                    new RestTemplate().getForEntity(MAIN_URL + "/slow", String.class);
                } catch (Exception ignored) {}
            });
        }

        // 스레드가 점유될 때까지 대기
        Thread.sleep(500);

        // 8081 헬스체크: 별도 스레드풀이므로 즉시 응답해야 함
        RestTemplate healthClient = clientWithTimeout(2_000);
        long start = System.currentTimeMillis();
        ResponseEntity<String> health = healthClient.getForEntity(HEALTH_URL + "/actuator/health", String.class);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(health.getStatusCode().is2xxSuccessful())
                .as("8081 헬스체크는 성공해야 함")
                .isTrue();
        assertThat(elapsed)
                .as("헬스체크 응답은 1초 이내여야 함 (실제: %dms)", elapsed)
                .isLessThan(1_000L);

        // 8080: 스레드풀 고갈 상태 → 새 요청은 타임아웃
        assertThatThrownBy(() -> clientWithTimeout(1_000).getForEntity(MAIN_URL + "/slow", String.class))
                .as("8080은 스레드풀 고갈로 타임아웃이어야 함")
                .isInstanceOf(ResourceAccessException.class);

        executor.shutdownNow();
    }

    private RestTemplate clientWithTimeout(int ms) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(ms);
        factory.setReadTimeout(ms);
        return new RestTemplate(factory);
    }
}
