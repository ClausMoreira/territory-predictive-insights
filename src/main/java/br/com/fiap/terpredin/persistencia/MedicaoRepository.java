package br.com.fiap.terpredin.persistencia;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.fiap.terpredin.dominio.Medicao;

/**
 * Acesso às medições gravadas. O Spring Data JPA gera a implementação
 * a partir do nome dos métodos.
 */
public interface MedicaoRepository extends JpaRepository<Medicao, Long> {

    /** Histórico de um sensor em ordem cronológica (mais antiga primeiro). */
    List<Medicao> findBySensorIdOrderByDataHoraAscIdAsc(String sensorId);
}