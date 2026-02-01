package org.neueda.rest.project.repository;

import org.neueda.rest.project.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment,Long> {
}
