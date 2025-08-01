// package com.biotrack.backend.exceptions;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.validation.FieldError;
// import org.springframework.web.bind.MethodArgumentNotValidException;
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.bind.annotation.RestControllerAdvice;
// import org.springframework.web.context.request.WebRequest;

// import java.util.HashMap;
// import java.util.Map;

// @RestControllerAdvice
// public class GlobalExceptionHandler {

//     @ExceptionHandler(ResourceNotFoundException.class)
//     public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
//             ResourceNotFoundException ex, WebRequest request) {
//         ErrorResponse errorResponse = new ErrorResponse(
//             HttpStatus.NOT_FOUND.value(),
//             "Not Found",
//             ex.getMessage()
//         );
//         return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
//     }

//     @ExceptionHandler(MethodArgumentNotValidException.class)
//     public ResponseEntity<ErrorResponse> handleValidationExceptions(
//             MethodArgumentNotValidException ex) {
//         Map<String, String> errors = new HashMap<>();
//         ex.getBindingResult().getAllErrors().forEach((error) -> {
//             String fieldName = ((FieldError) error).getField();
//             String errorMessage = error.getDefaultMessage();
//             errors.put(fieldName, errorMessage);
//         });

//         ErrorResponse errorResponse = new ErrorResponse(
//             HttpStatus.BAD_REQUEST.value(),
//             "Validation Failed",
//             "Invalid input data: " + errors.toString()
//         );
//         return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
//     }

//     @ExceptionHandler(Exception.class)
//     public ResponseEntity<ErrorResponse> handleGlobalException(
//             Exception ex, WebRequest request) {
//         ErrorResponse errorResponse = new ErrorResponse(
//             HttpStatus.INTERNAL_SERVER_ERROR.value(),
//             "Internal Server Error",
//             "An unexpected error occurred. Please try again later."
//         );
//         return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
//     }
// }
