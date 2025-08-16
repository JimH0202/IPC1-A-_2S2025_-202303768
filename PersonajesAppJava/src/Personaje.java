import java.util.Scanner;

public class Personaje {
    public static final int MAX_PERSONAJES = 100;
    public static final int MAX_HABILIDADES = 5;

    // Arrays para guardar los datos de personajes
    static int[] ids = new int[MAX_PERSONAJES];
    static String[] nombres = new String[MAX_PERSONAJES];
    static String[] armas = new String[MAX_PERSONAJES];
    static String[][] habilidades = new String[MAX_PERSONAJES][MAX_HABILIDADES];
    static int[] nivelesPoder = new int[MAX_PERSONAJES];
    static int contadorPersonajes = 000;

    // Generar ID correlativo
    private static int generarId() {
        return contadorPersonajes + 1;
    }

    public static void agregarPersonaje(Scanner scanner) {
        if (contadorPersonajes >= MAX_PERSONAJES) {
            System.out.println("No se pueden agregar más personajes, capacidad máxima alcanzada.");
            return;
        }

        System.out.print("Ingrese nombre del personaje (único): ");
        String nombre = scanner.nextLine().trim();

        // Validar nombre único
        if (buscarIndicePorNombre(nombre) != -1) {
            System.out.println("El nombre ya existe, ingrese otro nombre.");
            return;
        }

        System.out.print("Ingrese arma del personaje: ");
        String arma = scanner.nextLine().trim();

        String[] habs = new String[MAX_HABILIDADES];
        System.out.println("Ingrese hasta 5 habilidades (deje vacío para terminar):");
        for (int i = 0; i < MAX_HABILIDADES; i++) {
            System.out.print("Habilidad " + (i + 1) + ": ");
            String hab = scanner.nextLine().trim();
            if (hab.isEmpty()) {
                break;
            }
            habs[i] = hab;
        }

        int nivelPoder;
        while (true) {
            System.out.print("Ingrese nivel de poder (1-100): ");
            if (scanner.hasNextInt()) {
                nivelPoder = scanner.nextInt();
                scanner.nextLine(); // limpiar buffer
                if (nivelPoder >= 1 && nivelPoder <= 100) {
                    break;
                } else {
                    System.out.println("Nivel de poder debe estar entre 1 y 100.");
                }
            } else {
                System.out.println("Entrada inválida, ingrese un número.");
                scanner.nextLine(); // limpiar entrada inválida
            }
        }

        // Guardar personaje en arrays
        ids[contadorPersonajes] = generarId();
        nombres[contadorPersonajes] = nombre;
        armas[contadorPersonajes] = arma;
        habilidades[contadorPersonajes] = habs;
        nivelesPoder[contadorPersonajes] = nivelPoder;

        contadorPersonajes++;
        System.out.println("Personaje agregado con éxito. ID: " + (contadorPersonajes));
    }

    // Buscar índice en arrays por nombre (retorna -1 si no existe)
    private static int buscarIndicePorNombre(String nombre) {
        for (int i = 0; i < contadorPersonajes; i++) {
            if (nombres[i].equalsIgnoreCase(nombre)) {
                return i;
            }
        }
        return -1;
    }

    // Buscar índice en arrays por ID (retorna -1 si no existe)
    private static int buscarIndicePorId(int id) {
        for (int i = 0; i < contadorPersonajes; i++) {
            if (ids[i] == id) {
                return i;
            }
        }
        return -1;
    }

