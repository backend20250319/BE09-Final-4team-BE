package com.hermes.communicationservice.announcement.exception;

public class AnnouncementNotFoundException extends RuntimeException {
    public AnnouncementNotFoundException(Long id) {
        super("존재하지 않는 공지입니다. id=" + id);
    }
}
