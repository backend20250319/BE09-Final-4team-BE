package com.hermes.orgservice.repository;

import com.hermes.orgservice.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByName(String name);
    
    List<Organization> findByParentIsNull();
    
    List<Organization> findByParentId(Long parentId);
    
    @Query("SELECT o FROM Organization o WHERE o.name LIKE %:keyword%")
    List<Organization> findByNameContaining(@Param("keyword") String keyword);
    
    boolean existsByName(String name);
}