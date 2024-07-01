package com.ajua.Dromed.exceptions;

import com.ajua.Dromed.dtos.ApiResponseDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ApiResponseDTO<Object> response = new ApiResponseDTO<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiResponseDTO<Object> response = new ApiResponseDTO<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        ApiResponseDTO<Object> response = new ApiResponseDTO<>(false, "A drone with this serial number already exists.", null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleException(Exception ex) {
        ApiResponseDTO<Object> response = new ApiResponseDTO<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
