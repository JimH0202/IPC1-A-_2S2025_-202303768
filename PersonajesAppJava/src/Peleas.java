import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Peleas {

    static final int MAX_PELEAS = 100;

    static int[] idPersonaje1 = new int[MAX_PELEAS];
    static int[] idPersonaje2 = new int[MAX_PELEAS];
    static String[] fechaHora = new String[MAX_PELEAS];
    static int contadorPeleas = 0;

    public static void realizarPelea(Scanner scanner) {
        if (Personaje.contadorPersonajes < 2) {
            System.out.println("Se necesitan al menos 2 personajes registrados para una pelea.");
            return;
        }

        System.out.println("Ingrese el ID del primer personaje:");
        int id1 = leerId(scanner);
        if (!existePersonaje(id1)) {
            System.out.println("ID no válido para el primer personaje.");
            return;
        }

        System.out.println("Ingrese el ID del segundo personaje:");
        int id2 = leerId(scanner);
        if (!existePersonaje(id2)) {
            System.out.println("ID no válido para el segundo personaje.");
            return;
        }

        if (id1 == id2) {
            System.out.println("Un personaje no puede pelear contra sí mismo.");
            return;
        }

        if (contadorPeleas >= MAX_PELEAS) {
            System.out.println("No se pueden registrar más peleas. Capacidad máxima alcanzada.");
            return;
        }

        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fecha = ahora.format(formato);

        idPersonaje1[contadorPeleas] = id1;
        idPersonaje2[contadorPeleas] = id2;
        fechaHora[contadorPeleas] = fecha;

        contadorPeleas++;

        System.out.println("Pelea registrada con éxito el " + fecha);
    }

    public static void verHistorial() {
        if (contadorPeleas == 0) {
            System.out.println("No hay peleas registradas.");
            return;
        }

        System.out.println("=== Historial de Peleas ===");
        for (int i = 0; i < contadorPeleas; i++) {
            String nombre1 = obtenerNombrePorId(idPersonaje1[i]);
            String nombre2 = obtenerNombrePorId(idPersonaje2[i]);
            System.out.println("[" + fechaHora[i] + "] " + nombre1 + " vs " + nombre2);
        }
    }

    private static boolean existePersonaje(int id) {
        for (int i = 0; i < Personaje.contadorPersonajes; i++) {
            if (Personaje.ids[i] == id) {
                return true;
            }
        }
        return false;
    }

    private static String obtenerNombrePorId(int id) {
        for (int i = 0; i < Personaje.contadorPersonajes; i++) {
            if (Personaje.ids[i] == id) {
                return Personaje.nombres[i];
            }
        }
        return "Desconocido";
    }

    private static int leerId(Scanner scanner) {
        int id;
        if (scanner.hasNextInt()) {
            id = scanner.nextInt();
            scanner.nextLine(); // Limpiar buffer
            return id;
        } else {
            scanner.nextLine(); // Limpiar buffer de entrada inválida
            return -1;
        }
    }
}
