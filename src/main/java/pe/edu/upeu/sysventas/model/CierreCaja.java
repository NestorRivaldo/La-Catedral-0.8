package pe.edu.upeu.sysventas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "upeu_cierre_caja")
public class CierreCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cierre")
    private Long idCierre;

    @Column(name = "fecha_cierre", nullable = false)
    private LocalDateTime fechaCierre;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDateTime fechaDesde;

    @Column(name = "fecha_hasta", nullable = false)
    private LocalDateTime fechaHasta;

    @Column(name = "total_ingresos", nullable = false)
    private Double totalIngresos;

    @Column(name = "total_gastos", nullable = false)
    private Double totalGastos;

    @Column(name = "total_ganancia", nullable = false)
    private Double totalGanancia;

    @Column(name = "num_ventas", nullable = false)
    private Integer numVentas;

    @ManyToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", nullable = false, foreignKey = @ForeignKey(name = "FK_USUARIO_CIERRE"))
    private Usuario usuario;
}
