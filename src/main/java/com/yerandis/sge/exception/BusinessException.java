package com.yerandis.sge.exception;


/**
 * Se lanza cuando una operación viola una regla de negocio.
 * Ejemplo: intentar crear un empleado con un email que ya existe.
 *
 * El GlobalExceptionHandler la captura y devuelve HTTP 409 Conflict
 * o HTTP 400 Bad Request según el contexto.
 */
public class BusinessException extends RuntimeException {

//    public BusinessException(String message) {
//        super(message);
//    }

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() { return code; }
}
