package pe.edu.upeu.sysventas.controller;

import pe.edu.upeu.sysventas.model.Usuario;
import pe.edu.upeu.sysventas.service.IUsuarioService;

import java.util.ArrayList;
import java.util.List;

public class Credenciales {

    private static List<Usuario> usuariosCache = new ArrayList<>();

    // Cargar usuarios desde la base de datos al cache
    public static void cargarUsuarios(IUsuarioService service) {
        usuariosCache = service.findAll();
        System.out.println("Cache de usuarios actualizado. Total: " + usuariosCache.size());
    }

    // Validar usuario contra el cache
    public static Usuario validar(String user, String pass) {
        for (Usuario u : usuariosCache) {
            if (u.getUser().equals(user) && u.getClave().equals(pass)) {
                return u;
            }
        }
        return null;
    }

    // Obtener usuario del cache
    public static Usuario getUsuario(String user) {
        for (Usuario u : usuariosCache) {
            if (u.getUser().equals(user)) {
                return u;
            }
        }
        return null;
    }
}
