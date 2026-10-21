package spiring.exapmle.apigateway.client;

import spiring.exapmle.apigateway.dto.AuthPayloadDto;
import spiring.exapmle.apigateway.dto.LoginInputDto;
import spiring.exapmle.apigateway.dto.RegisterInputDto;

public interface AuthServiceClient {
    AuthPayloadDto register(RegisterInputDto input);
    AuthPayloadDto login(LoginInputDto input);
}
