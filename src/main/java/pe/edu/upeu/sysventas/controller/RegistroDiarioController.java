package pe.edu.upeu.sysventas.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;
import pe.edu.upeu.sysventas.components.Toast;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
public class RegistroDiarioController {

    @FXML
    private TextArea txtRegistro;

    private String contenidoRegistro;

    public void setContenidoRegistro(double ingresos, double gastos, double ganancia, int numVentas) {
        LocalDate hoy = LocalDate.now();
        String fechaFormateada = hoy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        StringBuilder registro = new StringBuilder();
        registro.append("═══════════════════════════════════════\n");
        registro.append("    💰 LA CATEDRAL - REGISTRO DIARIO\n");
        registro.append("═══════════════════════════════════════\n\n");
        registro.append("📅 Fecha: ").append(fechaFormateada).append("\n\n");
        registro.append("───────────────────────────────────────\n");
        registro.append("  RESUMEN FINANCIERO\n");
        registro.append("───────────────────────────────────────\n\n");
        registro.append(String.format("💵 Ingresos:  S/ %.2f\n", ingresos));
        registro.append(String.format("📦 Gastos:    S/ %.2f\n", gastos));
        registro.append(String.format("💰 Ganancia:  S/ %.2f\n\n", ganancia));
        registro.append(String.format("📊 Total de ventas: %d\n\n", numVentas));
        registro.append("═══════════════════════════════════════\n");
        registro.append("\nGenerado por SysVentas - La Catedral\n");
        registro.append("Sistema de Gestión de Ventas\n");

        contenidoRegistro = registro.toString();
        txtRegistro.setText(contenidoRegistro);
    }

    @FXML
    public void imprimirRegistro(ActionEvent event) {
        try {
            PrinterJob printerJob = PrinterJob.createPrinterJob();
            if (printerJob != null && printerJob.showPrintDialog(txtRegistro.getScene().getWindow())) {
                Text text = new Text(contenidoRegistro);
                text.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");

                boolean success = printerJob.printPage(text);
                if (success) {
                    printerJob.endJob();
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    Toast.showToast(stage, "✅ Registro enviado a impresora", 2000, 200, 200);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Toast.showToast(stage, "Error al imprimir: " + e.getMessage(), 3000, 200, 200);
        }
    }

    @FXML
    public void guardarArchivo(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Registro Diario");

            // Nombre sugerido con fecha
            LocalDate hoy = LocalDate.now();
            String nombreSugerido = "Registro_LaCatedral_" +
                    hoy.format(DateTimeFormatter.ofPattern("ddMMyyyy")) + ".txt";
            fileChooser.setInitialFileName(nombreSugerido);

            // Filtros de extensión
            FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Archivo de Texto (*.txt)",
                    "*.txt");
            fileChooser.getExtensionFilters().add(txtFilter);

            // Mostrar diálogo
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(contenidoRegistro);
                    Toast.showToast(stage, "✅ Archivo guardado exitosamente", 2000, 200, 200);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Toast.showToast(stage, "Error al guardar archivo: " + e.getMessage(), 3000, 200, 200);
        }
    }

    @FXML
    public void copiarPortapapeles(ActionEvent event) {
        try {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(contenidoRegistro);
            clipboard.setContent(content);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Toast.showToast(stage, "✅ Copiado al portapapeles", 2000, 200, 200);
        } catch (Exception e) {
            e.printStackTrace();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Toast.showToast(stage, "Error al copiar: " + e.getMessage(), 3000, 200, 200);
        }
    }

    @FXML
    public void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
