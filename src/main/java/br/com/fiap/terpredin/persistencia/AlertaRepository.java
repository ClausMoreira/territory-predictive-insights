package br.com.fiap.terpredin.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.terpredin.dominio.Alerta;

/**
 * Acesso aos alertas já emitidos.
 */
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    /** Alertas do mais recente para o mais antigo. */
    List<Alerta> findAllByOrderByDataHoraDescIdDesc();
}