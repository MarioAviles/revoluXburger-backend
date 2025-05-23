package com.reboluxBurger.backend.repository;

import com.reboluxBurger.backend.entity.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypeRepository extends JpaRepository<Type, Long> {
    Optional<Type> findByName(String name);
    boolean existsByName(String name);

}
