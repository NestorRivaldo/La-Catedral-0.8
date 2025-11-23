package pe.edu.upeu.sysventas.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import pe.edu.upeu.sysventas.components.Toast;
import pe.edu.upeu.sysventas.model.Usuario;
import pe.edu.upeu.sysventas.service.IUsuarioService;

import java.io.IOException;

@Controller
public class LoginColaboradorController {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private IUsuarioService usuarioService;

    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtClave;

    @FXML
    private Button btnIngresar;

    @FXML
    public void login(ActionEvent event) {
        try {
            // Asegurar que el cache esté cargado
            if (Credenciales.getUsuario(txtUsuario.getText()) == null) {
                Credenciales.cargarUsuarios(usuarioService);
            }

            Usuario usuario = Credenciales.validar(txtUsuario.getText(), txtClave.getText());

            if (usuario != null) {
                if ("Activo".equals(usuario.getEstado())) {
                    // Redirigir a la vista de venta del colaborador
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/colaboradorVenta.fxml"));
                    loader.setControllerFactory(context::getBean);
                    Parent root = loader.load();
                    Scene scene = new Scene(root);
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("Venta Colaborador - La Catedral");
                    stage.show();
                } else {
                    Toast.showToast((Stage) ((Node) event.getSource()).getScene().getWindow(),
                            "Usuario inactivo. Contacte al administrador.", 2000, 200, 200);
                }
            } else {
                Toast.showToast((Stage) ((Node) event.getSource()).getScene().getWindow(), "Credenciales incorrectas",
                        2000, 200, 200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void regresar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            loader.setControllerFactory(context::getBean);
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
