package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.Patient;
import com.biotrack.backend.services.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${email.from}")
    private String emailFrom;

    @Value("${email.from.name}")
    private String emailFromName;

    @Value("${notification.email}")
    private String notificationEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendReportNotificationEmail(Patient patient, String specialistRecommendation, String specialistType, boolean needsSpecialist) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailFrom, emailFromName);
            helper.setTo(patient.getEmail());
            helper.setSubject("🩺 Tu reporte médico está listo - Oslo Track");

            String htmlContent = buildEmailContent(patient, specialistRecommendation, specialistType, needsSpecialist);
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error sending report notification email: " + e.getMessage(), e);
        }
    }

    private String buildEmailContent(Patient patient, String specialistRecommendation, String specialistType, boolean needsSpecialist) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html lang='es'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Reporte Médico Listo</title>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); overflow: hidden; }");
        html.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px 20px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 28px; font-weight: 300; }");
        html.append(".content { padding: 30px; }");
        html.append(".greeting { font-size: 18px; color: #333; margin-bottom: 20px; }");
        html.append(".message { font-size: 16px; line-height: 1.6; color: #555; margin-bottom: 25px; }");
        html.append(".recommendation-box { background-color: #e8f4fd; border-left: 4px solid #2196F3; padding: 20px; margin: 20px 0; border-radius: 5px; }");
        html.append(".recommendation-title { font-size: 18px; font-weight: bold; color: #1976D2; margin-bottom: 10px; }");
        html.append(".recommendation-text { font-size: 16px; color: #424242; line-height: 1.5; }");
        html.append(".cta-button { display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; text-decoration: none; padding: 15px 30px; border-radius: 25px; font-weight: bold; font-size: 16px; margin: 20px 0; transition: transform 0.2s; }");
        html.append(".cta-button:hover { transform: translateY(-2px); }");
        html.append(".footer { background-color: #f8f9fa; padding: 20px; text-align: center; font-size: 14px; color: #666; }");
        html.append(".divider { height: 1px; background-color: #e0e0e0; margin: 25px 0; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class='container'>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1>🩺 Tu Reporte Médico</h1>");
        html.append("<p>Oslo Track - Cuidando tu salud</p>");
        html.append("</div>");
        
        // Content
        html.append("<div class='content'>");
        html.append("<div class='greeting'>Hola ").append(patient.getFirstName()).append(",</div>");
        
        html.append("<div class='message'>");
        html.append("¡Excelentes noticias! Tu reporte médico ya está listo y disponible en tu portal de paciente. ");
        html.append("Nuestro equipo médico ha analizado cuidadosamente tus resultados y hemos preparado un reporte ");
        html.append("detallado que te ayudará a entender mejor tu estado de salud actual.");
        html.append("</div>");
        
        // Specialist recommendation section (only if needed)
        if (needsSpecialist && specialistType != null && !specialistType.trim().isEmpty()) {
            html.append("<div class='recommendation-box'>");
            html.append("<div class='recommendation-title'>💡 Recomendación Médica</div>");
            html.append("<div class='recommendation-text'>");
            html.append("Basado en los resultados de tus análisis, recomendamos agendar una consulta con un <strong>");
            html.append(specialistType).append("</strong>.");
            if (specialistRecommendation != null && !specialistRecommendation.trim().isEmpty()) {
                html.append("<br><br>");
                html.append(specialistRecommendation);
            }
            html.append("</div>");
            html.append("</div>");
            
            // CTA Button for scheduling
            html.append("<div style='text-align: center; margin: 30px 0;'>");
            html.append("<a href='https://d3nv49w8q0y7qp.cloudfront.net/patient/schedule' class='cta-button'>");
            html.append("📅 Agendar Cita con ").append(specialistType);
            html.append("</a>");
            html.append("</div>");
        } else {
            html.append("<div class='message'>");
            html.append("Tus resultados se encuentran dentro de parámetros normales. Te recomendamos mantener ");
            html.append("tus hábitos saludables y continuar con tus chequeos médicos regulares.");
            html.append("</div>");
        }
        
        html.append("<div class='divider'></div>");
        
        html.append("<div class='message'>");
        html.append("<strong>¿Qué puedes hacer ahora?</strong><br>");
        html.append("• Revisa tu reporte completo en el portal de paciente<br>");
        html.append("• Si tienes preguntas, no dudes en contactar a tu médico<br>");
        if (needsSpecialist) {
            html.append("• Considera agendar la cita recomendada para un seguimiento especializado<br>");
        }
        html.append("• Mantén este reporte para futuras consultas médicas");
        html.append("</div>");
        
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p><strong>Oslo Track</strong><br>");
        html.append("Tecnología médica al servicio de tu salud</p>");
        html.append("<p style='font-size: 12px; color: #999;'>");
        html.append("Este es un correo automático, por favor no respondas a este mensaje. ");
        html.append("Para consultas médicas, contacta directamente a tu proveedor de salud.");
        html.append("</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
}