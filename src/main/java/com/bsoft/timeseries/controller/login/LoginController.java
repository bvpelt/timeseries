package com.bsoft.timeseries.controller.login;


import com.bsoft.timeseries.login.api.LoginApi;
import com.bsoft.timeseries.login.model.LoginRequest;
import com.bsoft.timeseries.login.model.LoginResponse;

import com.bsoft.timeseries.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("${openapi.login.base-path}")
@Controller
public class LoginController implements LoginApi {

    private final AuthenticationService authenticationService;

    @Value("${info.project.version}")
    private String version;

    private HttpHeaders getVersionHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Version", version);
        return headers;
    }

    @Override
    public ResponseEntity<LoginResponse> _postLogin(LoginRequest loginRequest) {
        log.debug("_postTestLogin apikey");
        return ResponseEntity.status(HttpStatus.OK)
                .headers(getVersionHeaders())
                .body(authenticationService.basicjwt(loginRequest));
    }

    @Override
    public ResponseEntity<LoginResponse> _postAuthenticate(LoginRequest loginRequest) {
        log.debug("_postAuthenticate apikey");
        return ResponseEntity.status(HttpStatus.OK)
                .headers(getVersionHeaders())
                .body(authenticationService.authenticate(loginRequest));
    }

    @Override
    public ResponseEntity<LoginResponse> _postRegister(LoginRequest loginRequest) {
        log.debug("_postRegister apikey");
        return ResponseEntity.status(HttpStatus.OK)
                .headers(getVersionHeaders())
                .body(authenticationService.register(loginRequest));
    }


}

