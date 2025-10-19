package util;

import java.io.*;

/**
 * Utilitario para serializar y deserializar objetos en disco.
 * Provee métodos genéricos usados por controladores cuando se requiere persistencia binaria.
 */
public final class Serializador {
    private Serializador() { }

    public static void guardar(Object objeto, File destino) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(destino))) {
            oos.writeObject(objeto);
            oos.flush();
        }
    }

    public static Object cargar(File origen) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(origen))) {
            return ois.readObject();
        }
    }

    public static boolean existe(File f) {
        return f != null && f.exists() && f.isFile();
    }
}