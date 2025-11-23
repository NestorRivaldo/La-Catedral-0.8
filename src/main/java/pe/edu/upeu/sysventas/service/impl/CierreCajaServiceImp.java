package pe.edu.upeu.sysventas.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.upeu.sysventas.model.CierreCaja;
import pe.edu.upeu.sysventas.repository.CierreCajaRepository;
import pe.edu.upeu.sysventas.service.ICierreCajaService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CierreCajaServiceImp implements ICierreCajaService {

    @Autowired
    private CierreCajaRepository cierreCajaRepository;

    @Override
    public CierreCaja save(CierreCaja cierreCaja) {
        return cierreCajaRepository.save(cierreCaja);
    }

    @Override
    public List<CierreCaja> findAll() {
        return cierreCajaRepository.findAll();
    }

    @Override
    public List<CierreCaja> findByFechaCierreBetween(LocalDateTime desde, LocalDateTime hasta) {
        return cierreCajaRepository.findByFechaCierreBetween(desde, hasta);
    }
}
