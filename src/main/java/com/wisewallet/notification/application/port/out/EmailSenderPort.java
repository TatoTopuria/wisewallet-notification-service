package com.wisewallet.notification.application.port.out;

/**
 * Output port for sending rendered email messages.
 */
public interface EmailSenderPort {
    void send(String to, String subject, String htmlBody);
}
