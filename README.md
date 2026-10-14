# Gestión de Inmuebles

Aplicación de escritorio JavaFX para administrar apartamentos y casas. Está construida con Java 21, Maven, JDBC y H2, siguiendo arquitectura hexagonal; no utiliza ORM ni framework de inyección.

## Requisitos

- JDK 21 o superior.
- Maven 3.9 o superior.
- Entorno gráfico compatible con JavaFX para abrir la aplicación.

## Ejecutar

Desde la raíz del proyecto:

```bash
mvn clean test
mvn javafx:run
```

La base persistente se crea automáticamente en `data/inmuebles.mv.db`. La aplicación no borra los datos al iniciar. La primera inicialización crea el esquema y carga idempotentemente tres propietarios predeterminados:

- Ana María Torres (`100000001`)
- Carlos Andrés Ruiz (`100000002`)
- María Fernanda López (`100000003`)

Es posible cambiar la URL JDBC sin modificar el código:

```bash
JAVA_TOOL_OPTIONS='-Dinmuebles.db.url=jdbc:h2:file:./data/inmuebles' mvn javafx:run
```

## Funcionalidad

- Consulta tabular y recarga de inmuebles.
- Alta, edición y eliminación con confirmación.
- Apartamentos con número de piso y casas con pisos y sótano opcional.
- Cambio de Casa a Apartamento y viceversa, manteniendo el CHIP.
- Selección de propietarios y estado del inmueble.
- Validaciones de dominio, aplicación y base de datos.

La edición comienza al seleccionar una fila. El tipo elegido determina los campos específicos requeridos antes de guardar.

## Arquitectura

- `inmuebles.dominio`: entidades, enumeraciones y excepciones; no depende de infraestructura.
- `inmuebles.aplicacion.puertos.entrada`: contratos de los cinco casos de uso.
- `inmuebles.aplicacion.puertos.salida`: contratos de repositorios.
- `inmuebles.aplicacion.casos_uso`: reglas de aplicación y mapeo a DTO.
- `inmuebles.aplicacion.dto`: datos transportados entre interfaz y aplicación.
- `inmuebles.adaptadores.entrada.javafx`: aplicación, vista y controlador JavaFX.
- `inmuebles.adaptadores.salida.persistencia`: repositorios JDBC y consultas H2.
- `inmuebles.configuracion`: Singleton de conexiones e inicialización de esquema/datos.
- `src/main/resources/db/esquema.sql`: esquema SQL H2 con herencia JOINED.

Los tests de persistencia usan bases H2 en memoria con nombres exclusivos y no alteran la base de desarrollo.
