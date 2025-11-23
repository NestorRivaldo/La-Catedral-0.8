package pe.edu.upeu.sysventas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.sysventas.model.CierreCaja;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CierreCajaRepository extends JpaRepository<CierreCaja, Long> {
    List<CierreCaja> findByFechaCierreBetween(LocalDateTime desde, LocalDateTime hasta);
}
