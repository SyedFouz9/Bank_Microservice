package com.eazybytes.account.service;

import com.eazybytes.account.dto.CustomerDto;
import com.eazybytes.account.exceptions.ResourceNotFoundException;

public interface IAccountsService {

    /**
     *
     * @param customerDto
     */
    void createAccount(CustomerDto customerDto);

    /**
     *
     * @param mobileNumber
     * @return
     */
    CustomerDto fetchAccount(String mobileNumber) throws ResourceNotFoundException;

    boolean updateAccount(CustomerDto customerDto);

    boolean deleteAccount(String mobileNumber);
}
