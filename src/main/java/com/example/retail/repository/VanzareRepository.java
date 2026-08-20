package com.example.retail.repository;

import com.example.retail.model.Vanzare;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VanzareRepository extends JpaRepository<Vanzare, Long> {
    boolean existsByProdusId(Long produsId);
}
