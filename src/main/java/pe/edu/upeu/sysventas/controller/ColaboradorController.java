package pe.edu.upeu.sysventas.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import pe.edu.upeu.sysventas.components.Toast;
import pe.edu.upeu.sysventas.model.Perfil;
import pe.edu.upeu.sysventas.model.Usuario;
import pe.edu.upeu.sysventas.service.IPerfilService;
import pe.edu.upeu.sysventas.service.IUsuarioService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class ColaboradorController implements Initializable {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private IPerfilService perfilService;

    @FXML
    private TextField txtNombres;
    @FXML
    private TextField txtApellidoPaterno;
    @FXML
    private TextField txtApellidoMaterno;
    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtClave;
    @FXML
    private ComboBox<Perfil> cbPerfil;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnModificar;
    @FXML
    private Button btnEliminar;
    @FXML
    private TableView<Usuario> tableColaboradores;
    @FXML
    private TableColumn<Usuario, Long> colId;
    @FXML
    private TableColumn<Usuario, String> colNombres;
    @FXML
    private TableColumn<Usuario, String> colApellidos;
    @FXML
    private TableColumn<Usuario, String> colUsuario;
    @FXML
    private TableColumn<Usuario, String> colPerfil;
    @FXML
    private TableColumn<Usuario, String> colEstado;

    private Usuario usuarioSeleccionado;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("ColaboradorController inicializado.");
        initTable();
        loadPerfiles();
        loadUsuarios();
        loadUsuarios();

        // Listener para selección en tabla
        tableColaboradores.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                usuarioSeleccionado = newSelection;
                llenarCampos(usuarioSeleccionado);
            }
        });
    }

    private void initTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombres.setCellValueFactory(new PropertyValueFactory<>("nombres"));
        colApellidos.setCellValueFactory(cellData -> new SimpleStringProperty(
                (cellData.getValue().getApellidoPaterno() != null ? cellData.getValue().getApellidoPaterno() : "") + " "
                        +
                        (cellData.getValue().getApellidoMaterno() != null ? cellData.getValue().getApellidoMaterno()
                                : "")));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("user"));
        colPerfil.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getIdPerfil().getNombre()));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
    }

    private void loadPerfiles() {
        List<Perfil> perfiles = perfilService.findAll();
        cbPerfil.setItems(FXCollections.observableArrayList(perfiles));
        // Configurar cómo se muestra el objeto Perfil en el ComboBox
        cbPerfil.setConverter(new javafx.util.StringConverter<Perfil>() {
            @Override
            public String toString(Perfil perfil) {
                return perfil != null ? perfil.getNombre() : "";
            }

            @Override
            public Perfil fromString(String string) {
                return cbPerfil.getItems().stream().filter(p -> p.getNombre().equals(string)).findFirst().orElse(null);
            }
        });
    }

    private void loadUsuarios() {
        List<Usuario> usuarios = usuarioService.findAll();
        tableColaboradores.setItems(FXCollections.observableArrayList(usuarios));
    }

    private void llenarCampos(Usuario u) {
        txtNombres.setText(u.getNombres());
        txtApellidoPaterno.setText(u.getApellidoPaterno());
        txtApellidoMaterno.setText(u.getApellidoMaterno());
        txtUsuario.setText(u.getUser());
        txtClave.setText(u.getClave()); // Mostrar clave es inseguro, pero solicitado
        cbPerfil.getSelectionModel().select(u.getIdPerfil());
        // cbTipoDocumento no está mapeado en Usuario actualmente, lo dejamos opcional o
        // por defecto
    }

    private void limpiarCampos() {
        txtNombres.clear();
        txtApellidoPaterno.clear();
        txtApellidoMaterno.clear();
        cbPerfil.getSelectionModel().clearSelection();
        usuarioSeleccionado = null;
    }

    @FXML
    void guardarColaborador(ActionEvent event) {
        try {
            Usuario u = new Usuario();
            u.setNombres(txtNombres.getText());
            u.setApellidoPaterno(txtApellidoPaterno.getText());
            u.setApellidoMaterno(txtApellidoMaterno.getText());
            u.setUser(txtUsuario.getText());
            u.setClave(txtClave.getText());
            u.setEstado("Activo");
            u.setIdPerfil(cbPerfil.getValue());

            if (u.getIdPerfil() == null) {
                mostrarToast(event, "Seleccione un perfil");
                return;
            }

            usuarioService.save(u);
            Credenciales.cargarUsuarios(usuarioService); // Actualizar cache
            mostrarToast(event, "Colaborador guardado");
            loadUsuarios();
            limpiarCampos();
        } catch (Exception e) {
            mostrarToast(event, "Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void modificarColaborador(ActionEvent event) {
        if (usuarioSeleccionado == null) {
            mostrarToast(event, "Seleccione un colaborador");
            return;
        }
        try {
            usuarioSeleccionado.setNombres(txtNombres.getText());
            usuarioSeleccionado.setApellidoPaterno(txtApellidoPaterno.getText());
            usuarioSeleccionado.setApellidoMaterno(txtApellidoMaterno.getText());
            usuarioSeleccionado.setUser(txtUsuario.getText());
            usuarioSeleccionado.setClave(txtClave.getText());
            usuarioSeleccionado.setIdPerfil(cbPerfil.getValue());

            usuarioService.update(usuarioSeleccionado.getIdUsuario(), usuarioSeleccionado);
            Credenciales.cargarUsuarios(usuarioService); // Actualizar cache
            mostrarToast(event, "Colaborador modificado");
            loadUsuarios();
            limpiarCampos();
        } catch (Exception e) {
            mostrarToast(event, "Error al modificar: " + e.getMessage());
        }
    }

    @FXML
    void eliminarColaborador(ActionEvent event) {
        if (usuarioSeleccionado == null) {
            mostrarToast(event, "Seleccione un colaborador");
            return;
        }
        try {
            usuarioService.deleteById(usuarioSeleccionado.getIdUsuario());
            Credenciales.cargarUsuarios(usuarioService); // Actualizar cache
            mostrarToast(event, "Colaborador eliminado");
            loadUsuarios();
            limpiarCampos();
        } catch (Exception e) {
            mostrarToast(event, "Error al eliminar: " + e.getMessage());
        }
    }

    private void mostrarToast(ActionEvent event, String mensaje) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Toast.showToast(stage, mensaje, 2000, 200, 200);
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
