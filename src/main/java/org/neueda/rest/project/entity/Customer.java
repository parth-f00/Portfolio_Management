package org.neueda.rest.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;




    @Data
    @Entity
//@Table(name = "customers")
    public class Customer {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;

        @Column(unique=true)
        private String email;

        private String password;

        @OneToMany(mappedBy = "customer", cascade=CascadeType.ALL,orphanRemoval = true)
        @JsonIgnore
        @ToString.Exclude
        private List<Investment>investments;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public List<Investment> getInvestments() {
            return investments;
        }

        public void setInvestments(List<Investment> investments) {
            this.investments = investments;
        }

        public Customer(Long id, String name, String email, String password, List<Investment> investments) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.password = password;
            this.investments = investments;

        }
    }


