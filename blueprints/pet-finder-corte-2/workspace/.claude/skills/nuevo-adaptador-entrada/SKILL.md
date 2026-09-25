---
name: nuevo-adaptador-entrada
description: Procedimiento para agregar una nueva forma de usar Pet Finder (un controlador REST, un comando de consola, un canal nuevo como voz o chat) sin tocar el dominio ni los servicios. Úsala cuando pidan "agregar una entrada", "nuevo controlador", "nuevo endpoint", "otra forma de usar el sistema" o cuando una tarea cree un archivo en adaptadores/entrada/.
---

# Nuevo adaptador de entrada

La promesa de la arquitectura hexagonal en este proyecto es que **una entrada nueva no cambia el núcleo**. Esta skill la cumple paso a paso. Si en algún punto necesitas editar `domain/` o `application/service/`, detente: probablemente falta un puerto, y eso es una decisión que la persona responsable debe tomar.

## 1. Elige el puerto

| Quiero… | Puerto de entrada |
|---|---|
| crear, consultar, resolver o cerrar reportes | `GestionReportes` |
| registrar un avistamiento | `RegistroAvistamientos` |
| interpretar una frase y ejecutar la acción | `ProcesadorComandosVoz` |
| llenar un borrador por turnos (tarjeta viva) | `AsistenteReportes` |

Si ninguno sirve, no inventes uno aquí: pregunta.

## 2. Escribe el adaptador

- Paquete: `adaptadores/entrada/<canal>/` (por ejemplo `web`, `consola`).
- Recibe el puerto **por constructor**. Nunca importes `application.service`.
- Traduce: formato del canal → tipo del dominio → puerto → formato del canal.
- Web: DTO `record` en `adaptadores/entrada/web/dto/`; errores solo vía `ManejadorErrores` (cuerpo `{"error": "..."}`).
- Todo en español; sufijo `Controller` solo para controladores REST.

## 3. Conéctalo

- Web: Spring lo detecta con `@RestController` y le inyecta el bean del puerto que ya crea `config/ConfiguracionPetFinder` o `config/ConfiguracionAsistente`.
- Consola u otro canal sin anotaciones: crea el `@Bean` en `config/`.

## 4. Pruébalo

- Web: `@WebMvcTest(TuController.class)` con `@MockitoBean` del puerto; cubre éxito, 400, 404/409 y el JSON exacto.
- Consola: prueba unitaria con un doble del puerto.

```bash
./mvnw -q test -Dtest=TuControllerTest
```

## 5. Confirma que el núcleo no se movió

```bash
./mvnw -q test -Dtest=ReglasArquitecturaTest
git diff --stat -- src/main/java/petfinder/domain src/main/java/petfinder/application/service
```

El segundo comando no debe listar archivos. Si lista alguno, deshaz ese cambio o justifícalo con la persona responsable.

## 6. Gate

```bash
./mvnw -q clean verify
```

Termina entregando el bloque de commit y tag de la tarea; el commit lo hace la persona, nunca el agente.
