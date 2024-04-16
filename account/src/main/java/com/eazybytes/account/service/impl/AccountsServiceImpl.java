package com.eazybytes.account.service.impl;

import com.eazybytes.account.constants.AccountsConstants;
import com.eazybytes.account.dto.AccountsDto;
import com.eazybytes.account.dto.CustomerDto;
import com.eazybytes.account.entity.Accounts;
import com.eazybytes.account.entity.Customer;
import com.eazybytes.account.exceptions.CustomerAlreadyExistsException;
import com.eazybytes.account.exceptions.ResourceNotFoundException;
import com.eazybytes.account.mapper.AccountsMapper;
import com.eazybytes.account.mapper.CustomerMapper;
import com.eazybytes.account.repository.AccountsRepository;
import com.eazybytes.account.repository.CustomerRepository;
import com.eazybytes.account.service.IAccountsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class AccountsServiceImpl implements IAccountsService {

    private CustomerRepository customerRepository;

    private AccountsRepository accountsRepository;

    @Autowired
    public AccountsServiceImpl(CustomerRepository customerRepository, AccountsRepository accountsRepository) {
        this.customerRepository = customerRepository;
        this.accountsRepository = accountsRepository;
    }

    /**
     * @param customerDto
     */
    @Override
    public void createAccount(CustomerDto customerDto) {
        Customer customer = CustomerMapper.mapToCustomer(customerDto, new Customer());
        Optional<Customer> customerExits = customerRepository.findByMobileNumber(customer.getMobileNumber());
        if (customerExits.isPresent()) {
            throw new CustomerAlreadyExistsException("Customer with name :" + customerExits.get().getName()
                    + " Mob num: " + customerExits.get().getMobileNumber() + " already exits");
        }
        Customer savedCustomer = customerRepository.save(customer);

        accountsRepository.save(createNewAccount(savedCustomer));
    }

    /**
     * @param customer
     * @return
     */
    private Accounts createNewAccount(Customer customer) {
        Accounts account = new Accounts();
        account.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + new Random().nextInt(900000000);

        account.setAccountNumber(randomAccNumber);
        account.setAccountType(AccountsConstants.SAVINGS);
        account.setBranchAddress(AccountsConstants.ADDRESS);
        return account;
    }

    @Override
    public CustomerDto fetchAccount(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(() ->
                new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );

        Accounts customerAccount = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "MobileNum", mobileNumber)
        );

        CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer, new CustomerDto());
        customerDto.setAccountsDto(AccountsMapper.mapToAccountsDto(customerAccount, new AccountsDto()));
        return customerDto;
    }

    public boolean updateAccount(CustomerDto customerDto) {
        boolean isUpdated = false;
        AccountsDto accountsDto = customerDto.getAccountsDto();
        if (accountsDto != null) {
            Accounts accounts = accountsRepository.findById(accountsDto.getAccountNumber()).orElseThrow(
                    () -> new ResourceNotFoundException("Account", "AccountNumber", accountsDto.getAccountNumber().toString())
            );
            AccountsMapper.mapToAccounts(accountsDto, accounts);
            accounts = accountsRepository.save(accounts);

            Long customerId = accounts.getCustomerId();
            Customer customer = customerRepository.findById(customerId).orElseThrow(
                    () -> new ResourceNotFoundException("Customer", "CustomerID", customerId.toString())
            );
            CustomerMapper.mapToCustomer(customerDto, customer);
            customerRepository.save(customer);
            isUpdated = true;
        }
        return isUpdated;
    }

    @Override
    public boolean deleteAccount(String mobileNumber) {

        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        accountsRepository.deleteByCustomerId(customer.getCustomerId());

        //for this one framework handles itself any transactional issues since it's framework generated
        customerRepository.deleteById(customer.getCustomerId());

        return true;
    }
}
