package com.tatutaller.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Enviar email simple
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    /**
     * Enviar email HTML usando template
     */
    public void sendHtmlEmail(String to, String subject, String templateName, Context context)
            throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        String htmlContent = templateEngine.process(templateName, context);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }

    /**
     * Enviar notificación de reserva al profesor
     */
    public void sendBookingNotificationToTeacher(String teacherEmail, String teacherName,
            String studentName, String className, String bookingDate, String bookingTime) {
        try {
            Context context = new Context();
            context.setVariable("teacherName", teacherName);
            context.setVariable("studentName", studentName);
            context.setVariable("className", className);
            context.setVariable("bookingDate", bookingDate);
            context.setVariable("bookingTime", bookingTime);

            sendHtmlEmail(
                    teacherEmail,
                    "Nueva Reserva en TatuTaller - " + className,
                    "booking-notification-teacher",
                    context);
        } catch (MessagingException e) {
            // Fallback a email simple si falla el HTML
            String message = String.format(
                    "Hola %s,\n\n" +
                            "Tienes una nueva reserva:\n" +
                            "Estudiante: %s\n" +
                            "Clase: %s\n" +
                            "Fecha: %s\n" +
                            "Hora: %s\n\n" +
                            "Por favor, confirma esta reserva desde el panel de profesor.\n\n" +
                            "Saludos,\nTatuTaller",
                    teacherName, studentName, className, bookingDate, bookingTime);
            sendSimpleEmail(teacherEmail, "Nueva Reserva en TatuTaller - " + className, message);
        }
    }

    /**
     * Enviar confirmación de reserva al estudiante
     */
    public void sendBookingConfirmationToStudent(String studentEmail, String studentName,
            String className, String teacherName, String bookingDate, String bookingTime) {
        try {
            Context context = new Context();
            context.setVariable("studentName", studentName);
            context.setVariable("className", className);
            context.setVariable("teacherName", teacherName);
            context.setVariable("bookingDate", bookingDate);
            context.setVariable("bookingTime", bookingTime);

            sendHtmlEmail(
                    studentEmail,
                    "Reserva Confirmada - " + className + " | TatuTaller",
                    "booking-confirmation-student",
                    context);
        } catch (MessagingException e) {
            // Fallback a email simple si falla el HTML
            String message = String.format(
                    "Hola %s,\n\n" +
                            "¡Tu reserva ha sido confirmada!\n\n" +
                            "Detalles:\n" +
                            "Clase: %s\n" +
                            "Profesor: %s\n" +
                            "Fecha: %s\n" +
                            "Hora: %s\n\n" +
                            "Te esperamos en TatuTaller.\n\n" +
                            "Saludos,\nTatuTaller",
                    studentName, className, teacherName, bookingDate, bookingTime);
            sendSimpleEmail(studentEmail, "Reserva Confirmada - " + className, message);
        }
    }

    /**
     * Enviar notificación de cancelación
     */
    public void sendBookingCancellationToStudent(String studentEmail, String studentName,
            String className, String reason) {
        try {
            Context context = new Context();
            context.setVariable("studentName", studentName);
            context.setVariable("className", className);
            context.setVariable("reason", reason);

            sendHtmlEmail(
                    studentEmail,
                    "Reserva Cancelada - " + className + " | TatuTaller",
                    "booking-cancellation-student",
                    context);
        } catch (MessagingException e) {
            // Fallback a email simple si falla el HTML
            String message = String.format(
                    "Hola %s,\n\n" +
                            "Lamentamos informarte que tu reserva para la clase '%s' ha sido cancelada.\n\n" +
                            "Motivo: %s\n\n" +
                            "Puedes realizar una nueva reserva cuando gustes.\n\n" +
                            "Saludos,\nTatuTaller",
                    studentName, className, reason != null ? reason : "No especificado");
            sendSimpleEmail(studentEmail, "Reserva Cancelada - " + className, message);
        }
    }

    /**
     * Enviar email de bienvenida
     */
    public void sendWelcomeEmail(String userEmail, String userName) {
        try {
            Context context = new Context();
            context.setVariable("userName", userName);

            sendHtmlEmail(
                    userEmail,
                    "¡Bienvenido a TatuTaller!",
                    "welcome-email",
                    context);
        } catch (MessagingException e) {
            // Fallback a email simple si falla el HTML
            String message = String.format(
                    "¡Hola %s!\n\n" +
                            "¡Bienvenido a TatuTaller!\n\n" +
                            "Estamos emocionados de tenerte como parte de nuestra comunidad de ceramistas.\n" +
                            "Ahora puedes explorar nuestras clases y realizar reservas.\n\n" +
                            "¡Esperamos verte pronto en el taller!\n\n" +
                            "Saludos,\nEl equipo de TatuTaller",
                    userName);
            sendSimpleEmail(userEmail, "¡Bienvenido a TatuTaller!", message);
        }
    }
}
