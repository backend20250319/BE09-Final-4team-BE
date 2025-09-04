package com.hermes.communicationservice.archive.repository;

import com.hermes.communicationservice.archive.entity.Archive;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArchiveRepository extends JpaRepository<Archive, Long> {

  @Query("SELECT a FROM Archive a ORDER BY a.createdAt DESC")
  List<Archive> findAllOrderByCreatedAtDesc();

  @Query("SELECT a FROM Archive a WHERE a.title LIKE %:keyword%")
  List<Archive> findByTitleContaining(@Param("keyword") String keyword);

}
