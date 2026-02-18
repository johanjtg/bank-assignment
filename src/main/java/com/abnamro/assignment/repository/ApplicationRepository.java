package com.abnamro.assignment.repository;

import com.abnamro.assignment.model.AccountApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<AccountApplication, UUID> {
}
