package spiring.exapmle.apigateway.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import spiring.exapmle.apigateway.dto.AuthPayloadDto;
import spiring.exapmle.apigateway.dto.LoginInputDto;
import spiring.exapmle.apigateway.dto.RegisterInputDto;
import spiring.exapmle.apigateway.dto.SessionUserDto;

@Service
public class RestAuthServiceClient implements AuthServiceClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public RestAuthServiceClient(RestClient.Builder builder,
                                 ObjectMapper objectMapper,
                                 @Value("${services.auth.url}") String authServiceUrl) {
        this.restClient = builder
                .baseUrl(authServiceUrl)
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public AuthPayloadDto register(RegisterInputDto input) {
        return post("/api/auth/register", input);
    }

    @Override
    public AuthPayloadDto login(LoginInputDto input) {
        return post("/api/auth/login", input);
    }

    @Override
    public SessionUserDto validateSession(String authorizationHeader) {
        return restClient.get()
                .uri("/api/auth/session")
                .header("Authorization", authorizationHeader == null ? "" : authorizationHeader)
                .exchange((httpRequest, httpResponse) -> {
                    if (httpResponse.getStatusCode().isError()) {
                        throw new IllegalArgumentException(readProblemDetail(httpResponse));
                    }
                    return objectMapper.readValue(httpResponse.getBody(), SessionUserDto.class);
                });
    }

    private AuthPayloadDto post(String uri, Object request) {
        return restClient.post()
                .uri(uri)
                .body(request)
                .exchange((httpRequest, httpResponse) -> {
                    if (httpResponse.getStatusCode().isError()) {
                        throw new IllegalArgumentException(readProblemDetail(httpResponse));
                    }
                    return objectMapper.readValue(
                            httpResponse.getBody(), AuthPayloadDto.class);
                });
    }

    private String readProblemDetail(org.springframework.http.client.ClientHttpResponse response)
            throws java.io.IOException {
        ProblemDetail problem = objectMapper.readValue(response.getBody(), ProblemDetail.class);
        String detail = problem.getDetail();
        return detail == null || detail.isBlank() ? "Authentication request failed" : detail;
    }
}
