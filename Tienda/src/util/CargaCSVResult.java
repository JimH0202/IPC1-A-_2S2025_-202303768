package util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CargaCSVResult implements Serializable {
    private static final long serialVersionUID = 1L;
    public int procesadas = 0;
    public int aceptadas = 0;
    public int rechazadas = 0;
    public List<String> errores = new ArrayList<>();

    public String resumen() {
        return String.format("Procesadas:%d Aceptadas:%d Rechazadas:%d", procesadas, aceptadas, rechazadas);
    }
}
