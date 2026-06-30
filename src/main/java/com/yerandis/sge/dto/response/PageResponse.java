package com.yerandis.sge.dto.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wrapper para respuestas paginadas.
 * Spring devuelve un objeto Page enorme con mucha información interna.
 * Nosotros devolvemos solo lo que el frontend necesita.
 */
@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    /**
     * Constructor que recibe un Page<T> de Spring y extrae solo lo relevante.
     * El frontend recibirá exactamente estos campos.
     */
    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.first = page.isFirst();
        this.last = page.isLast();
    }
}
