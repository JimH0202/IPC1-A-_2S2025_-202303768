import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            int opcion;
            
            do {
                mostrarMenu();
                System.out.print("Seleccione una opción: ");
                
                while (!scanner.hasNextInt()) {
                    System.out.println("Entrada inválida. Ingrese un número.");
                    scanner.next();
                    System.out.print("Seleccione una opción: ");
                }
                
                opcion = scanner.nextInt();
                scanner.nextLine(); // Limpiar buffer
                
                switch (opcion) {
                    case 1 -> Personaje.agregarPersonaje(scanner);
                    case 2 -> Personaje.modificarPersonaje(scanner);
                    case 3 -> Personaje.eliminarPersonaje(scanner);
                    case 4 -> Personaje.mostrarDatosPersonaje(scanner);
                    case 5 -> Personaje.listarPersonajes();
                    case 6 -> Peleas.realizarPelea(scanner);
                    case 7 -> Peleas.verHistorial();
                    case 8 -> mostrarDatosEstudiante();
                    case 9 -> System.out.println("Saliendo del sistema...");
                    default -> System.out.println("Opción no válida. Intente de nuevo.");
                }
                
                System.out.println(); // Espacio entre acciones
                
            } while (opcion != 9);
        }
    }

    private static void mostrarMenu() {
        System.out.println("======= MENÚ PRINCIPAL =======");
        System.out.println("1. Agregar personaje");
        System.out.println("2. Modificar personaje");
        System.out.println("3. Eliminar personaje");
        System.out.println("4. Ver datos de un personaje");
        System.out.println("5. Ver listado de personajes");
        System.out.println("6. Realizar pelea entre personajes");
        System.out.println("7. Ver historial de peleas");
        System.out.println("8. Ver datos del estudiante");
        System.out.println("9. Salir");
        System.out.println("================================");
    }

    private static void mostrarDatosEstudiante() {
        System.out.println("=== Datos del Estudiante ===");
        System.out.println("Nombre: Jimmy Brian Hurtarte López");
        System.out.println("Carnet: 202303768");
        System.out.println("Curso: Laboratorio de Introducción a la Programación y Computación 1");
        System.out.println("Sección: A");
    }
}
