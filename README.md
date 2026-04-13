# VitalTrail — Notification Service

Microservicio de notificaciones transaccionales por correo electrónico para la plataforma **VitalTrail**. Construido con **Spring Boot 3.4.3 / Java 21** e integrado con la API de **Mailgun** para el envío de emails renderizados con plantillas **Thymeleaf**.

---

## Índice

- [Descripción general](#descripción-general)
- [Stack tecnológico](#stack-tecnológico)
- [Arquitectura](#arquitectura)
- [API Reference](#api-reference)
- [Modelos de datos](#modelos-de-datos)
- [Manejo de errores](#manejo-de-errores)
- [Plantillas de email](#plantillas-de-email)
- [Configuración y variables de entorno](#configuración-y-variables-de-entorno)
- [Instalación y ejecución](#instalación-y-ejecución)
- [Docker](#docker)
- [Tests](#tests)
- [Estructura del proyecto](#estructura-del-proyecto)

---

## Descripción general

El Notification Service actúa como intermediario desacoplado entre el backend principal de VitalTrail y la API de Mailgun. Su única responsabilidad es recibir una solicitud de envío de email, renderizar la plantilla HTML correspondiente con los datos proporcionados y despachar el mensaje a través de Mailgun.

**Características clave:**

- Servicio **stateless**: no persiste ningún dato en base de datos
- **Un único endpoint REST** para el envío de emails
- Renderizado de plantillas HTML dinámicas mediante **Thymeleaf**
- Manejo centralizado de errores con respuestas estructuradas
- Configuración mediante variables de entorno (`.env` via `java-dotenv`)

---

## Stack tecnológico

| Tecnología | Versión | Rol |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.4.3 | Framework principal |
| Spring Web (MVC) | — | Capa REST |
| Spring Validation | — | Validación de DTOs |
| Thymeleaf | — | Motor de plantillas HTML |
| Mailgun Java SDK | 1.1.3 | Cliente HTTP para Mailgun API |
| Lombok | — | Reducción de boilerplate |
| java-dotenv | 5.2.2 | Carga de variables de entorno desde `.env` |
| spring-boot-devtools | — | Hot reload en desarrollo |
| JUnit 5 + Mockito | — | Testing |
| Maven | 3.9.x | Gestión de dependencias y build |

---

## Arquitectura

El proyecto sigue una **arquitectura por capas** con separación estricta de responsabilidades:

```
com.springboot.notification
├── api/
│   ├── exception/          # @ControllerAdvice, ErrorMessages (cuerpo de respuesta de error)
│   └── notification/       # @RestController, DTOs de entrada/salida
├── domain/
│   ├── exception/          # Error enum, NotificationException
│   └── notification/       # Interfaz del servicio + implementación
├── infra/
│   └── config/             # @Configuration: Dotenv, Mailgun, Thymeleaf
└── NotificationApplication.java
```

**Regla de dependencias:**

```
api  →  domain  ←  infra
```

- La capa `api` solo conoce las interfaces de `domain`.
- La capa `infra` provee las implementaciones de los clientes externos (Mailgun) que `domain` consume.
- La capa `domain` no importa nada de Spring MVC ni de clientes externos.

### Flujo de una solicitud de envío

```
Backend principal (WebClient)
        │
        ▼
POST /notifications/email
        │
        ▼
NotificationController
        │
        ▼
MailgunEmailServiceImpl
   ├── getEmailTemplate()  →  Thymeleaf renderiza HTML
   └── mailgunMessagesApi.sendMessage()  →  Mailgun API
        │
        ▼
 200 OK { "message": "Email enviado con id: <mailgun-id>" }
```

---

## API Reference

### `POST /notifications/email`

Envía un email transaccional renderizando la plantilla indicada con los datos proporcionados.

**URL:** `http://localhost:8081/notifications/email`

**Método:** `POST`

**Content-Type:** `application/json`

#### Request body

```json
{
  "to": "usuario@example.com",
  "subject": "Confirmación de suscripción",
  "template": "subscription-email",
  "dataSubscription": {
    "userName": "María García",
    "planName": "Plan Premium",
    "startDate": "2026-04-13"
  }
}
```

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `to` | `string` | ✅ | Dirección de email del destinatario |
| `subject` | `string` | ✅ | Asunto del correo |
| `template` | `string` | ✅ | Nombre del archivo de plantilla (sin extensión `.html`) |
| `dataSubscription` | `object` | ✅ | Variables dinámicas que se inyectan en la plantilla Thymeleaf |

#### Respuesta exitosa — `200 OK`

```json
{
  "message": "Email enviado con id: <20260413120000.1234567890@sandbox.mailgun.org>"
}
```

#### Respuestas de error

| HTTP Status | Código de error | Descripción |
|---|---|---|
| `400 Bad Request` | `INVALID_REQUEST` | Cuerpo de la solicitud inválido o parámetros incorrectos |
| `401 Unauthorized` | `UNAUTHORIZED` | API Key de Mailgun inválida o ausente |
| `403 Forbidden` | `FORBIDDEN` | Dominio no autorizado en Mailgun |
| `404 Not Found` | `ENDPOINT_NOT_FOUND` | Endpoint de Mailgun no encontrado |
| `422 Unprocessable Entity` | `<campo>` | Error de validación de campos (campo ausente o nulo) |
| `500 Internal Server Error` | `INTERNAL_SERVER_ERROR` | Error interno del servidor |
| `500 Internal Server Error` | `SERVICE_UNAVAILABLE` | Error de conexión con la API de Mailgun |

---

## Modelos de datos

### `NotificationDto.MailgunEmail`

DTO de entrada para el endpoint de envío de emails.

```java
public static class MailgunEmail {
    @NotNull String to;
    @NotNull String subject;
    @NotNull String template;
    @NotNull Map<String, Object> dataSubscription;
}
```

### `ErrorMessages`

Estructura de respuesta para todos los errores.

```json
{
  "errors": {
    "CODIGO_ERROR": "Descripción del error"
  }
}
```

---

## Manejo de errores

El manejo de errores está centralizado en `NotificationExceptionHandler` (`@ControllerAdvice`) con tres niveles de captura:

1. **`NotificationException`** — errores de dominio específicos (Mailgun API errors mapeados)
2. **`MethodArgumentNotValidException`** — errores de validación de DTOs (`@NotNull`, etc.)
3. **`RuntimeException` / `Exception`** — fallback genérico

Los errores de Mailgun (vía `FeignException`) se capturan en la capa de servicio y se convierten a `NotificationException` antes de propagarse, garantizando que nunca llegue una excepción de terceros sin mapear al cliente.

```
FeignException.BadRequest      →  Error.INVALID_REQUEST   (400)
FeignException.Unauthorized    →  Error.UNAUTHORIZED      (401)
FeignException.Forbidden       →  Error.FORBIDDEN         (403)
FeignException.NotFound        →  Error.ENDPOINT_NOT_FOUND (404)
FeignException (genérico)      →  Error.SERVICE_UNAVAILABLE (500)
Exception (checked)            →  Error.INTERNAL_SERVER_ERROR (500)
```

---

## Plantillas de email

Las plantillas se ubican en `src/main/resources/templates/` y siguen la convención `kebab-case` en minúsculas.

El nombre del archivo se pasa como valor del campo `template` en el request body. El servicio convierte el nombre a minúsculas antes de procesarlo (`template.toLowerCase()`).

### Plantilla disponible

| Nombre | Archivo | Descripción |
|---|---|---|
| `subscription-email` | `subscription-email.html` | Confirmación de suscripción |

### Variables dinámicas

Las variables se definen en el objeto `dataSubscription` del request y se acceden en la plantilla con la sintaxis inline de Thymeleaf:

```html
<!-- En la plantilla .html -->
<p>Hola, [[${userName}]]</p>
<p>Tu plan <strong>[[${planName}]]</strong> está activo desde [[${startDate}]].</p>
```

### Agregar una nueva plantilla

1. Crear el archivo `src/main/resources/templates/nombre-plantilla.html`
2. Usar `[[${variable}]]` para variables dinámicas (sintaxis inline, compatible con HTML estático)
3. Asegurarse de que el archivo esté en `kebab-case` y en minúsculas
4. Pasar `"template": "nombre-plantilla"` en el request body

---

## Configuración y variables de entorno

El servicio utiliza `java-dotenv` para cargar las variables desde un archivo `.env` en la raíz del proyecto. **No usar `@Value` para credenciales.**

### Archivo `.env`

Crear un archivo `.env` en la raíz del proyecto con las siguientes variables:

```env
SERVER_PORT=8081
MAILGUN_API_KEY=key-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
MAILGUN_DOMAIN=sandbox-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.mailgun.org
```

| Variable | Descripción | Requerida |
|---|---|---|
| `SERVER_PORT` | Puerto en que escucha el servidor (por defecto `8081`) | ✅ |
| `MAILGUN_API_KEY` | API Key privada de Mailgun | ✅ |
| `MAILGUN_DOMAIN` | Dominio de Mailgun (sandbox o producción) | ✅ |

> **Importante:** El archivo `.env` está incluido en `.gitignore`. No commitear credenciales reales.

### Obtener credenciales de Mailgun

1. Crear una cuenta en [mailgun.com](https://www.mailgun.com)
2. En el panel de control: **Sending → Domains** → copiar el dominio sandbox
3. En **API Security** → copiar la Private API Key

---

## Instalación y ejecución

### Requisitos

- **Java 21** o superior
- **Maven 3.9.x** (o usar el wrapper `./mvnw` incluido)

### Pasos

```bash
# 1. Clonar el repositorio
git clone <url-del-repositorio>
cd backend-Notifications-SpringBoot-VitalTrail

# 2. Crear el archivo de variables de entorno
cp .env.example .env
# Editar .env con tus credenciales de Mailgun

# 3. Compilar el proyecto
./mvnw clean compile

# 4. Ejecutar en modo desarrollo (con hot reload)
./mvnw spring-boot:run
```

El servidor queda disponible en: `http://localhost:8081`

### Comandos Maven útiles

```bash
# Compilar sin tests
./mvnw clean compile

# Empaquetar (genera JAR en target/)
./mvnw clean package -DskipTests

# Ejecutar todos los tests
./mvnw test

# Ejecutar una clase de tests específica
./mvnw test -Dtest=NotificationApplicationTests

# Limpiar artefactos compilados
./mvnw clean
```

---

## Docker

### Build de la imagen

```bash
docker build -f Dockerfile.mailgun -t vitaltrail-notification-service .
```

### Ejecutar el contenedor

```bash
docker run --env-file .env -p 8081:8081 vitaltrail-notification-service
```

### Docker Compose (integración con VitalTrail)

Para integrar este servicio en el stack completo de VitalTrail, agregar al `docker-compose.yaml` del proyecto raíz:

```yaml
notifications_springboot:
  build:
    context: ./backend-Notifications-SpringBoot-VitalTrail
    dockerfile: Dockerfile.mailgun
  container_name: notifications_springboot
  env_file:
    - ./backend-Notifications-SpringBoot-VitalTrail/.env
  ports:
    - "8081:8081"
  networks:
    - vitaltrail-network
```

> **Nota:** Al usar Docker Compose, el backend principal debe apuntar al nombre del servicio (`http://notifications_springboot:8081`) en lugar de `http://localhost:8081`.

---

## Tests

El proyecto utiliza **JUnit 5** con `spring-boot-starter-test` (incluye Mockito y AssertJ).

```bash
# Ejecutar todos los tests
./mvnw test
```

Los tests de contexto de Spring requieren que las variables de entorno estén disponibles. En entornos CI o tests aislados, utilizar `@TestPropertySource`:

```java
@SpringBootTest
@TestPropertySource(properties = {
    "SERVER_PORT=8081",
    "MAILGUN_API_KEY=test-key",
    "MAILGUN_DOMAIN=test.mailgun.org"
})
class MailgunEmailServiceImplTests {
    // ...
}
```

---

## Estructura del proyecto

```
backend-Notifications-SpringBoot-VitalTrail/
├── src/
│   ├── main/
│   │   ├── java/com/springboot/notification/
│   │   │   ├── api/
│   │   │   │   ├── exception/
│   │   │   │   │   ├── ErrorMessages.java          # Estructura de respuesta de error
│   │   │   │   │   └── NotificationExceptionHandler.java  # @ControllerAdvice global
│   │   │   │   └── notification/
│   │   │   │       ├── NotificationController.java  # POST /notifications/email
│   │   │   │       └── NotificationDto.java         # DTO de entrada (MailgunEmail)
│   │   │   ├── domain/
│   │   │   │   ├── exception/
│   │   │   │   │   ├── Error.java                  # Enum de códigos de error
│   │   │   │   │   └── NotificationException.java  # RuntimeException de dominio
│   │   │   │   └── notification/
│   │   │   │       ├── MailgunEmailService.java     # Interfaz del servicio
│   │   │   │       └── MailgunEmailServiceImpl.java # Lógica de envío + mapeo Feign
│   │   │   ├── infra/
│   │   │   │   └── config/
│   │   │   │       ├── DotenvConfig.java            # Bean Dotenv
│   │   │   │       ├── MailgunConfig.java           # Bean MailgunMessagesApi + domain
│   │   │   │       └── ThymeleafConfig.java         # Configuración del motor Thymeleaf
│   │   │   └── NotificationApplication.java         # Punto de entrada
│   │   └── resources/
│   │       ├── templates/
│   │       │   └── subscription-email.html          # Plantilla de suscripción
│   │       └── application.properties
│   └── test/
│       └── java/com/springboot/notification/
│           └── NotificationApplicationTests.java
├── .env                    # Variables de entorno (no commitear)
├── Dockerfile.mailgun      # Imagen Docker del servicio
├── pom.xml
├── mvnw / mvnw.cmd
└── AGENTS.md               # Guía para agentes de codificación
```

---

## Integración con el ecosistema VitalTrail

Este microservicio es consumido por el **backend principal** (`backend-SpringBoot-VitalTrail`) a través de un cliente `WebClient` reactivo con las siguientes características:

- **Endpoint:** `POST {MAILGUN_BACKEND_URL}/notifications/email`
- **Timeout:** 4 segundos
- **Reintentos:** 3 intentos con backoff exponencial
- **Modo de llamada:** bloqueante (`.block()`)

El backend principal es responsable de construir el payload de la solicitud (destinatario, asunto, nombre de plantilla y datos dinámicos) antes de delegarlo a este servicio.

---

## Licencia

Proyecto privado — VitalTrail. Todos los derechos reservados.
