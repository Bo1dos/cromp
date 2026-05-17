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

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserFacade userFacade;

    @GetMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal")
    public UserResponse getUser(@PathVariable Long userId) {
        return userFacade.getById(userId);
    }

    @PutMapping("/{userId}/profile")
    @PreAuthorize("#userId == authentication.principal")
    public UserResponse updateProfile(@PathVariable Long userId,
                                    @Valid @RequestBody UpdateUserProfileRequest request) {
        return userFacade.updateProfile(request);
    }

    
    @PutMapping("/{userId}/email")
    @PreAuthorize("#userId == authentication.principal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeEmail(@PathVariable Long userId,
                            @Valid @RequestBody ChangeUserEmailRequest request) {
        userFacade.changeEmail(request);
    }

    @PutMapping("/{userId}/password")
    @PreAuthorize("#userId == authentication.principal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable Long userId,
                            @Valid @RequestBody ChangeUserPasswordRequest request) {
        userFacade.changePassword(request);
    }
}