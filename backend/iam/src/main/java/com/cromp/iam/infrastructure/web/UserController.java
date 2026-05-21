package com.cromp.iam.infrastructure.web;

import com.cromp.iam.api.dto.request.ChangeUserEmailRequest;
import com.cromp.iam.api.dto.request.ChangeUserPasswordRequest;
import com.cromp.iam.api.dto.request.UpdateUserProfileRequest;
import com.cromp.iam.api.dto.response.UserResponse;
import com.cromp.iam.api.service.UserFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserFacade userFacade;

    @GetMapping("/{userUuid}")
    @PreAuthorize("isAuthenticated()")
    public UserResponse getUser(@PathVariable UUID userUuid) {
        return userFacade.getById(userUuid);
    }

    @PutMapping("/{userUuid}/profile")
    @PreAuthorize("isAuthenticated()")
    public UserResponse updateProfile(@PathVariable UUID userUuid,
                                      @Valid @RequestBody UpdateUserProfileRequest request) {
        return userFacade.updateProfile(userUuid, request);
    }

    
    @PutMapping("/{userUuid}/email")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeEmail(@PathVariable UUID userUuid,
                            @Valid @RequestBody ChangeUserEmailRequest request) {
        userFacade.changeEmail(userUuid, request);
    }

    @PutMapping("/{userUuid}/password")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable UUID userUuid,
                               @Valid @RequestBody ChangeUserPasswordRequest request) {
        userFacade.changePassword(userUuid, request);
    }
}
