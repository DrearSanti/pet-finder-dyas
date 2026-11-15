-- Código de gestión (Corte 3, tarea S9): solo se guarda su huella SHA-256 en hexadecimal (64 caracteres),
-- nunca el código. Admite nulos porque los casos publicados antes no tienen código: esos solo los cierra el
-- administrador. SQL común a H2 y PostgreSQL.
alter table reportes add column huella_codigo varchar(64);
