package org.neueda.rest.project.controller;

import org.neueda.rest.project.entity.Customer;
import org.neueda.rest.project.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/customers")
public class CustomerController {

    @Autowired
    private CustomerService service;

    @PostMapping("/")
    public ResponseEntity<Customer> register(@RequestBody Customer customer){
        return ResponseEntity.ok(service.createCustomer(customer));
    }


}
