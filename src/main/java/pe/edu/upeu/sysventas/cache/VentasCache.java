package pe.edu.upeu.sysventas.cache;

import org.springframework.stereotype.Component;
import pe.edu.upeu.sysventas.dto.VentaCacheDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Caché en memoria para almacenar las ventas del día.
 * Se usa para mostrar finanzas en tiempo real sin consultar la BD.
 */
@Component
public class VentasCache {

    // Almacena ventas por fecha
    private final ConcurrentHashMap<LocalDate, List<VentaCacheDTO>> ventasPorDia = new ConcurrentHashMap<>();

    /**
     * Agrega una venta al caché del día actual
     */
    public void agregarVenta(VentaCacheDTO venta) {
        LocalDate hoy = LocalDate.now();
        ventasPorDia.computeIfAbsent(hoy, k -> new ArrayList<>()).add(venta);
        System.out.println("✅ Venta agregada al caché: " + venta);
    }

    /**
     * Obtiene todas las ventas del día actual
     */
    public List<VentaCacheDTO> getVentasDelDia() {
        LocalDate hoy = LocalDate.now();
        return new ArrayList<>(ventasPorDia.getOrDefault(hoy, new ArrayList<>()));
    }

    /**
     * Obtiene ventas de una fecha específica
     */
    public List<VentaCacheDTO> getVentasPorFecha(LocalDate fecha) {
        return new ArrayList<>(ventasPorDia.getOrDefault(fecha, new ArrayList<>()));
    }

    /**
     * Calcula el total de ingresos del día
     */
    public double getIngresosDelDia() {
        return getVentasDelDia().stream()
                .mapToDouble(VentaCacheDTO::getTotal)
                .sum();
    }

    /**
     * Calcula la ganancia del día (30% de ingresos)
     */
    public double getGananciaDelDia() {
        return getIngresosDelDia() * 0.30;
    }

    /**
     * Calcula los gastos del día (70% de ingresos)
     */
    public double getGastosDelDia() {
        return getIngresosDelDia() * 0.70;
    }

    /**
     * Obtiene el número de ventas del día
     */
    public int getNumVentasDelDia() {
        return getVentasDelDia().size();
    }

    /**
     * Limpia el caché del día actual (usado al cerrar caja)
     */
    public void limpiarCacheDelDia() {
        LocalDate hoy = LocalDate.now();
        ventasPorDia.remove(hoy);
        System.out.println("🗑️ Caché del día limpiado");
    }

    /**
     * Obtiene todas las ventas del caché (para debug)
     */
    public int getTotalVentasEnCache() {
        return ventasPorDia.values().stream()
                .mapToInt(List::size)
                .sum();
    }
}
