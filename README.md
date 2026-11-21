# sisi414_parcial

API REST de **activos fijos** construida con **Spring Boot 3.5 · Java 21 · Gradle**, sobre los datos migrados desde los archivos DBF (`dbfs.zip`).

- Dependencias: Spring Web, Spring Data JPA, Validation, springdoc-openapi (Swagger UI) y H2.
- Capas: `entity` → `repository` → `service` → `controller`, con `dto` (Request/Response) entre cliente y entidades.
- Paquete base: `com.sisi414.parcial`

## Cómo ejecutarlo en IntelliJ

1. `File > Open` y elige la carpeta `sisi414_parcial` (abre el `build.gradle`).
2. Verifica que *Settings > Build Tools > Gradle > Gradle JVM* sea **JDK 21**.
3. Ejecuta `Sisi414ParcialApplication` (o `gradle bootRun`).
4. Abre:
   - Swagger UI: <http://localhost:8080/swagger-ui.html>
   - OpenAPI JSON: <http://localhost:8080/v3/api-docs>
   - Consola H2: <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:file:./data/sisi414_db`, usuario `sa`, sin clave)

## Dónde se almacenan los datos

JPA no puede leer archivos DBF, así que los datos se **migraron a una base H2 en archivo**:

- Los datos originales están en `src/main/resources/seed/seed.sql` (generado desde los DBF).
- Al primer arranque se crea `./data/sisi414_db.mv.db`, Hibernate crea las tablas y `SeedDataLoader` carga el seed **solo si la BD está vacía**.
- Los cambios hechos por la API se conservan entre reinicios. Para volver a los datos originales, borra la carpeta `data/`.
- Para usar PostgreSQL o MySQL basta cambiar la URL/driver en `application.properties` y agregar el driver.

## Cómo se relacionan las tablas

```
UnidadAdmin (entidad, unidad)
 ├─< Oficina (entidad, unidad, codOfic)
 │    ├─< Resp (entidad, unidad, codOfic, codResp)
 │    └─< Actual            (activo asignado a oficina)
 ├─< Auxiliar (entidad, unidad, codCont, codAux) >─ CodCont
 └─< Actual (entidad, unidad, codigo)
        ├─ Auxiliar · CodCont · Oficina · Resp · Estado
        ├─< Reval        (revaluaciones del activo)
        └─< Trasfe       (transferencias del activo)

