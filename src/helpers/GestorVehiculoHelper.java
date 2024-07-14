// src/helpers/GestorVehiculoHelper.java

package helpers;

import data.GestorVehiculo;
import data.VehiculoNoEncontradoException;
import data.VehiculoYaExistenteException;
import interfaces.ArriendoInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import model.Vehiculo;
import model.VehiculoCarga;
import model.VehiculoPasajeros;

public class GestorVehiculoHelper {

    private GestorVehiculo gestorVehiculo;
    private Scanner scanner;

    public GestorVehiculoHelper(GestorVehiculo gestorVehiculo) {
        this.gestorVehiculo = gestorVehiculo;
        this.scanner = new Scanner(System.in);
    }

    public void agregarVehiculo() throws IOException {
        System.out.println("Ingrese tipo de vehículo (carga/pasajeros):");
        String tipo = scanner.nextLine().toLowerCase();

        // Validar que el tipo sea carga o pasajeros
        while (!tipo.equals("carga") && !tipo.equals("pasajeros")) {
            System.out.println("Tipo de vehículo no válido. Ingrese carga o pasajeros:");
            tipo = scanner.nextLine().toLowerCase();
        }

        System.out.println("Ingrese patente:");
        String patente = scanner.nextLine();
        System.out.println("Ingrese marca:");
        String marca = scanner.nextLine();
        System.out.println("Ingrese modelo:");
        String modelo = scanner.nextLine();
        System.out.println("Ingrese precio diario:");
        double precioDiario = scanner.nextDouble();
        scanner.nextLine();  // Consumir el salto de línea

        Vehiculo vehiculo;
        if ("carga".equalsIgnoreCase(tipo)) {
            System.out.println("Ingrese capacidad de carga:");
            double capacidadCarga = scanner.nextDouble();
            scanner.nextLine();  // Consumir el salto de línea
            vehiculo = new VehiculoCarga(patente, marca, modelo, precioDiario, capacidadCarga, true, "Disponible");
        } else {
            System.out.println("Ingrese número de pasajeros:");
            int numeroPasajeros = scanner.nextInt();
            scanner.nextLine();  // Consumir el salto de línea
            vehiculo = new VehiculoPasajeros(patente, marca, modelo, precioDiario, numeroPasajeros, false, "Disponible");
        }

        try {
            gestorVehiculo.agregarVehiculo(vehiculo);
            System.out.println("Vehículo agregado exitosamente.");
        } catch (VehiculoYaExistenteException e) {
            System.out.println(e.getMessage());
        }
    }

    public void buscarVehiculo() {
        System.out.println("Ingrese patente del vehículo a buscar:");
        String patente = scanner.nextLine();
        try {
            Vehiculo vehiculo = gestorVehiculo.buscarVehiculo(patente);
            System.out.println("Vehículo encontrado: " + vehiculo.getDetalles());
        } catch (VehiculoNoEncontradoException e) {
            System.out.println(e.getMessage());
        }
    }

    public void eliminarVehiculo() {
        System.out.println("Ingrese patente del vehículo a eliminar:");
        String patente = scanner.nextLine();
        try {
            gestorVehiculo.eliminarVehiculo(patente);
            System.out.println("Vehículo eliminado exitosamente.");
        } catch (IOException | VehiculoNoEncontradoException e) {
            System.out.println(e.getMessage());
        }
    }

    public void listarVehiculos() {
        gestorVehiculo.listarVehiculos();
    }

    public boolean arrendarVehiculoPorPatente() {
        System.out.println("Ingrese patente del vehículo a arrendar:");
        String patente = scanner.nextLine();
        Vehiculo vehiculo = gestorVehiculo.getVehiculos().get(patente);
        if (vehiculo != null && "Disponible".equals(vehiculo.getEstado())) {
            vehiculo.setEstado("Arrendado");
            try {
                gestorVehiculo.actualizarVehiculo(vehiculo); 
                System.out.println("Vehículo arrendado exitosamente.");
                return true; // Arrendamiento exitoso
            } catch (IOException e) {
                System.err.println("Error al guardar el estado del vehículo: " + e.getMessage());
                // Opcionalmente, revertir el estado del vehículo a "Disponible"
                vehiculo.setEstado("Disponible");
            }
            catch (VehiculoNoEncontradoException e) {
                System.err.println("Error al actualizar el vehículo: " + e.getMessage());
            }
        } else {
            System.out.println("Vehículo no encontrado o no disponible para arrendar.");
        }
        return false; // No se pudo arrendar el vehículo
    }

    public boolean devolverVehiculoPorPatente() {
        System.out.println("Ingrese patente del vehículo a devolver:");
        String patente = scanner.nextLine();
        Vehiculo vehiculo = gestorVehiculo.getVehiculos().get(patente);
        if (vehiculo != null && "Arrendado".equals(vehiculo.getEstado())) {
            vehiculo.setEstado("Disponible");
            try {
                // Actualiza el estado del vehículo en el archivo CSV
                gestorVehiculo.actualizarVehiculo(vehiculo); 
                System.out.println("Vehículo devuelto exitosamente.");
                return true; // Devolución exitosa
            } catch (IOException e) {
                System.err.println("Error al guardar el estado del vehículo: " + e.getMessage());
                // Opcionalmente, revertir el estado del vehículo a "Arrendado"
                vehiculo.setEstado("Arrendado");
            }
            catch (VehiculoNoEncontradoException e) {
                System.err.println("Error al actualizar el vehículo: " + e.getMessage());
            }
        } else {
            System.out.println("Vehículo no encontrado o no está arrendado.");
        }
        return false; // No se pudo devolver el vehículo
    }

    public void mostrarBoleta() {
        System.out.println("Ingrese patente del vehículo para mostrar la boleta:");
        String patente = scanner.nextLine();
        try {
            Vehiculo vehiculo = gestorVehiculo.buscarVehiculo(patente);

            // Verificar si el vehículo implementa ArriendoInterface
            if (vehiculo instanceof ArriendoInterface) {
                ArriendoInterface vehiculoArriendo = (ArriendoInterface) vehiculo;
                vehiculoArriendo.calcularYMostrarBoleta();
            } else {
                System.out.println("El vehículo no permite calcular boleta de arriendo.");
            }
        } catch (VehiculoNoEncontradoException e) {
            System.out.println(e.getMessage());
        }
    }
    
    
    public List<Vehiculo> obtenerVehiculosArriendoLargoPlazo() {
        List<Vehiculo> vehiculosArriendoLargoPlazo = new ArrayList<>();
        for (Vehiculo vehiculo : gestorVehiculo.getVehiculos().values()) {
            if (vehiculo.isArriendoLargoPlazo()) {
                vehiculosArriendoLargoPlazo.add(vehiculo);
            }
        }
        return vehiculosArriendoLargoPlazo;
    }
    
    // Getter para obtener la lista de vehículos
    public Map<String, Vehiculo> getVehiculos() {
        return gestorVehiculo.getVehiculos();
    }

    
    
}