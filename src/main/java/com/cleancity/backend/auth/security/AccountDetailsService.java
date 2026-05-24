package com.cleancity.backend.auth.security;

import com.cleancity.backend.auth.domain.Account;
import com.cleancity.backend.auth.repository.AccountRepository;
import com.cleancity.backend.exception.ApiException;
import com.cleancity.backend.exception.ErrorCode;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    public AccountDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found: " + email));

        if (account.getStatus() == com.cleancity.backend.auth.domain.AccountStatus.INACTIVE) {
            throw new ApiException(ErrorCode.ACCOUNT_INACTIVE);
        }

        return AccountDetailsImpl.build(account);
    }
}
