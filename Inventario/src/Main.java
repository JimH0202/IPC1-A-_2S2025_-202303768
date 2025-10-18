import java.util.Scanner;

public class Main {
    private static String usuarioActual; // usuario para acción
    private static String sucursal; 

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            //  pedir usuario al iniciar
            System.out.print("Ingrese su nombre de usuario: ");
            usuarioActual = scanner.nextLine();

            int opcion;
            do {
                mostrarMenuInventario();
                System.out.print("Seleccione una opción: ");

                while (!scanner.hasNextInt()) {
                    System.out.println("Entrada incorrecta. Ingrese un número.");
                    scanner.next();
                    System.out.print("Seleccione una opción: ");
                }

                opcion = scanner.nextInt();
                scanner.nextLine(); // Limpiar buffer

                switch (opcion) {
                    case 1 -> Producto.Agregarproducto(scanner, usuarioActual);
                    case 2 -> Producto.Buscarproducto(scanner, usuarioActual);
                    case 3 -> Producto.Eliminarproducto(scanner, usuarioActual);
                    case 4 -> Venta.Registrarventa(scanner, usuarioActual);
                    case 5 -> Reportes.Generarreportes(scanner, usuarioActual);
                    case 6 -> Reportes.bitacora();
                    case 7 -> mostrarDatosEstudiante();
                    case 8 -> System.out.println("Saliendo del sistema...");
                    case 9 -> sucursal();
                    default -> System.out.println("Opción Incorrecta. Intente de nuevo.");
                }

                System.out.println(); // Espacio entre acciones
            } while (opcion != 8);
        }
    }

    private static void mostrarMenuInventario() {
        System.out.println("********** MENÚ GESTIÓN DE INVENTARIO **********");
        System.out.println("1. Agregar Producto");
        System.out.println("2. Buscar Producto");
        System.out.println("3. Eliminar Producto");
        System.out.println("4. Registrar Venta");
        System.out.println("5. Generar Reportes");
        System.out.println("6. Bitácora");
        System.out.println("7. Ver datos del estudiante");
        System.out.println("8. Salir");
        system.out.println(x:"9. crear sucursal");
        System.out.println("------------------------------------------------");
    }

    private static void mostrarDatosEstudiante() {
        System.out.println("=== Datos del Estudiante ===");
        System.out.println("Nombre: Jimmy Brian Hurtarte López");
        System.out.println("Carnet: 202303768");
        System.out.println("Curso: Laboratorio de Introducción a la Programación y Computación 1");
        System.out.println("Sección: A");
    }



    private static void sucursal () {
            try (Scanner scanner = new Scanner(System.in)) {
                //  pedir usuario al iniciar
                System.out.print("Ingrese su nombre de usuario: ");
                sucursal = scanner.nextLine();
            }

        }

}
