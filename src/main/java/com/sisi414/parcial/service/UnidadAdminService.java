package com.sisi414.parcial.service;

import com.sisi414.parcial.common.PageResponse;
import com.sisi414.parcial.dto.*;
import com.sisi414.parcial.entity.*;
import com.sisi414.parcial.exception.*;
import com.sisi414.parcial.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class UnidadAdminService {

    private final UnidadAdminRepository unidadAdminRepository;
    private final OficinaRepository oficinaRepository;
    private final AuxiliarRepository auxiliarRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public UnidadAdminService(UnidadAdminRepository unidadAdminRepository, OficinaRepository oficinaRepository, AuxiliarRepository auxiliarRepository) {
        this.unidadAdminRepository = unidadAdminRepository;
        this.oficinaRepository = oficinaRepository;
        this.auxiliarRepository = auxiliarRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<UnidadAdminResponse> listar(Pageable pageable) {
        Page<UnidadAdmin> page = unidadAdminRepository.findAll(pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public UnidadAdminResponse obtener(String entidad, String unidad) {
        return toResponse(buscar(entidad, unidad));
    }

    private UnidadAdmin buscar(String entidad, String unidad) {
        return unidadAdminRepository.findById(new UnidadAdminId(entidad, unidad))
                .orElseThrow(() -> new NotFoundException("No se encontró unidad administrativa con clave [" + String.valueOf(entidad) + ", " + String.valueOf(unidad) + "]"));
    }

    @Transactional
    public UnidadAdminResponse crear(UnidadAdminRequest request) {
        if (request.entidad() == null || request.entidad().isBlank()) {
            throw new BadRequestException("El campo 'entidad' es obligatorio para crear unidad administrativa");
        }
        if (request.unidad() == null || request.unidad().isBlank()) {
            throw new BadRequestException("El campo 'unidad' es obligatorio para crear unidad administrativa");
        }
        if (unidadAdminRepository.existsById(new UnidadAdminId(request.entidad(), request.unidad()))) {
            throw new ConflictException("Ya existe unidad administrativa con clave [" + String.valueOf(request.entidad()) + ", " + String.valueOf(request.unidad()) + "]");
        }
        UnidadAdmin e = new UnidadAdmin();
        e.setEntidad(request.entidad());
        e.setUnidad(request.unidad());
        aplicar(e, request);
        validarReferencias(e);
        unidadAdminRepository.saveAndFlush(e);
        entityManager.refresh(e);
        return toResponse(e);
    }

    @Transactional
    public UnidadAdminResponse actualizar(String entidad, String unidad, UnidadAdminRequest request) {
        UnidadAdmin e = buscar(entidad, unidad);
        aplicar(e, request);
        validarReferencias(e);
        unidadAdminRepository.saveAndFlush(e);
        entityManager.refresh(e);
        return toResponse(e);
    }

    @Transactional
    public void eliminar(String entidad, String unidad) {
        UnidadAdmin e = buscar(entidad, unidad);
        if (oficinaRepository.existsByEntidadAndUnidad(e.getEntidad(), e.getUnidad())) {
            throw new ConflictException("No se puede eliminar unidad administrativa: tiene oficinas asociados");
        }
        if (auxiliarRepository.existsByEntidadAndUnidad(e.getEntidad(), e.getUnidad())) {
            throw new ConflictException("No se puede eliminar unidad administrativa: tiene auxiliares contables asociados");
        }
        unidadAdminRepository.delete(e);
    }

    private void aplicar(UnidadAdmin e, UnidadAdminRequest r) {
        e.setDescrip(r.descrip());
        e.setCiudad(r.ciudad());
        e.setEstadoUni(r.estadoUni());
    }

    private void validarReferencias(UnidadAdmin e) {
    }

    private UnidadAdminResponse toResponse(UnidadAdmin e) {
        return new UnidadAdminResponse(
                e.getEntidad(),
                e.getUnidad(),
                e.getDescrip(),
                e.getCiudad(),
                e.getEstadoUni()
        );
    }
}
