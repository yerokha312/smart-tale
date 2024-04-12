package dev.yerokha.smarttale.service.interfaces;

public interface NotificationService {

    void send(String to, String subject, String body);
}
