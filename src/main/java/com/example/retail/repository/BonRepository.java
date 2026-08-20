package com.example.retail.repository;

import com.example.retail.model.Bon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BonRepository extends JpaRepository<Bon, Long> {
}
