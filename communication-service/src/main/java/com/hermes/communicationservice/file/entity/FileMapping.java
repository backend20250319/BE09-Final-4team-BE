package com.hermes.communicationservice.file.entity;


import jakarta.persistence.*;
import lombok.*;
import com.hermes.communicationservice.file.enums.OwnerType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 클라이언트가 보낸 원본 파일명
  @Column(nullable = false)
  private String originalName;

  // 실제 저장된 UUID 파일명
  @Column(nullable = false)
  private String storedName;

  // 파일 소유자 타입
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OwnerType ownerType;

  // 파일 소유자 ID(공지사항ID, 문서ID 등)
  @Column(nullable = false)
  private Long ownerId;

}