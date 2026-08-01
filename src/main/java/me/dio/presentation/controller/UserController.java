package me.dio.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import me.dio.application.usecase.CreateUserUseCase;
import me.dio.application.usecase.ManageCardUseCase;
import me.dio.application.usecase.TransferFundsUseCase;
import me.dio.domain.model.Card;
import me.dio.domain.model.User;
import me.dio.presentation.dto.CardDto;
import me.dio.presentation.dto.TransferDto;
import me.dio.presentation.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@CrossOrigin
@RestController
@RequestMapping("/users")
@Tag(name = "Users Controller", description = "RESTful API for managing users, accounts, cards, and transactions.")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final TransferFundsUseCase transferFundsUseCase;
    private final ManageCardUseCase manageCardUseCase;

    public UserController(
            CreateUserUseCase createUserUseCase,
            TransferFundsUseCase transferFundsUseCase,
            ManageCardUseCase manageCardUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.transferFundsUseCase = transferFundsUseCase;
        this.manageCardUseCase = manageCardUseCase;
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a list of all registered users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation")
    })
    public ResponseEntity<List<UserDto>> findAll() {
        List<User> users = createUserUseCase.findAll();
        List<UserDto> dtos = users.stream().map(UserDto::new).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID", description = "Retrieve a specific user based on their unique ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDto> findById(@PathVariable Long id) {
        User user = createUserUseCase.findById(id);
        return ResponseEntity.ok(new UserDto(user));
    }

    @PostMapping
    @Operation(summary = "Create a new user", description = "Create a new user and return the created user's data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "422", description = "Invalid user data provided")
    })
    public ResponseEntity<UserDto> create(@RequestBody UserDto userDto) {
        User user = userDto.toModel();
        User createdUser = createUserUseCase.create(user);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();
        return ResponseEntity.created(location).body(new UserDto(createdUser));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user by ID", description = "Delete an existing user based on their unique ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        createUserUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds (Pix)", description = "Transfer money between accounts, validating daily limit and balance")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transfer completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid accounts or insufficient balance"),
        @ApiResponse(responseCode = "422", description = "Pix limit exceeded")
    })
    public ResponseEntity<Void> transfer(@RequestBody TransferDto transferDto) {
        transferFundsUseCase.transfer(
                transferDto.sourceAccountNumber(),
                transferDto.destinationAccountNumber(),
                transferDto.amount()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/card/block")
    @Operation(summary = "Block user credit card", description = "Block the user's credit card preventing operations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Card blocked successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<CardDto> blockCard(@PathVariable Long id) {
        Card card = manageCardUseCase.blockCard(id);
        return ResponseEntity.ok(new CardDto(card));
    }

    @PostMapping("/{id}/card/unblock")
    @Operation(summary = "Unblock user credit card", description = "Unblock the user's credit card enabling operations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Card unblocked successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<CardDto> unblockCard(@PathVariable Long id) {
        Card card = manageCardUseCase.unblockCard(id);
        return ResponseEntity.ok(new CardDto(card));
    }

    @PutMapping("/{id}/card/limit")
    @Operation(summary = "Update card limit", description = "Set a new credit limit for the card if active and below approval limit")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Card limit updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid limit amount or blocked card"),
        @ApiResponse(responseCode = "422", description = "Limit exceeds maximum allowed approval limit")
    })
    public ResponseEntity<CardDto> updateCardLimit(
            @PathVariable Long id,
            @RequestParam BigDecimal newLimit) {
        Card card = manageCardUseCase.updateCardLimit(id, newLimit);
        return ResponseEntity.ok(new CardDto(card));
    }
}
