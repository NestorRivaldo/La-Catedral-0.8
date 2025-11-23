package pe.edu.upeu.sysventas.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pe.edu.upeu.sysventas.components.Toast;
import pe.edu.upeu.sysventas.model.*;
import pe.edu.upeu.sysventas.service.IClienteService;
import pe.edu.upeu.sysventas.service.IUsuarioService;
import pe.edu.upeu.sysventas.service.IVentaService;
import pe.edu.upeu.sysventas.service.ProductoIService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.springframework.context.ApplicationContext;

@Controller
public class ColaboradorVentaController {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ProductoIService productoService;

    @Autowired
    private IVentaService ventaService;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private IClienteService clienteService;

    @FXML
    private TextField txtBuscarProducto;

    @FXML
    private TableView<Producto> tblProductos;
    @FXML
    private TableColumn<Producto, String> colNombre;
    @FXML
    private TableColumn<Producto, Double> colPrecio;
    @FXML
    private TableColumn<Producto, Double> colStock;
    @FXML
    private TableColumn<Producto, String> colCategoria;

    @FXML
    private TableView<VentaDetalle> tblCarrito;
    @FXML
    private TableColumn<VentaDetalle, String> colCarProducto;
    @FXML
    private TableColumn<VentaDetalle, Double> colCarCantidad;
    @FXML
    private TableColumn<VentaDetalle, Double> colCarPrecio;
    @FXML
    private TableColumn<VentaDetalle, Double> colCarSubtotal;

    @FXML
    private Label lblTotal;

    private ObservableList<Producto> productosList;
    private ObservableList<VentaDetalle> carritoList;
    private Timeline autoRefresh;

    @FXML
    public void initialize() {
        initTables();
        loadProductos();
        carritoList = FXCollections.observableArrayList();
        tblCarrito.setItems(carritoList);

        // Auto-refresh cada 5 segundos
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            loadProductos();
        }));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }

    private void initTables() {
        // Tabla Productos
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("pu"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStock.setCellFactory(column -> new TableCell<Producto, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    if (item <= 0) {
                        setText("Agotado");
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setText(item.toString());
                        setStyle("");
                    }
                }
            }
        });
        colCategoria.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getCategoria().getNombre()));

        // Tabla Carrito
        colCarProducto.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getProducto().getNombre()));
        colCarCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCarPrecio.setCellValueFactory(new PropertyValueFactory<>("pu"));
        colCarSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
    }

    private void loadProductos() {
        productosList = FXCollections.observableArrayList(productoService.findAll());
        tblProductos.setItems(productosList);
    }

    @FXML
    public void buscarProducto() {
        String filtro = txtBuscarProducto.getText().toLowerCase();
        if (filtro.isEmpty()) {
            tblProductos.setItems(productosList);
        } else {
            ObservableList<Producto> filtrados = productosList
                    .filtered(p -> p.getNombre().toLowerCase().contains(filtro) ||
                            p.getCategoria().getNombre().toLowerCase().contains(filtro));
            tblProductos.setItems(filtrados);
        }
    }

    @FXML
    public void agregarAlCarrito(ActionEvent event) {
        Producto producto = tblProductos.getSelectionModel().getSelectedItem();
        if (producto == null) {
            mostrarToast(event, "Seleccione un producto");
            return;
        }

        if (producto.getStock() <= 0) {
            mostrarToast(event, "Producto sin stock");
            return;
        }

        // Verificar si ya está en el carrito
        Optional<VentaDetalle> existente = carritoList.stream()
                .filter(d -> d.getProducto().getIdProducto().equals(producto.getIdProducto()))
                .findFirst();

        if (existente.isPresent()) {
            VentaDetalle detalle = existente.get();
            if (detalle.getCantidad() + 1 > producto.getStock()) {
                mostrarToast(event, "Stock insuficiente");
                return;
            }
            detalle.setCantidad(detalle.getCantidad() + 1);
            detalle.setSubtotal(detalle.getCantidad() * detalle.getPu());
            tblCarrito.refresh();
        } else {
            VentaDetalle detalle = new VentaDetalle();
            detalle.setProducto(producto);
            detalle.setPu(producto.getPu());
            detalle.setCantidad(1.0);
            detalle.setDescuento(0.0);
            detalle.setSubtotal(producto.getPu());
            carritoList.add(detalle);
        }
        calcularTotal();
    }

    private void calcularTotal() {
        double total = carritoList.stream().mapToDouble(VentaDetalle::getSubtotal).sum();
        lblTotal.setText(String.format("Total: S/. %.2f", total));
    }

    @FXML
    public void realizarVenta(ActionEvent event) {
        if (carritoList.isEmpty()) {
            mostrarToast(event, "Carrito vacío");
            return;
        }

        try {
            // Obtener Cliente (Default o primero)
            List<Cliente> clientes = clienteService.findAll();
            if (clientes.isEmpty()) {
                mostrarToast(event, "No hay clientes registrados");
                return;
            }
            Cliente cliente = clientes.get(0); // Usamos el primero por defecto

            // Obtener Usuario (Default o primero)
            List<Usuario> usuarios = usuarioService.findAll();
            Usuario usuario = usuarios.stream()
                    .filter(u -> "maricielo".equals(u.getUser()) || "Nestor".equals(u.getUser()))
                    .findFirst()
                    .orElse(usuarios.get(0));

            double total = carritoList.stream().mapToDouble(VentaDetalle::getSubtotal).sum();
            double igv = total * 0.18; // Asumiendo IGV incluido o calculado
            double base = total - igv;

            Venta venta = new Venta();
            venta.setCliente(cliente);
            venta.setUsuario(usuario);
            venta.setFechaGener(LocalDateTime.now());
            venta.setNumDoc("001-00001"); // Generar dinámicamente en real
            venta.setSerie("F001");
            venta.setTipoDoc("Factura");
            venta.setPrecioBase(base);
            venta.setIgv(igv);
            venta.setPrecioTotal(total);

            // Asignar venta a detalles
            for (VentaDetalle detalle : carritoList) {
                detalle.setVenta(venta);
            }
            venta.setVentaDetalles(new ArrayList<>(carritoList));

            ventaService.save(venta);

            // Actualizar stock (Opcional, si no lo hace el servicio/trigger)
            for (VentaDetalle detalle : carritoList) {
                Producto p = detalle.getProducto();
                p.setStock(p.getStock() - detalle.getCantidad());
                productoService.update(p);
            }

            mostrarToast(event, "Venta realizada con éxito");
            cancelar(); // Limpiar
            loadProductos(); // Recargar stock
        } catch (Exception e) {
            e.printStackTrace();
            mostrarToast(event, "Error al realizar venta: " + e.getMessage());
        }
    }

    @FXML
    public void cancelar() {
        carritoList.clear();
        calcularTotal();
    }

    @FXML
    public void cerrarSesion(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/loginColaborador.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login Colaborador - La Catedral");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarToast(event, "Error al cerrar sesión");
        }
    }

    private void mostrarToast(ActionEvent event, String mensaje) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Toast.showToast(stage, mensaje, 2000, 200, 200);
    }
}
