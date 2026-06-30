package com.yerandis.sge.exception;

import java.util.UUID;

/**
     * Se lanza cuando se busca un recurso por ID y no existe.
     * Ejemplo: GET /employees/999 cuando el empleado 999 no existe.
     *
     * Extiende RuntimeException (unchecked) para no obligar al caller a capturarla.
     * El GlobalExceptionHandler la captura y devuelve HTTP 404.
     */
    public class ResourceNotFoundException extends RuntimeException {

        public ResourceNotFoundException(String resource, UUID id) {
            super(String.format("%s con ID %d no encontrado ", resource, id));
        }

        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

