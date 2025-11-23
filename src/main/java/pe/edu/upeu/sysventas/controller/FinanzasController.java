package pe.edu.upeu.sysventas.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import pe.edu.upeu.sysventas.model.CierreCaja;
import pe.edu.upeu.sysventas.model.Usuario;
import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.service.ICierreCajaService;
import pe.edu.upeu.sysventas.service.IUsuarioService;
import pe.edu.upeu.sysventas.service.IVentaService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class FinanzasController implements Initializable {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private IVentaService ventaService;

    @Autowired
    private ICierreCajaService cierreCajaService;

    @Autowired
    private IUsuarioService usuarioService;

    @FXML
    private Label lblGananciaDelDia;
    @FXML
    private Label lblIngresosDelDia;
    @FXML
    private Label lblGastosDelDia;
    @FXML
    private Label lblNumVentas;
    @FXML
    private ListView<String> lvHistorialCierres;

    // Margen de ganancia fijo: 30%
    private static final double MARGEN_GANANCIA = 0.30;

    private double ingresosActuales = 0.0;
    private double gastosActuales = 0.0;
    private double gananciaActual = 0.0;
    private int numVentasActuales = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargarVentasDelDia();
        cargarHistorialCierres();
    }

    private void cargarVentasDelDia() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime desde = hoy.atStartOfDay();
        LocalDateTime hasta = hoy.atTime(23, 59, 59);

        List<Venta> ventas = ventaService.findByFechaGenerBetween(desde, hasta);

        // Calcular totales
        ingresosActuales = ventas.stream()
                .mapToDouble(Venta::getPrecioTotal)
                .sum();

        gananciaActual = ingresosActuales * MARGEN_GANANCIA;
        gastosActuales = ingresosActuales * (1 - MARGEN_GANANCIA);
        numVentasActuales = ventas.size();

        // Actualizar labels
        lblIngresosDelDia.setText(String.format("S/ %.2f", ingresosActuales));
        lblGastosDelDia.setText(String.format("S/ %.2f", gastosActuales));
        lblGananciaDelDia.setText(String.format("S/ %.2f", gananciaActual));
        lblNumVentas.setText(String.valueOf(numVentasActuales));
    }

    private void cargarHistorialCierres() {
        List<CierreCaja> cierres = cierreCajaService.findAll();

        List<String> historialItems = cierres.stream()
                .sorted((c1, c2) -> c2.getFechaCierre().compareTo(c1.getFechaCierre()))
                .map(cierre -> {
                    String fecha = cierre.getFechaCierre().format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    return String.format("📅 %s  |  💰 Ganancia: S/ %.2f  |  📊 Ventas: %d",
                            fecha,
                            cierre.getTotalGanancia(),
                            cierre.getNumVentas());
                })
                .toList();

        lvHistorialCierres.setItems(FXCollections.observableArrayList(historialItems));
    }

    @FXML
    public void cerrarCaja(ActionEvent event) {
        if (numVentasActuales == 0) {
            pe.edu.upeu.sysventas.components.Toast.showToast(
                    (Stage) ((Node) event.getSource()).getScene().getWindow(),
                    "⚠️ No hay ventas para cerrar",
                    2000, 300, 200);
            return;
        }

        try {
            // Obtener usuario administrador
            List<Usuario> usuarios = usuarioService.findAll();
            if (usuarios.isEmpty()) {
                pe.edu.upeu.sysventas.components.Toast.showToast(
                        (Stage) ((Node) event.getSource()).getScene().getWindow(),
                        "No hay usuarios registrados",
                        2000, 300, 200);
                return;
            }

            LocalDate hoy = LocalDate.now();

            // Crear registro de cierre de caja
            CierreCaja cierre = CierreCaja.builder()
                    .fechaCierre(LocalDateTime.now())
                    .fechaDesde(hoy.atStartOfDay())
                    .fechaHasta(hoy.atTime(23, 59, 59))
                    .totalIngresos(ingresosActuales)
                    .totalGastos(gastosActuales)
                    .totalGanancia(gananciaActual)
                    .numVentas(numVentasActuales)
                    .usuario(usuarios.get(0))
                    .build();

            cierreCajaService.save(cierre);

            // Reiniciar contadores
            ingresosActuales = 0.0;
            gastosActuales = 0.0;
            gananciaActual = 0.0;
            numVentasActuales = 0;

            lblIngresosDelDia.setText("S/ 0.00");
            lblGastosDelDia.setText("S/ 0.00");
            lblGananciaDelDia.setText("S/ 0.00");
            lblNumVentas.setText("0");

            // Recargar historial
            cargarHistorialCierres();

            pe.edu.upeu.sysventas.components.Toast.showToast(
                    (Stage) ((Node) event.getSource()).getScene().getWindow(),
                    "✅ Caja cerrada exitosamente",
                    2000, 300, 200);

        } catch (Exception e) {
            e.printStackTrace();
            pe.edu.upeu.sysventas.components.Toast.showToast(
                    (Stage) ((Node) event.getSource()).getScene().getWindow(),
                    "Error al cerrar caja: " + e.getMessage(),
                    3000, 300, 200);
        }
    }

    @FXML
    public void copiarRegistro(ActionEvent event) {
        try {
            // Crear nueva ventana
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/registro_diario.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            // Obtener el controlador y pasarle los datos
            RegistroDiarioController controller = loader.getController();
            controller.setContenidoRegistro(ingresosActuales, gastosActuales, gananciaActual, numVentasActuales);

            // Crear y mostrar la ventana
            Stage stage = new Stage();
            stage.setTitle("Registro Diario - La Catedral");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            pe.edu.upeu.sysventas.components.Toast.showToast(
                    (Stage) ((Node) event.getSource()).getScene().getWindow(),
                    "Error al abrir registro: " + e.getMessage(),
                    3000, 300, 200);
        }
    }

    @FXML
    public void irAProductos(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_producto.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
