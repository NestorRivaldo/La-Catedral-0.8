package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Venta;

public interface VentaRepository extends ICrudGenericoRepository<Venta, Long> {
    java.util.List<Venta> findByFechaGenerBetween(java.time.LocalDateTime inicio, java.time.LocalDateTime fin);
}
