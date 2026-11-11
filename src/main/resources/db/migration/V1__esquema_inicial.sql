-- Esquema inicial de Pet Finder (Corte 3, tarea S7). Desde aquí las tablas las crea Flyway y Hibernate
-- solo las valida (spring.jpa.hibernate.ddl-auto=validate). Sale de ReporteEntity y AvistamientoEntity.
-- SQL común: el mismo archivo corre en H2 (local y pruebas) y en PostgreSQL (Compose y Render).
-- Los enum van como texto (EnumType.STRING) y sin CHECK: en H2 2.4 un CHECK con IN (...) falla con
-- "Check constraint invalid ... The database has been closed" cuando se cierra la conexión que creó la tabla.
-- Nunca se edita un archivo ya aplicado: Flyway guarda su checksum y la app no arranca si cambia.

create table reportes (
    id                  varchar(255)  not null,
    tipo                varchar(255)  not null,
    estado              varchar(255)  not null,
    fecha_creacion      timestamp(6)  not null,
    zona                varchar(255),
    referencia          varchar(255),
    latitud             float(53),
    longitud            float(53),
    descripcion         varchar(2000),
    contacto_nombre     varchar(255),
    contacto_medio      varchar(255),
    mascota_nombre      varchar(255),
    mascota_especie     varchar(255),
    mascota_raza        varchar(255),
    mascota_color       varchar(255),
    mascota_senas       varchar(255),
    descripcion_mascota varchar(2000),
    constraint pk_reportes primary key (id)
);

create index idx_reportes_estado on reportes (estado);

create table avistamientos (
    id              varchar(255)  not null,
    reporte_id      varchar(255)  not null,
    fecha_hora      timestamp(6)  not null,
    zona            varchar(255),
    referencia      varchar(255),
    latitud         float(53),
    longitud        float(53),
    descripcion     varchar(2000),
    contacto_nombre varchar(255),
    contacto_medio  varchar(255),
    tipo            varchar(255),
    constraint pk_avistamientos primary key (id),
    constraint fk_avistamientos_reporte foreign key (reporte_id) references reportes (id)
);

-- PostgreSQL no indexa solo las llaves foráneas; la lista con sus avistamientos (S8) busca por aquí.
create index idx_avistamientos_reporte on avistamientos (reporte_id);