CodCont ─< CtaPar >─ ObjGasto (partida)      EstEnt ─< Entidades
MotivoBaja ─< ActivoBaja                      Estado ─< Actual
Catálogos sueltos: Mes, ClaDepts (departamentos), OrganismoFin, Config, Usuario (passw)
```

- Las claves son **las naturales del DBF** (`@IdClass` compuestas), por eso las URLs llevan los campos de la clave: `/api/oficinas/{entidad}/{unidad}/{codOfic}`.
- `Config`, `ActivoBaja`, `Reval` y `Trasfe` no tienen clave única en el DBF, así que usan un `id` autogenerado.
- Las relaciones JPA (`@ManyToOne`) son **de solo lectura y sin restricción FK**, porque los datos legados tienen referencias huérfanas (ver "Observaciones"). Sirven para que los DTO muestren nombres (por ejemplo `unidadDescrip`, `oficinaNombre`).
- La integridad se valida en los `service`: al crear/modificar se comprueba que existan los registros padre, y al eliminar que no haya hijos.

## Endpoints

Todos tienen: `GET` (listar paginado `?page=0&size=20&sort=campo,asc`), `GET /{clave}`, `POST`, `PUT /{clave}` y `DELETE /{clave}`.

| Ruta | Recurso | Tabla | Clave | Registros migrados |
|---|---|---|---|---|
| `/api/unidades` | Unidades administrativas | `unidadadmin` | entidad, unidad | 4 |
| `/api/oficinas` | Oficinas | `oficina` | entidad, unidad, codOfic | 3 |
| `/api/responsables` | Responsables | `resp` | entidad, unidad, codOfic, codResp | 7 |
| `/api/auxiliares` | Auxiliares contables | `auxiliar` | entidad, unidad, codCont, codAux | 6 |
| `/api/activos` | Activos fijos | `actual` | entidad, unidad, codigo | 2 |
| `/api/cuentas-contables` | Cuentas contables | `codcont` | codCont | 39 |
| `/api/estados` | Estados del bien | `estado` | codEstado | 3 |
| `/api/meses` | Meses | `mes` | mes | 12 |
| `/api/motivos-baja` | Motivos de baja | `baja` | codBaja | 9 |
| `/api/activos-baja` | Activos dados de baja | `bajas` | id autogenerado | 0 |
| `/api/revaluaciones` | Revaluaciones | `reval` | id autogenerado | 0 |
| `/api/transferencias` | Transferencias | `trasfe` | id autogenerado | 0 |
| `/api/cuentas-partidas` | Cuentas y partidas | `cta_par` | codCont, partida, gestion | 273 |
| `/api/partidas` | Partidas de gasto | `objgasto` | gestion, partida | 98 |
| `/api/organismos-financiadores` | Organismos financiadores | `organismo_fin` | gestion, codOf | 142 |
| `/api/estructuras-entidad` | Estructuras institucionales | `est_ent` | gestion, sectorEnt, subsecEnt, areaEnt, subAreaEnt, nivelInst | 108 |
| `/api/entidades` | Entidades | `entidades` | gestion, entidad | 642 |
| `/api/departamentos` | Departamentos | `cla_depts` | codigo | 10 |
| `/api/configuracion` | Configuración | `config` | id autogenerado | 18 |
| `/api/usuarios` | Usuarios | `passw` | usuario | 1 |

Endpoints anidados:

- `GET /api/unidades/{entidad}/{unidad}/oficinas` y `/activos`
- `GET /api/oficinas/{entidad}/{unidad}/{codOfic}/responsables` y `/activos`
- `GET /api/activos/{entidad}/{unidad}/{codigo}/revaluaciones` y `/transferencias`
- `GET /api/cuentas-contables/{codCont}/auxiliares`

## DTO y datos sensibles

- **Usuarios**: la respuesta nunca incluye la contraseña. En `POST/PUT` la contraseña es solo de entrada y se guarda con hash PBKDF2 (`psw`). El hash legado del `admin` se conservó como `LEGACY:<hex>`; no es verificable y no se expone.
- **Responsables**: el CI se devuelve enmascarado (`ciEnmascarado`, solo los últimos 3 dígitos). El CI completo solo se recibe al crear/modificar.
- Los campos de auditoría (`feult`, `usuar`, `fecMod`, `usuMod`, `apiEstado`) los maneja el servidor.

## Reglas respetadas del DBF

- **Longitudes** de texto (`@Size`), **precisión decimal** (`@Digits`, p. ej. `COSTO N15.2`) y números no negativos.
- **Campos obligatorios**: las claves y los campos descriptivos principales (nombre, descripción, costo, etc.).
- **Baja lógica** en `oficina`, `resp` y `actual`: `DELETE` pone `api_estado = 3` (anulado) y el listado los oculta salvo `?incluirAnulados=true`.
- `FEULT/USUAR` se actualizan en cada alta o cambio (`usuar` por defecto `api`).
- Errores en formato JSON (ProblemDetail): 400 validación/referencia inexistente, 404 no encontrado, 409 duplicado o con dependientes.

## Observaciones sobre los datos migrados

- Los registros **marcados como borrados en el DBF no se migraron** (3 responsables, 2 entidades, 1 oficina vacía queda como `api_estado = 3`).
- **No se migraron** las tablas técnicas del programa original: `reports`, `orguser`, `data`, `backups`.
- `oficina`, `resp`, `auxiliar` y `actual` usan la unidad **`siste`**, pero en `unidadadmin` el código es **`siete`**. Por eso esas filas muestran `unidadDescrip = null`. Como la API valida referencias, para crear nuevos registros bajo `siste` primero hay que crear esa unidad (o corregir el dato).

## Pruebas

`gradle test` ejecuta pruebas de humo (MockMvc, H2 en memoria): meses cargados, 404, y que usuarios/responsables no expongan datos sensibles.