    public static void modificarPersonaje(Scanner scanner) {
        if (contadorPersonajes == 0) {
            System.out.println("No hay personajes registrados.");
            return;
        }

        System.out.print("Ingrese ID del personaje a modificar: ");
        int id;
        if (scanner.hasNextInt()) {
            id = scanner.nextInt();
            scanner.nextLine(); // limpiar buffer
        } else {
            System.out.println("Entrada inválida.");
            scanner.nextLine();
            return;
        }

        int idx = buscarIndicePorId(id);
        if (idx == -1) {
            System.out.println("No se encontró personaje con ID " + id);
            return;
        }

        System.out.println("Datos actuales:");
        mostrarDatosPersonajePorIndice(idx);

        System.out.print("Ingrese nuevo arma (deje vacío para mantener actual): ");
        String armaNueva = scanner.nextLine().trim();
        if (!armaNueva.isEmpty()) {
            armas[idx] = armaNueva;
        }

        System.out.println("Ingrese hasta 5 nuevas habilidades (deje vacío para mantener actuales):");
        String[] habsNuevas = new String[MAX_HABILIDADES];
        boolean ingresoHabilidades = false;
        for (int i = 0; i < MAX_HABILIDADES; i++) {
            System.out.print("Habilidad " + (i + 1) + ": ");
            String hab = scanner.nextLine().trim();
            if (hab.isEmpty()) {
                break;
            }
            habsNuevas[i] = hab;
            ingresoHabilidades = true;
        }
        if (ingresoHabilidades) {
            habilidades[idx] = habsNuevas;
        }

        System.out.print("Ingrese nuevo nivel de poder (1-100, 0 para mantener actual): ");
        int nivelNuevo = 0;
        if (scanner.hasNextInt()) {
            nivelNuevo = scanner.nextInt();
            scanner.nextLine();
            if (nivelNuevo >= 1 && nivelNuevo <= 100) {
                nivelesPoder[idx] = nivelNuevo;
            } else if (nivelNuevo != 0) {
                System.out.println("Nivel inválido, se mantiene el nivel anterior.");
            }
        } else {
            System.out.println("Entrada inválida, se mantiene el nivel anterior.");
            scanner.nextLine();
        }

        System.out.println("Personaje modificado con éxito.");
    }

    public static void eliminarPersonaje(Scanner scanner) {
        if (contadorPersonajes == 0) {
            System.out.println("No hay personajes registrados.");
            return;
        }

        System.out.print("Ingrese ID del personaje a eliminar: ");
        int id;
        if (scanner.hasNextInt()) {
            id = scanner.nextInt();
            scanner.nextLine();
        } else {
            System.out.println("Entrada inválida.");
            scanner.nextLine();
            return;
        }

        int idx = buscarIndicePorId(id);
        if (idx == -1) {
            System.out.println("No se encontró personaje con ID " + id);
            return;
        }

        System.out.print("¿Está seguro que desea eliminar al personaje " + nombres[idx] + "? (s/n): ");
        String confirma = scanner.nextLine().trim().toLowerCase();
        if (!confirma.equals("s")) {
            System.out.println("Eliminación cancelada.");
            return;
        }

        // Mover todos los elementos posteriores una posición atrás para "eliminar"
        for (int i = idx; i < contadorPersonajes - 1; i++) {
            ids[i] = ids[i + 1];
            nombres[i] = nombres[i + 1];
            armas[i] = armas[i + 1];
            habilidades[i] = habilidades[i + 1];
            nivelesPoder[i] = nivelesPoder[i + 1];
        }
        contadorPersonajes--;

        System.out.println("Personaje eliminado correctamente.");
    }

    public static void mostrarDatosPersonaje(Scanner scanner) {
        if (contadorPersonajes == 0) {
            System.out.println("No hay personajes registrados.");
            return;
        }

        System.out.print("Ingrese ID del personaje a consultar: ");
        int id;
        if (scanner.hasNextInt()) {
            id = scanner.nextInt();
            scanner.nextLine();
        } else {
            System.out.println("Entrada inválida.");
            scanner.nextLine();
            return;
        }

        int idx = buscarIndicePorId(id);
        if (idx == -1) {
            System.out.println("No se encontró personaje con ID " + id);
            return;
        }

        mostrarDatosPersonajePorIndice(idx);
    }

    private static void mostrarDatosPersonajePorIndice(int idx) {
        System.out.println("ID: " + ids[idx]);
        System.out.println("Nombre: " + nombres[idx]);
        System.out.println("Arma: " + armas[idx]);
        System.out.println("Nivel de poder: " + nivelesPoder[idx]);
        System.out.println("Habilidades:");
        for (int i = 0; i < MAX_HABILIDADES; i++) {
            if (habilidades[idx][i] != null && !habilidades[idx][i].isEmpty()) {
                System.out.println(" - " + habilidades[idx][i]);
            }
        }
    }

    public static void listarPersonajes() {
        if (contadorPersonajes == 0) {
            System.out.println("No hay personajes registrados.");
            return;
        }

        System.out.println("Listado de personajes:");
        for (int i = 0; i < contadorPersonajes; i++) {
            System.out.println("ID: " + ids[i] + " | Nombre: " + nombres[i] + " | Nivel de poder: " + nivelesPoder[i]);
        }
    }
}
