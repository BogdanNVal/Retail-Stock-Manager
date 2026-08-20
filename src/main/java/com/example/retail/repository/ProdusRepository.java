package com.example.retail.repository;

import com.example.retail.model.Produs;
import org.springframework.data.jpa.repository.JpaRepository;


 /// Repository pattern

public interface ProdusRepository extends JpaRepository<Produs, Long> {
    boolean existsByCodEan(String codEan);
}
