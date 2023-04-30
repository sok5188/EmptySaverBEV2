package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository  extends JpaRepository<Department, Long> {
}
