package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.model.CierreCaja;

import java.time.LocalDateTime;
import java.util.List;

public interface ICierreCajaService {
    CierreCaja save(CierreCaja cierreCaja);

    List<CierreCaja> findAll();

    List<CierreCaja> findByFechaCierreBetween(LocalDateTime desde, LocalDateTime hasta);
}
