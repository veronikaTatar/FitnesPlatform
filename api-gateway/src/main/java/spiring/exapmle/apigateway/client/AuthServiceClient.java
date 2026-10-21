package spiring.exapmle.apigateway.client;

import spiring.exapmle.apigateway.dto.AuthPayloadDto;
import spiring.exapmle.apigateway.dto.LoginInputDto;
import spiring.exapmle.apigateway.dto.RegisterInputDto;
import spiring.exapmle.apigateway.dto.SessionUserDto;

public interface AuthServiceClient {
    AuthPayloadDto register(RegisterInputDto input);
    AuthPayloadDto login(LoginInputDto input);
    SessionUserDto validateSession(String authorizationHeader);
}
