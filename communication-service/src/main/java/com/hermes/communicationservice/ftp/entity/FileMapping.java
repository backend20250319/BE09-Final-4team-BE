package com.hermes.communicationservice.ftp.entity;


import jakarta.persistence.*;
import lombok.*;

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
  @Column(nullable = false, unique = true)
  private String storedName;
}