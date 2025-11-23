package pe.edu.upeu.sysventas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para almacenar ventas en el caché.
 * Contiene toda la información necesaria sin referencias a entidades JPA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaCacheDTO {

    private Long idVenta;
    private LocalDateTime fecha;
    private String cliente;
    private String usuario;
    private Double total;
    private List<ProductoVendidoDTO> productos;

    /**
     * DTO para productos vendidos
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoVendidoDTO {
        private String nombre;
        private Double cantidad;
        private Double precio;
        private Double subtotal;
    }
}
