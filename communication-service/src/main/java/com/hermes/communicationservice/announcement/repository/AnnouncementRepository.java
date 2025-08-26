package com.hermes.communicationservice.announcement.repository;

import com.hermes.communicationservice.announcement.dto.AnnouncementSummaryDto;
import com.hermes.communicationservice.announcement.entity.Announcement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

  @Query("select new com.hermes.communicationservice.announcement.dto.AnnouncementSummaryDto(" +
      "a.id, a.title, a.displayAuthor, a.views, size(a.comments), a.createdAt) " +
      "from Announcement a")
  List<AnnouncementSummaryDto> findAllAnnouncementSummary();

  @Modifying
  @Query("update Announcement a set a.views = a.views + 1 where a.id = :id") // 동시성 이슈로 db에서 직접 가져오기
  int increaseViews(@Param("id") Long id);
}
