package com.sisi414.parcial.service;

import com.sisi414.parcial.common.PageResponse;
import com.sisi414.parcial.common.PasswordHasher;
import com.sisi414.parcial.dto.UsuarioRequest;
import com.sisi414.parcial.dto.UsuarioResponse;
import com.sisi414.parcial.entity.Usuario;
import com.sisi414.parcial.exception.BadRequestException;
import com.sisi414.parcial.exception.ConflictException;
import com.sisi414.parcial.exception.NotFoundException;
import com.sisi414.parcial.repository.UsuarioRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> listar(Pageable pageable) {
        return PageResponse.of(repository.findAll(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(String usuario) {
        return toResponse(buscar(usuario));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (request.usuario() == null || request.usuario().isBlank()) {
            throw new BadRequestException("El campo 'usuario' es obligatorio para crear usuario");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new BadRequestException("El campo 'password' es obligatorio para crear usuario");
        }
        if (repository.existsById(request.usuario())) {
            throw new ConflictException("Ya existe usuario con clave [" + request.usuario() + "]");
        }
        Usuario u = new Usuario();
        u.setUsuario(request.usuario());
        aplicar(u, request);
        return toResponse(repository.save(u));
    }

    @Transactional
    public UsuarioResponse actualizar(String usuario, UsuarioRequest request) {
        Usuario u = buscar(usuario);
        aplicar(u, request);
        return toResponse(repository.save(u));
    }

    @Transactional
    public void eliminar(String usuario) {
        repository.delete(buscar(usuario));
    }

    private Usuario buscar(String usuario) {
        return repository.findById(usuario)
                .orElseThrow(() -> new NotFoundException("No se encontró usuario con clave [" + usuario + "]"));
    }

    private void aplicar(Usuario u, UsuarioRequest r) {
        u.setCompleto(r.completo());
        u.setDes(r.des());
        u.setTipo(r.tipo());
        u.setGrupo(r.grupo());
        if (r.password() != null && !r.password().isBlank()) {
            u.setPsw(PasswordHasher.hash(r.password()));
        }
        u.setFeult(LocalDate.now());
        u.setUsuar((r.usuar() == null || r.usuar().isBlank()) ? "api" : r.usuar());
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(u.getUsuario(), u.getCompleto(), u.getDes(), u.getTipo(), u.getGrupo(),
                u.getFeult(), u.getUsuar());
    }
}
