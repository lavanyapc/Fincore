package com.smartbank.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.smartbank.dto.AccountResponse;
import com.smartbank.dto.CreateAccountRequest;
import com.smartbank.entity.Account;
import com.smartbank.entity.User;
import com.smartbank.repository.AccountRepository;
import com.smartbank.repository.UserRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository, AccountNumberGenerator accountNumberGenerator) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountNumberGenerator = accountNumberGenerator;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }

    public AccountResponse createAccount(CreateAccountRequest request) {
        User currentUser = getCurrentUser();

        Account account = new Account();
        account.setUser(currentUser);
        account.setAccountNumber(accountNumberGenerator.generate());
        account.setAccountType(request.getAccountType());

        accountRepository.save(account);

        return toResponse(account);
    }

    public List<AccountResponse> getMyAccounts() {
        User currentUser = getCurrentUser();
        return accountRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus()
        );
    }
}