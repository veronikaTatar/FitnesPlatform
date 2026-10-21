package spiring.exapmle.authservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import spiring.exapmle.authservice.dto.AuthResponse;
import spiring.exapmle.authservice.dto.LoginRequest;
import spiring.exapmle.authservice.dto.RegisterRequest;
import spiring.exapmle.authservice.dto.SessionValidationResponse;
import spiring.exapmle.authservice.service.AuthenticationService;
import spiring.exapmle.authservice.service.SessionService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final SessionService sessionService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authenticationService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }

    @GetMapping("/session")
    public SessionValidationResponse validateSession(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return sessionService.validateSession(authorizationHeader);
    }
}
