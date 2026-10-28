# ms-pedidos

Microservicio encargado de gestionar el ciclo de vida de los pedidos del sistema Cloud Native.

## Descripción

`ms-pedidos` administra la creación y actualización de pedidos, sus estados, ítems y códigos de seguimiento.

Además, actúa como productor de eventos RabbitMQ. Cuando un pedido cambia al estado `ACEPTADO`, publica un evento `PEDIDO_ACEPTADO`, que posteriormente es consumido por `ms-catalogo` para actualizar el stock.

Esto permite desacoplar la gestión de pedidos de la gestión de inventario.

## Tecnologías

- Java
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Supabase
- Spring AMQP
- RabbitMQ
- Lombok
- Docker
- GitHub Actions

## Puerto

```text
8081
```

## Responsabilidades

- Crear pedidos.
- Gestionar estados de pedidos.
- Gestionar ítems del pedido.
- Generar códigos de seguimiento.
- Persistir pedidos en PostgreSQL.
- Publicar eventos de dominio en RabbitMQ.
- Mantener desacoplada la lógica de pedidos respecto del catálogo.

## RabbitMQ

`ms-pedidos` funciona como productor de eventos.

### Exchange

```text
pedidos.events
```

Tipo:

```text
topic
```

### Routing key

```text
pedido.aceptado
```

### Cola de destino

```text
catalogo.stock.queue
```

La cola es consumida por `ms-catalogo`.

## Evento PEDIDO_ACEPTADO

El evento se publica únicamente cuando un pedido cambia correctamente al estado:

```text
ACEPTADO
```

Estructura:

```json
{
  "eventId": "UUID",
  "eventType": "PEDIDO_ACEPTADO",
  "version": 1,
  "pedidoId": 19,
  "productoId": 1,
  "cantidad": 1,
  "occurredAt": "2026-10-27T18:15:40"
}
```

## Arquitectura RabbitMQ

Los nombres utilizados por RabbitMQ se encuentran centralizados en:

```text
RabbitNames
```

La configuración se encuentra en:

```text
RabbitConfig
```

La publicación se encuentra encapsulada en:

```text
PedidoEventPublisher
```

## Flujo

```text
Pedido CREADO
      |
      v
Cambio a ACEPTADO
      |
      v
PedidoEventPublisher
      |
      v
pedidos.events
      |
      | pedido.aceptado
      v
RabbitMQ
```

`ms-pedidos` no necesita llamar directamente a `ms-catalogo` para modificar el stock.

## Base de datos

El microservicio utiliza PostgreSQL alojado en Supabase.

La persistencia es gestionada mediante Spring Data JPA y Hibernate.

## Variables de entorno

```text
SERVER_PORT
DB_URL
DB_USERNAME
DB_PASSWORD

RABBITMQ_HOST
RABBITMQ_PORT
RABBITMQ_USERNAME
RABBITMQ_PASSWORD
RABBITMQ_VHOST
```

No se deben versionar credenciales reales.

## Compilación

Windows:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Linux:

```bash
./mvnw clean package -DskipTests
```

## Docker

```bash
docker build -t ms-pedidos .
```

Ejemplo de ejecución:

```bash
docker run -d \
  --name ms-pedidos \
  --restart unless-stopped \
  --network cloudnative-network \
  -p 8081:8081 \
  --env-file pedidos.env \
  ms-pedidos
```

## Verificación RabbitMQ

Después de aceptar un pedido:

```bash
docker logs ms-pedidos --since 5m | \
grep -E "RabbitMQ|PEDIDO_ACEPTADO"
```

Ejemplo esperado:

```text
Evento PEDIDO_ACEPTADO enviado a RabbitMQ
```

## Despliegue

El microservicio se despliega en Amazon EC2 mediante Docker y GitHub Actions.

Flujo:

```text
develop
   |
   v
deploy
   |
   v
GitHub Actions
   |
   v
Amazon EC2
   |
   v
Docker
```

La rama `deploy` activa el pipeline de despliegue.

## Buenas prácticas

- Configuración RabbitMQ separada de la lógica de negocio.
- Nombres de infraestructura centralizados.
- Eventos identificados mediante UUID.
- Contratos de eventos versionados.
- Logging mediante SLF4J.
- Variables sensibles fuera del repositorio.
- Docker.
- Despliegue automatizado.