package com.biotrack.backend.services.impl;

import com.biotrack.backend.services.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import java.util.HashMap;
import java.util.Map;

@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);
    
    private final SnsClient snsClient;
    private final boolean smsEnabled;

    public SmsServiceImpl(@Value("${aws.region:us-east-1}") String region,
                         @Value("${aws.sns.enabled:false}") boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
        
        if (smsEnabled) {
            this.snsClient = SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        } else {
            this.snsClient = null;
            logger.info("SMS notifications disabled");
        }
    }

    @Override
    public void sendReportGeneratedNotification(String phoneNumber, String patientName, String reportType) {
        if (!smsEnabled) {
            logger.info("SMS disabled. Would send report notification to {} for patient {}", phoneNumber, patientName);
            return;
        }

        String message = String.format(
            "Hola %s, tu %s ha sido generado y está disponible para consulta en tu portal médico. " +
            "Ingresa con tu cuenta para revisarlo. - BioTrack",
            patientName, reportType
        );

        sendSms(phoneNumber, message);
    }

    @Override
    public void sendAppointmentReminder(String phoneNumber, String patientName, String appointmentDate) {
        if (!smsEnabled) {
            logger.info("SMS disabled. Would send appointment reminder to {} for patient {}", phoneNumber, patientName);
            return;
        }

        String message = String.format(
            "Recordatorio: %s, tienes una cita médica programada para %s. " +
            "Confirma tu asistencia en el portal o contacta al hospital. - BioTrack",
            patientName, appointmentDate
        );

        sendSms(phoneNumber, message);
    }

    private void sendSms(String phoneNumber, String message) {
        try {
            // Asegurar formato internacional (+52 para México)
            String formattedPhone = formatPhoneNumber(phoneNumber);
            
            Map<String, MessageAttributeValue> smsAttributes = new HashMap<>();
            smsAttributes.put("AWS.SNS.SMS.SenderID", MessageAttributeValue.builder()
                .stringValue("BioTrack")
                .dataType("String")
                .build());
            smsAttributes.put("AWS.SNS.SMS.SMSType", MessageAttributeValue.builder()
                .stringValue("Transactional")
                .dataType("String")
                .build());

            PublishRequest request = PublishRequest.builder()
                .message(message)
                .phoneNumber(formattedPhone)
                .messageAttributes(smsAttributes)
                .build();

            PublishResponse response = snsClient.publish(request);
            logger.info("SMS sent successfully to {}. MessageId: {}", formattedPhone, response.messageId());

        } catch (SnsException e) {
            logger.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to send SMS notification", e);
        } catch (Exception e) {
            logger.error("Unexpected error sending SMS to {}: {}", phoneNumber, e.getMessage(), e);
            throw new RuntimeException("Unexpected error in SMS service", e);
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        // Remover espacios, guiones, paréntesis
        String clean = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");
        
        // Si empieza con +, devolverlo tal como está
        if (clean.startsWith("+")) {
            return clean;
        }
        
        // Si empieza con 52 (código México), agregar +
        if (clean.startsWith("52") && clean.length() == 12) {
            return "+" + clean;
        }
        
        // Si es número de 10 dígitos (México), agregar +52
        if (clean.length() == 10) {
            return "+52" + clean;
        }
        
        // Si no coincide con patrones conocidos, intentar con +52
        logger.warn("Phone number format unclear: {}. Trying with +52 prefix.", phoneNumber);
        return "+52" + clean;
    }
}