package org.neueda.rest.project.service;


import org.neueda.rest.project.entity.Customer;
import org.neueda.rest.project.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository repository;

    public Customer createCustomer(Customer customer){
        if(repository.existsByEmail(customer.getEmail())){
            throw new RuntimeException("Email Already taken");
        }
        return repository.save(customer);
    }

    public Customer getCustomerById(Long id){
        return repository.findById(id).orElseThrow(()->new RuntimeException("Customer not found"));
    }

}


