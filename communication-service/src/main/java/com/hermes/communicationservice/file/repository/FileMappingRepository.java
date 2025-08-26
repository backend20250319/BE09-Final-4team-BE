package com.hermes.communicationservice.file.repository;


import com.hermes.communicationservice.file.entity.FileMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FileMappingRepository extends JpaRepository<FileMapping, Long> {

  Optional<FileMapping> findByOriginalName(String originalName);

  Optional<FileMapping> findByStoredName(String storedName);

  void deleteByOriginalName(String originalName);

  boolean existsByOriginalName(String originalName);

}
