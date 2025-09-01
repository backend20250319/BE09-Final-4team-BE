package com.hermes.notification.sender;

import com.hermes.notification.event.NotificationEvent;

public interface NotificationSender {

  void sendNotification(NotificationEvent event);

}