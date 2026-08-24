# Pet Finder — Guía técnica

Instrucciones de compilación, ejecución y pruebas. La documentación conceptual del proyecto está en [`README.md`](README.md).

---

## Requisitos

| Herramienta | Versión mínima | Verificar con |
|---|---|---|
| JDK | 17 | `java -version` |
| Apache Maven | 3.8 | `mvn -v` |

El proyecto no tiene dependencias de red en tiempo de ejecución. La única dependencia externa es JUnit 5, con alcance de pruebas, y Maven la descarga en la primera compilación.

---

## Obtener el proyecto

```bash
git clone https://github.com/DrearSanti/pet-finder-dyas.git
cd pet-finder-dyas
```

---

## Compilar

```bash
mvn clean compile
```

Debe terminar en `BUILD SUCCESS`. Los archivos compilados quedan en `target/`, que está excluido del repositorio.

---

## Ejecutar

### Demostración automática

```bash
mvn compile exec:java
```

Recorre el escenario completo sin intervención del usuario. Es la forma recomendada para revisar el módulo: la salida es idéntica en cada corrida y muestra todas las funcionalidades en orden.

Lo que ocurre, paso a paso:

1. Se suscriben los dos observadores al publicador
2. Se registra un reporte de mascota perdida y se obtiene el identificador `PF-001`
3. Se registra un reporte de mascota encontrada y se obtiene `PF-002`
4. Se listan los reportes activos
5. Se registra un avistamiento sobre `PF-001`
6. Los dos observadores reaccionan: alerta al propietario y línea de auditoría
7. Se marca `PF-001` como resuelto
8. Se listan los activos de nuevo y `PF-001` ya no aparece
9. Se intenta agregar otro avistamiento sobre `PF-001` y el sistema lo rechaza

### Menú interactivo

```bash
mvn compile exec:java -Dexec.args="--menu"
```

Permite ejecutar las mismas operaciones ingresando los datos manualmente. Útil para probar validaciones y casos de error.

### Ejecutar sin Maven

Si prefieren correr las clases compiladas directamente:

```bash
java -cp target/classes petfinder.Main
java -cp target/classes petfinder.Main --menu
```

---

## Pruebas

```bash
mvn test
```

Once casos distribuidos en dos clases:

| Archivo | Casos | Qué verifica |
|---|---:|---|
| `ServicioReportesTest` | 5 | Creación polimórfica de los dos tipos, filtrado de activos, transiciones de estado y rechazo de operaciones inválidas |
| `ServicioAvistamientosTest` | 6 | Registro de avistamiento válido, notificación a los observadores, desuscripción, reporte inexistente, tipo incorrecto y reporte no activo |

Las pruebas corren sin base de datos, sin red y sin entrada de usuario. Eso es posible porque los servicios dependen de la interfaz `RepositorioReportes` y no de una implementación concreta.

Para ejecutar una sola clase de prueba:

```bash
mvn test -Dtest=ServicioAvistamientosTest
```

---

## Estructura del proyecto

```
pet-finder-dyas/
├── pom.xml                      Configuración de Maven
├── README.md                    Documentación del proyecto (Wiki)
├── README_TECNICO.md            Este archivo
├── docs/
│   ├── uml/                     Diagramas fuente en formato Mermaid
│   ├── comic/                   Presentación creativa del problema
│   └── evidencias/              Capturas de ejecución
└── src/
    ├── main/java/petfinder/
    │   ├── Main.java
    │   ├── ui/
    │   ├── application/
    │   ├── domain/
    │   │   ├── model/
    │   │   ├── factory/
    │   │   ├── observer/
    │   │   ├── repository/
    │   │   └── exception/
    │   └── infrastructure/persistence/
    └── test/java/petfinder/application/
```

---

## Notas de configuración

**Versión de Java.** El `pom.xml` fija origen y destino en 17. Si alguien tiene un JDK anterior, la compilación falla con un mensaje sobre la versión de clase. Los `record` y el `instanceof` con patrón requieren 16 o superior.

**Codificación.** El proyecto usa UTF-8, declarado en el `pom.xml`. Si en Windows la consola muestra caracteres extraños en lugar de tildes, ejecutar antes:

```powershell
chcp 65001
```

**Almacenamiento.** Los datos existen únicamente durante la ejecución. Al cerrar el programa se pierden. Es el comportamiento esperado para este corte.

---

## Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `No goals have been specified` o `there is no POM in this directory` | El comando se ejecutó fuera de la raíz del proyecto | Ubicarse en la carpeta que contiene `pom.xml` |
| `The declared package does not match the expected package` | El archivo está en una carpeta que no corresponde a su declaración `package` | Mover el archivo a la ruta que coincide con el paquete |
| VS Code marca errores pero `mvn compile` da `BUILD SUCCESS` | Caché del analizador de Java del editor | Paleta de comandos y ejecutar `Java: Clean Java Language Server Workspace` |
| `ClassNotFoundException: petfinder.Main` | Se ejecutó antes de compilar | Correr `mvn compile` primero |