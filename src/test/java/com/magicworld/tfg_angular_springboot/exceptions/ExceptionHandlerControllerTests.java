package com.magicworld.tfg_angular_springboot.exceptions;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Epic("Manejo de Excepciones")
@Feature("Controlador de Excepciones")
public class ExceptionHandlerControllerTests {

    private ExceptionHandlerController exceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.servlet.multipart.max-file-size", "10MB");
        exceptionHandler = new ExceptionHandlerController(env);
        webRequest = new ServletWebRequest(new MockHttpServletRequest());
    }

    @Test
    @Story("Excepciones de Recurso No Encontrado")
    @Description("Verifica que ResourceNotFoundException retorna 404")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("ResourceNotFoundException retorna 404")
    void testHandleResourceNotFoundExceptionReturns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("error.NOT_FOUND");
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones de Recurso No Encontrado")
    @Description("Verifica que ResourceNotFoundException contiene código de error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("ResourceNotFoundException contiene código")
    void testHandleResourceNotFoundExceptionContainsCode() {
        ResourceNotFoundException ex = new ResourceNotFoundException("error.resource.not.found");
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertNotNull(response.getBody());
        assertEquals("error.resource.not.found", response.getBody().getCode());
    }

    @Test
    @Story("Excepciones de Solicitud Incorrecta")
    @Description("Verifica que BadRequestException retorna 400")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("BadRequestException retorna 400")
    void testHandleBadRequestExceptionReturns400() {
        BadRequestException ex = new BadRequestException("error.bad");
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones de Credenciales")
    @Description("Verifica que InvalidCredentialsException retorna 401")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("InvalidCredentialsException retorna 401")
    void testHandleInvalidCredentialsExceptionReturns401() {
        InvalidCredentialsException ex = new InvalidCredentialsException();
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones de Conflicto")
    @Description("Verifica que EmailAlreadyExistsException retorna 409")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("EmailAlreadyExistsException retorna 409")
    void testHandleEmailAlreadyExistsExceptionReturns409() {
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("test@test.com");
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones de Conflicto")
    @Description("Verifica que UsernameAlreadyExistsException retorna 409")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("UsernameAlreadyExistsException retorna 409")
    void testHandleUsernameAlreadyExistsExceptionReturns409() {
        UsernameAlreadyExistsException ex = new UsernameAlreadyExistsException("testuser");
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones de Validación")
    @Description("Verifica que ValidationException retorna 400")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("ValidationException retorna 400")
    void testHandleValidationExceptionReturns400() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be null");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleValidationException(ex, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones de Validación")
    @Description("Verifica que ValidationException contiene código de validación")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("ValidationException contiene código")
    void testHandleValidationExceptionContainsValidationCode() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be null");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleValidationException(ex, webRequest);
        assertNotNull(response.getBody());
        assertEquals("error.validation", response.getBody().getCode());
    }

    @Test
    @Story("Excepciones de Validación")
    @Description("Verifica que ConstraintViolation retorna 400")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("ConstraintViolation retorna 400")
    void testHandleConstraintViolationReturns400() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("field");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be valid");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleConstraintViolation(ex, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones de Tamaño de Archivo")
    @Description("Verifica que MaxUploadSizeExceededException retorna 413")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("MaxUploadSizeExceededException retorna 413")
    void testHandleMaxUploadReturns413() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(10 * 1024 * 1024);
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleMaxUpload(ex, webRequest);
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones de Tamaño de Archivo")
    @Description("Verifica que MaxUploadSizeExceededException contiene código")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("MaxUploadSizeExceededException contiene código")
    void testHandleMaxUploadContainsSizeExceededCode() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(10 * 1024 * 1024);
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleMaxUpload(ex, webRequest);
        assertEquals("error.file.size_exceeded", response.getBody().getCode());
    }

    @Test
    @Story("Excepciones de Multipart")
    @Description("Verifica que MultipartException con causa de tamaño retorna 413")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("MultipartException con causa de tamaño retorna 413")
    void testHandleMultipartExceptionWithMaxUploadCauseReturns413() {
        MaxUploadSizeExceededException cause = new MaxUploadSizeExceededException(10 * 1024 * 1024);
        MultipartException ex = new MultipartException("Size exceeded", cause);
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleMultipartException(ex, webRequest);
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones de Multipart")
    @Description("Verifica que MultipartException con mensaje de tamaño retorna 413")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("MultipartException con mensaje de tamaño retorna 413")
    void testHandleMultipartExceptionWithSizeMessageReturns413() {
        MultipartException ex = new MultipartException("Maximum upload size exceeded");
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleMultipartException(ex, webRequest);
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones de Multipart")
    @Description("Verifica que MultipartException genérica retorna 400")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("MultipartException genérica retorna 400")
    void testHandleMultipartExceptionGenericReturns400() {
        MultipartException ex = new MultipartException("Some error");
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleMultipartException(ex, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones No Capturadas")
    @Description("Verifica que excepción no capturada retorna 500")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Excepción no capturada retorna 500")
    void testHandleAllUncaughtReturns500() {
        Exception ex = new RuntimeException("Unexpected error");
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleAllUncaught(ex, webRequest);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones No Capturadas")
    @Description("Verifica que excepción no capturada contiene código interno")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Excepción no capturada contiene código interno")
    void testHandleAllUncaughtContainsInternalCode() {
        Exception ex = new RuntimeException("Unexpected error");
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleAllUncaught(ex, webRequest);
        assertEquals("error.internal", response.getBody().getCode());
    }

    @Test
    @Story("Excepciones de Token")
    @Description("Verifica que InvalidTokenException retorna 401")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("InvalidTokenException retorna 401")
    void testHandleInvalidTokenExceptionReturns401() {
        InvalidTokenException ex = new InvalidTokenException();
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones de Contraseña")
    @Description("Verifica que PasswordsDoNotMatchException retorna 400")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("PasswordsDoNotMatchException retorna 400")
    void testHandlePasswordsDoNotMatchExceptionReturns400() {
        PasswordsDoNoMatchException ex = new PasswordsDoNoMatchException();
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones de Archivo")
    @Description("Verifica que FileStorageException retorna 400")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("FileStorageException retorna 400")
    void testHandleFileStorageExceptionReturns400() {
        FileStorageException ex = new FileStorageException("error.file");
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones de Descuentos")
    @Description("Verifica que NoDiscountsException retorna 409")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("NoDiscountsException retorna 409")
    void testHandleNoDiscountsExceptionReturns409() {
        NoDiscountsCanBeAssignedToTicketTypeException ex = new NoDiscountsCanBeAssignedToTicketTypeException();
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones de Tipos de Entrada")
    @Description("Verifica que AtLeastOneTicketTypeException retorna 400")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("AtLeastOneTicketTypeException retorna 400")
    void testHandleAtLeastOneTicketTypeExceptionReturns400() {
        AtLeastOneTicketTypeMustBeProvidedException ex = new AtLeastOneTicketTypeMustBeProvidedException();
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones de Rate Limiting")
    @Description("Verifica que TooManyRequestsException retorna 429")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("TooManyRequestsException retorna 429")
    void testHandleTooManyRequestsExceptionReturns429() {
        TooManyRequestsException ex = new TooManyRequestsException();
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.getStatusCode().value());
    }

    @Test
    @Story("Excepciones de Rate Limiting")
    @Description("Verifica que TooManyRequestsException contiene código")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("TooManyRequestsException contiene código")
    void testHandleTooManyRequestsExceptionContainsCode() {
        TooManyRequestsException ex = new TooManyRequestsException();
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertEquals("error.too.many.requests", response.getBody().getCode());
    }

    @Test
    @Story("Excepciones de Validación")
    @Description("Verifica que ConstraintViolation contiene código")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("ConstraintViolation contiene código")
    void testHandleConstraintViolationContainsCode() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("field");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be valid");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleConstraintViolation(ex, webRequest);
        assertEquals("error.validation", response.getBody().getCode());
    }

    @Test
    @Story("Excepciones de Multipart")
    @Description("Verifica que MultipartException genérica contiene código")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("MultipartException genérica contiene código")
    void testHandleMultipartExceptionGenericContainsCode() {
        MultipartException ex = new MultipartException("Some multipart error");
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleMultipartException(ex, webRequest);
        assertEquals("error.internal", response.getBody().getCode());
    }

    @Test
    @Story("Excepciones de Token")
    @Description("Verifica que InvalidTokenException contiene código")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("InvalidTokenException contiene código")
    void testHandleInvalidTokenExceptionContainsCode() {
        InvalidTokenException ex = new InvalidTokenException();
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertEquals("error.invalid.token", response.getBody().getCode());
    }

    @Test
    @Story("Excepciones de Contraseña")
    @Description("Verifica que PasswordsDoNotMatchException contiene código")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("PasswordsDoNotMatchException contiene código")
    void testHandlePasswordsDoNotMatchExceptionContainsCode() {
        PasswordsDoNoMatchException ex = new PasswordsDoNoMatchException();
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertEquals("error.password.do.not.match", response.getBody().getCode());
    }

    @Test
    @Story("Excepciones de Descuentos")
    @Description("Verifica que NoDiscountsException contiene código")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("NoDiscountsException contiene código")
    void testHandleNoDiscountsExceptionContainsCode() {
        NoDiscountsCanBeAssignedToTicketTypeException ex = new NoDiscountsCanBeAssignedToTicketTypeException();
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertEquals("error.ticket_type.discounts.assigned", response.getBody().getCode());
    }

    @Test
    @Story("Excepciones de Tipos de Entrada")
    @Description("Verifica que AtLeastOneTicketTypeException contiene código")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("AtLeastOneTicketTypeException contiene código")
    void testHandleAtLeastOneTicketTypeExceptionContainsCode() {
        AtLeastOneTicketTypeMustBeProvidedException ex = new AtLeastOneTicketTypeMustBeProvidedException();
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertEquals("error.at.least.one.ticket.type.must.be.provided", response.getBody().getCode());
    }

    @Test
    @Story("Mensaje de Error")
    @Description("Verifica que ErrorMessage contiene timestamp")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("ErrorMessage contiene timestamp")
    void testErrorMessageContainsTimestamp() {
        ResourceNotFoundException ex = new ResourceNotFoundException("test");
        ResponseEntity<ErrorMessage> response = exceptionHandler.handleApiException(ex, webRequest);
        assertNotNull(response.getBody().getTimestamp());
    }
}

