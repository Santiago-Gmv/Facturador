# Documentacion Completa del Sistema de Facturacion

**Version:** 1.0.0  
**Stack:** Java 17 + JavaFX 21 + MariaDB + Maven  
**Arquitectura:** MVC con JavaFX FXML, controladores y modelo POJO  

---

## Indice

1. [Arquitectura General](#1-arquitectura-general)
2. [Entry Point: Main.java](#2-entry-point-mainjava)
3. [Conexion a Base de Datos](#3-conexion-a-base-de-datos)
4. [Validaciones](#4-validaciones)
5. [Login](#5-login)
6. [Registro de Usuarios](#6-registro-de-usuarios)
7. [Panel Admin](#7-panel-admin)
8. [Panel Vendedor](#8-panel-vendedor)
9. [Panel Deposito](#9-panel-deposito)
10. [Modelos de Datos](#10-modelos-de-datos)
11. [Esquema de Base de Datos](#11-esquema-de-base-de-datos)
12. [CSS - Hoja de Estilos](#12-css---hoja-de-estilos)
13. [Configuracion del Proyecto (pom.xml)](#13-configuracion-del-proyecto-pomxml)
14. [Datos de Prueba](#14-datos-de-prueba)

---

## 1. Arquitectura General

```
src/main/java/
├── app/
│   └── Main.java              ← Entry point JavaFX
├── controllers/
│   ├── LoginController.java    ← Controlador de login
│   ├── RegistroController.java ← Controlador de registro
│   ├── AdminController.java    ← CRUD + facturacion + reportes
│   ├── VendedorController.java ← Facturacion limitada + consultas
│   └── DepositoController.java ← Ingreso/egreso stock + historial
├── database/
│   └── ConexionDB.java        ← Singleton conexion MariaDB
├── models/
│   ├── Usuario.java
│   ├── Cliente.java
│   ├── Proveedor.java
│   ├── Producto.java
│   ├── Factura.java
│   ├── DetalleFactura.java
│   └── MovimientoDeposito.java
└── utils/
    └── Validaciones.java       ← Metodos de validacion estaticos

src/main/resources/
├── css/
│   └── styles.css
├── fxml/
│   ├── login.fxml
│   ├── registro.fxml
│   ├── admin.fxml
│   ├── vendedor.fxml
│   └── deposito.fxml
└── sql/
    ├── schema.sql
    └── inserts.sql
```

### Flujo de navegacion general

```
Main.java
  └── FXMLLoader.load(login.fxml)
        └── LoginController
              ├── handleIniciarSesion() → SELECT validacion
              │     ├── rol=ADMIN       → FXMLLoader(admin.fxml)
              │     ├── rol=VENDEDOR    → FXMLLoader(vendedor.fxml)
              │     ├── rol=DEPOSITARIO → FXMLLoader(deposito.fxml)
              │     └── invalido        → mostrarError()
              ├── handleRegistrarse()   → FXMLLoader(registro.fxml)
              └── abrirDashboard()      → loader.getController().inicializar()
```

---

## 2. Entry Point: Main.java

### ¿Que hace?
Arranca la aplicacion JavaFX. Carga `login.fxml`, establece la escena con el CSS global, maximiza la ventana y la muestra.

### ¿Donde se ejecuta?
- Archivo: `Main.java:13` `extends Application`
- Metodo: `Main.java:16` `start(Stage primaryStage)`
- Entry point: `Main.java:40` `main(String[] args)` → `launch(args)`

### Flujo completo paso a paso

```
1. JVM llama a Main.main()
2. JavaFX llama a start(primaryStage)
3. FXMLLoader carga /fxml/login.fxml
4. Scene se crea con root del FXML
5. scene.getStylesheets().add("/css/styles.css")
6. primaryStage.setTitle("Sistema de Facturacion")
7. primaryStage.setMaximized(true)
8. primaryStage.show()
9. Si hay error → e.printStackTrace()
```

### Codigo Java relevante

```java
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml")
            );
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm()
            );
            primaryStage.setTitle("Sistema de Facturacion");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
```

### Diagrama de flujo

```
[JVM] → [Main.main()] → [Application.launch()]
                            ↓
                       [start()]
                            ↓
              [FXMLLoader.load(login.fxml)]
                            ↓
                   [Scene + CSS + Stage]
                            ↓
                   [Stage maximizado]
                            ↓
                  [Espera interaccion usuario]
```

---

## 3. Conexion a Base de Datos

### 3.1 ConexionDB.getConexion()

### ¿Que hace?
Implementa un singleton que establece la conexion a MariaDB usando credenciales desde un archivo `.env`. Retorna la misma instancia de `Connection` mientras este abierta.

### ¿Donde se ejecuta?
- Archivo: `ConexionDB.java:21` `getConexion()`
- Libreria: `io.github.cdimascio.dotenv` para leer `.env`

### Flujo completo paso a paso

```
1. ConexionDB.conexion es null?
2. Si SI → Dotenv carga .env del directorio actual
3. └── dotenv.get("DB_HOST", "localhost")
4. └── dotenv.get("DB_PORT", "3306")
5. └── dotenv.get("DB_NAME", "facturacion_db")
6. └── dotenv.get("DB_USER", "root")
7. └── dotenv.get("DB_PASSWORD", "")
8. └── Construye URL: jdbc:mariadb://host:port/db?useSSL=false&serverTimezone=UTC
9. └── DriverManager.getConnection(url, user, password)
10. └── conexion = connection
11. Si NO → retorna conexion existente
12. Si SQLException → imprime error y retorna null
```

### Codigo Java relevante

```java
private static Connection conexion = null;

public static Connection getConexion() {
    if (conexion == null) {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .ignoreIfMissing()
                    .load();
            String host = dotenv.get("DB_HOST", "localhost");
            String port = dotenv.get("DB_PORT", "3306");
            String dbName = dotenv.get("DB_NAME", "facturacion_db");
            String user = dotenv.get("DB_USER", "root");
            String password = dotenv.get("DB_PASSWORD", "");
            String url = "jdbc:mariadb://" + host + ":" + port + "/" + dbName
                    + "?useSSL=false&serverTimezone=UTC";
            conexion = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    return conexion;
}
```

### 3.2 ConexionDB.cerrarConexion()

### ¿Que hace?
Cierra la conexion activa y pone la variable `conexion` en null.

### Codigo Java relevante

```java
public static void cerrarConexion() {
    if (conexion != null) {
        try {
            conexion.close();
            conexion = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

### Archivo .env

```
DB_HOST=localhost
DB_PORT=3306
DB_NAME=facturacion_db
DB_USER=root
DB_PASSWORD=root
```

---

## 4. Validaciones

### 4.1 Metodos de Validacion

Todas las validaciones son metodos estaticos en `Validaciones.java`

| Metodo | Regex / Logica | Uso |
|--------|---------------|-----|
| `validarSoloLetras("texto")` | `[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*` | Nombres, apellidos |
| `validarSoloNumeros("texto")` | `[0-9]*` | DNI, telefono |
| `validarEmail("email")` | `^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$` | Campos email |
| `validarTelefono("tel")` | `[0-9]{6,20}` | Telefono (6-20 digitos) |
| `validarDNI("dni")` | `[0-9]{7,15}` | DNI (7-15 digitos) |
| `validarContrasena("pass")` | length >= 8 | Contrasenas |
| `validarPrecio("precio")` | `^[0-9]+(\\.[0-9]{1,2})?$` | Precios |
| `validarCantidad("cant")` | `^[1-9][0-9]*$` | Cantidades enteras positivas |
| `validarNoVacio("texto")` | `null || trim.isEmpty()` | Campos obligatorios |

### 4.2 Filtros para TextFields

| Metodo | Regex | Se usa en |
|--------|-------|-----------|
| `configurarSoloLetras(tf)` | `[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*` | Registro, Admin, Proveedor dialog |
| `configurarSoloNumeros(tf)` | `[0-9]*` | Registro, Admin dialogs |
| `configurarSoloDecimales(tf)` | `^[0-9]*(\\.[0-9]{0,2})?$` | Admin producto dialog |
| `configurarSoloEnteros(tf)` | `^[1-9][0-9]*$` | Deposito, Admin factura, Admin producto |
| `limitarCaracteres(tf, max)` | length > max → revert | Todos los dialogs |

### 4.3 Metodos de alerta

| Metodo | Tipo Alert | Descripcion |
|--------|-----------|-------------|
| `mostrarError(titulo, msg)` | `Alert.AlertType.ERROR` | Muestra error |
| `mostrarInfo(titulo, msg)` | `Alert.AlertType.INFORMATION` | Muestra informacion |
| `mostrarConfirmacion(titulo, msg)` | `Alert.AlertType.CONFIRMATION` | Retorna `true` si OK |

### Codigo Java relevante

```java
public static boolean validarSoloLetras(String texto) {
    return texto.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*");
}

public static void configurarSoloLetras(TextField textField) {
    textField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (!validarSoloLetras(newValue)) {
            textField.setText(oldValue);
        }
    });
}

public static void configurarSoloDecimales(TextField textField) {
    textField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (newValue.isEmpty()) return;
        if (!newValue.matches("^[0-9]*(\\.[0-9]{0,2})?$")) {
            textField.setText(oldValue);
        }
    });
}

public static void configurarSoloEnteros(TextField textField) {
    textField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (newValue.isEmpty()) return;
        if (!newValue.matches("^[1-9][0-9]*$") && !newValue.equals("0")) {
            textField.setText(oldValue);
        }
    });
}
```

---

## 5. Login

### 5.1 LoginController.handleIniciarSesion()

### ¿Que hace?
Valida credenciales contra la tabla `usuarios`. Si son correctas y `activo=1`, redirige al dashboard segun el rol (ADMIN, VENDEDOR, DEPOSITARIO) y cierra la ventana de login.

### ¿Donde se ejecuta?
- FXML: `login.fxml:27` `onAction="#handleIniciarSesion"`
- Controlador: `LoginController.java:30` `handleIniciarSesion()`

### Flujo completo paso a paso

```
1. Usuario completa txtUsuario y txtContrasena
2. Click "Iniciar Sesion"
3. handleIniciarSesion()
   └── Validar no vacio (usuario y contrasena)
   └── Si vacio → mostrarError("Campos vacios") y return
   └── Connection conn = ConexionDB.getConexion()
   └── PreparedStatement stmt = conn.prepareStatement(sql)
   └── stmt.setString(1, usuario)
   └── stmt.setString(2, contrasena)
   └── ResultSet rs = stmt.executeQuery()
   └── if rs.next():
   │     └── Obtener id, nombre, apellido, usuario, rol
   │     └── switch(rol):
   │     │     ├── "ADMIN"       → abrirDashboard("/fxml/admin.fxml", nombreApellido, userId)
   │     │     ├── "VENDEDOR"    → abrirDashboard("/fxml/vendedor.fxml", nombreApellido, userId)
   │     │     ├── "DEPOSITARIO" → abrirDashboard("/fxml/deposito.fxml", nombreApellido, userId)
   │     │     └── default       → mostrarError("Rol no valido")
   │     └── Stage.close() (ventana login)
   └── else → mostrarError("Usuario o contrasena invalidos")
   └── rs.close(); stmt.close()
   └── catch Exception → mostrarError("Error de conexion")
```

### Codigo SQL exacto

```sql
SELECT id, nombre, apellido, usuario, rol
FROM usuarios
WHERE usuario = ? AND contrasena = ? AND activo = 1
```

### Codigo Java relevante

```java
@FXML
private void handleIniciarSesion() {
    String usuario = txtUsuario.getText().trim();
    String contrasena = txtContrasena.getText();

    if (!Validaciones.validarNoVacio(usuario) || !Validaciones.validarNoVacio(contrasena)) {
        Validaciones.mostrarError("Campos vacios", "Por favor ingrese usuario y contrasena");
        return;
    }

    try {
        Connection conn = ConexionDB.getConexion();
        String sql = "SELECT id, nombre, apellido, usuario, rol FROM usuarios " +
                     "WHERE usuario = ? AND contrasena = ? AND activo = 1";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, usuario);
        stmt.setString(2, contrasena);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int userId = rs.getInt("id");
            String nombre = rs.getString("nombre");
            String apellido = rs.getString("apellido");
            String rol = rs.getString("rol");

            switch (rol) {
                case "ADMIN" -> abrirDashboard("/fxml/admin.fxml", nombre + " " + apellido, userId);
                case "VENDEDOR" -> abrirDashboard("/fxml/vendedor.fxml", nombre + " " + apellido, userId);
                case "DEPOSITARIO" -> abrirDashboard("/fxml/deposito.fxml", nombre + " " + apellido, userId);
                default -> Validaciones.mostrarError("Error", "Rol de usuario no valido");
            }

            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.close();
        } else {
            Validaciones.mostrarError("Credenciales incorrectas",
                    "Usuario o contrasena invalidos");
        }
        rs.close();
        stmt.close();
    } catch (Exception e) {
        Validaciones.mostrarError("Error de conexion",
                "No se pudo conectar con la base de datos: " + e.getMessage());
    }
}
```

### FXML relacionado

```xml
<TextField fx:id="txtUsuario" promptText="Usuario" styleClass="login-field" />
<PasswordField fx:id="txtContrasena" promptText="Contrasena" styleClass="login-field" />
<Button fx:id="btnIniciarSesion" mnemonicParsing="false"
        onAction="#handleIniciarSesion" prefWidth="200.0"
        styleClass="btn-primary" text="Iniciar Sesion" />
<Hyperlink fx:id="linkRegistrarse" onAction="#handleRegistrarse"
           text="Crear una cuenta nueva" />
```

### 5.2 LoginController.handleRegistrarse()

### ¿Que hace?
Navega a la pantalla de registro cargando `registro.fxml` en la misma ventana (no abre un Stage nuevo).

### Codigo Java relevante

```java
@FXML
private void handleRegistrarse() {
    try {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/registro.fxml"));
        Stage stage = (Stage) txtUsuario.getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
            getClass().getResource("/css/styles.css").toExternalForm()
        );
        stage.setScene(scene);
        stage.setTitle("Registro - Sistema de Facturacion");
        stage.show();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

### 5.3 LoginController.abrirDashboard()

### ¿Que hace?
Carga el FXML del rol correspondiente, pasa los datos del usuario al controlador via `inicializar()`, crea un Stage NUEVO maximizado, aplica CSS y lo muestra.

### Codigo Java relevante

```java
private void abrirDashboard(String fxmlPath, String nombreUsuario, int userId) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Object controller = loader.getController();
        if (controller instanceof AdminController) {
            ((AdminController) controller).inicializar(nombreUsuario, userId);
        } else if (controller instanceof VendedorController) {
            ((VendedorController) controller).inicializar(nombreUsuario, userId);
        } else if (controller instanceof DepositoController) {
            ((DepositoController) controller).inicializar(nombreUsuario, userId);
        }

        Stage stage = new Stage();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
            getClass().getResource("/css/styles.css").toExternalForm()
        );
        stage.setTitle("Sistema de Facturacion - " + nombreUsuario);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    } catch (Exception e) {
        Validaciones.mostrarError("Error", "No se pudo abrir el dashboard");
    }
}
```

### Diagrama de flujo del login

```
[login.fxml] → handleIniciarSesion()
                   ↓
           validarNoVacio() → si vacio → [ERROR]
                   ↓ OK
        ConexionDB.getConexion()
                   ↓
        SELECT ... WHERE usuario=? AND contrasena=? AND activo=1
                   ↓
         rs.next() == false? → [ERROR credenciales]
                   ↓ true
        switch(rol)
        ┌────┬─────┬──────────┐
       ADMIN VENDEDOR DEPOSITARIO
        ↓      ↓         ↓
   admin.fxml vendedor.fxml deposito.fxml
        ↓      ↓         ↓
   AdminController VendedorController DepositoController
   .inicializar() .inicializar()      .inicializar()
        ↓      ↓         ↓
   [Stage new maximizado]
```

---

## 6. Registro de Usuarios

### 6.1 RegistroController.initialize()

### ¿Que hace?
Configura los filtros de validacion en tiempo real mientras el usuario escribe. Se ejecuta automaticamente al cargar el FXML.

### Codigo Java relevante

```java
@FXML
private void initialize() {
    Validaciones.configurarSoloLetras(txtNombre);
    Validaciones.configurarSoloLetras(txtApellido);
    Validaciones.configurarSoloNumeros(txtTelefono);
    Validaciones.limitarCaracteres(txtTelefono, 20);
    Validaciones.limitarCaracteres(txtUsuario, 50);
}
```

### 6.2 RegistroController.handleRegistrarse()

### ¿Que hace?
Valida todos los campos del formulario y ejecuta un INSERT en la tabla usuarios con rol VENDEDOR por defecto. Captura duplicados en el campo `usuario` (UNIQUE).

### ¿Donde se ejecuta?
- FXML: `registro.fxml:34` `onAction="#handleRegistrarse"`
- Controlador: `RegistroController.java:48` `handleRegistrarse()`

### Flujo completo paso a paso

```
1. Usuario completa los 7 campos del formulario
2. Click "Registrarse"
3. handleRegistrarse()
   └── Obtener valores: txtNombre, txtApellido, txtUsuario, txtContrasena,
   │    txtConfirmarContrasena, txtTelefono, txtEmail
   └── validarNoVacio(nombre) → si no → mostrarError("nombre obligatorio")
   └── validarNoVacio(apellido) → si no → mostrarError("apellido obligatorio")
   └── validarNoVacio(usuario) → si no → mostrarError("usuario obligatorio")
   └── validarContrasena(contrasena) → si no → mostrarError("min 8 caracteres")
   └── contrasena.equals(confirmar) → si no → mostrarError("no coinciden")
   └── Si email no vacio → validarEmail(email) → si no → mostrarError("email invalido")
   └── Si telefono no vacio → validarTelefono(telefono) → si no → mostrarError("6-20 digitos")
   └── Connection conn = ConexionDB.getConexion()
   └── PreparedStatement stmt = conn.prepareStatement(INSERT)
   └── stmt.setString(1, nombre)
   └── stmt.setString(2, apellido)
   └── stmt.setString(3, usuario)
   └── stmt.setString(4, contrasena)
   └── stmt.setString(5, telefono.isEmpty() ? null : telefono)
   └── stmt.setString(6, email.isEmpty() ? null : email)
   └── stmt.executeUpdate()
   └── Si resultado > 0 → mostrarInfo() + handleCancelar() (vuelve a login)
   └── catch Exception
   │     ├── mensaje.contains("Duplicate") → mostrarError("Usuario existente")
   │     └── else → mostrarError("Error de base de datos")
```

### Codigo SQL exacto

```sql
INSERT INTO usuarios (nombre, apellido, usuario, contrasena, telefono, email, rol)
VALUES (?, ?, ?, ?, ?, ?, 'VENDEDOR')
```

### Codigo Java relevante

```java
@FXML
private void handleRegistrarse() {
    String nombre = txtNombre.getText().trim();
    String apellido = txtApellido.getText().trim();
    String usuario = txtUsuario.getText().trim();
    String contrasena = txtContrasena.getText();
    String confirmar = txtConfirmarContrasena.getText();
    String telefono = txtTelefono.getText().trim();
    String email = txtEmail.getText().trim();

    if (!Validaciones.validarNoVacio(nombre)) {
        Validaciones.mostrarError("Campo requerido", "El nombre es obligatorio");
        return;
    }
    if (!Validaciones.validarNoVacio(apellido)) {
        Validaciones.mostrarError("Campo requerido", "El apellido es obligatorio");
        return;
    }
    if (!Validaciones.validarNoVacio(usuario)) {
        Validaciones.mostrarError("Campo requerido", "El usuario es obligatorio");
        return;
    }
    if (!Validaciones.validarContrasena(contrasena)) {
        Validaciones.mostrarError("Contrasena debil", "La contrasena debe tener al menos 8 caracteres");
        return;
    }
    if (!contrasena.equals(confirmar)) {
        Validaciones.mostrarError("Contrasenas no coinciden", "Las contrasenas ingresadas no son iguales");
        return;
    }
    if (Validaciones.validarNoVacio(email) && !Validaciones.validarEmail(email)) {
        Validaciones.mostrarError("Email invalido", "Por favor ingrese un email valido");
        return;
    }
    if (Validaciones.validarNoVacio(telefono) && !Validaciones.validarTelefono(telefono)) {
        Validaciones.mostrarError("Telefono invalido", "El telefono debe contener solo numeros (6-20 digitos)");
        return;
    }

    try {
        Connection conn = ConexionDB.getConexion();
        String sql = "INSERT INTO usuarios (nombre, apellido, usuario, contrasena, telefono, email, rol) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'VENDEDOR')";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, nombre);
        stmt.setString(2, apellido);
        stmt.setString(3, usuario);
        stmt.setString(4, contrasena);
        stmt.setString(5, telefono.isEmpty() ? null : telefono);
        stmt.setString(6, email.isEmpty() ? null : email);

        int resultado = stmt.executeUpdate();

        if (resultado > 0) {
            Validaciones.mostrarInfo("Registro exitoso",
                    "Usuario creado correctamente. Ahora puede iniciar sesion.");
            handleCancelar();
        }
        stmt.close();
    } catch (Exception e) {
        if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
            Validaciones.mostrarError("Usuario existente",
                    "El nombre de usuario ya esta registrado");
        } else {
            Validaciones.mostrarError("Error de base de datos",
                    "No se pudo registrar el usuario: " + e.getMessage());
        }
    }
}
```

### 6.3 RegistroController.handleCancelar()

### ¿Que hace?
Vuelve a la pantalla de login sin registrar.

```java
@FXML
private void handleCancelar() {
    try {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Sistema de Facturacion - Login");
        stage.show();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

### FXML relacionado

```xml
<TextField fx:id="txtNombre" promptText="Nombre" />
<TextField fx:id="txtApellido" promptText="Apellido" />
<TextField fx:id="txtUsuario" promptText="Usuario" />
<PasswordField fx:id="txtContrasena" promptText="Contrasena (min. 8 caracteres)" />
<PasswordField fx:id="txtConfirmarContrasena" promptText="Confirmar contrasena" />
<TextField fx:id="txtTelefono" promptText="Telefono" />
<TextField fx:id="txtEmail" promptText="Email" />
<Button fx:id="btnRegistrarse" onAction="#handleRegistrarse" text="Registrarse" />
<Button fx:id="btnCancelar" onAction="#handleCancelar" text="Cancelar" />
```

### Diagrama de flujo

```
[registro.fxml] → initialize() (configura filtros en vivo)
                      ↓
          handleRegistrarse() → 7 validaciones consecutivas
                      ↓
             todas OK? → NO → [ERROR] → return
                      ↓ SI
         Connection conn = ConexionDB.getConexion()
                      ↓
      PreparedStatement → INSERT INTO usuarios VALUES (...)
                      ↓
       executeUpdate() > 0? → YES → [INFO] → handleCancelar() → login
                      ↓ NO
       catch Duplicate → [ERROR "usuario existente"]
       catch otro      → [ERROR "base de datos"]
```

---

## 7. Panel Admin

### 7.0 AdminController.inicializar()

### ¿Que hace?
Recibe el nombre del usuario y su ID, los almacena, muestra el panel de bienvenida, inicia el reloj, y carga todos los datos iniciales de todas las secciones.

### Codigo Java relevante

```java
public void inicializar(String nombreUsuario, int usuarioId) {
    this.nombreUsuario = nombreUsuario;
    this.usuarioId = usuarioId;
    lblUsuario.setText("Usuario: " + nombreUsuario);

    mostrarPanel(panelBienvenida);

    Timeline reloj = new Timeline(new KeyFrame(Duration.seconds(1), e -> actualizarReloj()));
    reloj.setCycleCount(Timeline.INDEFINITE);
    reloj.play();

    cargarUsuarios();
    cargarClientes();
    cargarProveedores();
    cargarProductos();
    cargarFacturas();
    cargarMovimientos();
    cargarEstadisticas();
    configurarDescuentos();
    configurarIVA();
}
```

### 7.0.1 Mostrar panel / navegacion

```java
private void mostrarPanel(VBox panelMostrar) {
    VBox[] paneles = {panelBienvenida, panelUsuarios, panelClientes, panelProveedores,
                      panelProductos, panelFacturacion, panelHistorial,
                      panelEstadisticas, panelMovimientos};
    for (VBox p : paneles) {
        p.setVisible(p == panelMostrar);
        p.setManaged(p == panelMostrar);
    }
}
```

### 7.0.2 Reloj en tiempo real

```java
private void actualizarReloj() {
    lblFechaActual.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    lblHoraActual.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
}
```

### 7.0.3 Metodos auxiliares de tabla

```java
private void configurarColumna(TableColumn<ObservableList<String>, String> col, int index) {
    col.setCellValueFactory(data -> {
        ObservableList<String> row = data.getValue();
        if (index < row.size() && row.get(index) != null) {
            return new SimpleStringProperty(row.get(index));
        }
        return new SimpleStringProperty("");
    });
}

private ObservableList<String> filaDesdeRS(ResultSet rs, String... columnas) throws SQLException {
    ObservableList<String> fila = FXCollections.observableArrayList();
    for (String col : columnas) {
        String val = rs.getString(col);
        fila.add(val != null ? val : "");
    }
    return fila;
}
```

### 7.0.4 Cerrar Sesion (todos los roles)

```java
@FXML private void handleCerrarSesion() {
    if (Validaciones.mostrarConfirmacion("Cerrar Sesion", "Desea cerrar la sesion?")) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) lblUsuario.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Sistema de Facturacion - Login");
        } catch (Exception e) { e.printStackTrace(); }
    }
}
```

### 7.1 ADMIN - CRUD Usuarios

#### 7.1.1 cargarUsuarios()

### ¿Que hace?
Ejecuta SELECT a `usuarios` y llena la tabla con 8 columnas (id, nombre, apellido, usuario, telefono, email, rol, activo).

### Codigo SQL exacto

```sql
SELECT id, nombre, apellido, usuario, telefono, email, rol, activo FROM usuarios
```

### Codigo Java relevante

```java
private void cargarUsuarios() {
    try {
        Connection conn = ConexionDB.getConexion();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT id, nombre, apellido, usuario, telefono, email, rol, activo FROM usuarios");

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (int i = 0; i < 8; i++) configurarColumna(getColUsuario(i), i);

        while (rs.next()) {
            String activoStr = rs.getInt("activo") == 1 ? "Activo" : "Inactivo";
            data.add(FXCollections.observableArrayList(
                String.valueOf(rs.getInt("id")),
                rs.getString("nombre"), rs.getString("apellido"),
                rs.getString("usuario"),
                rs.getString("telefono") != null ? rs.getString("telefono") : "",
                rs.getString("email") != null ? rs.getString("email") : "",
                rs.getString("rol"), activoStr));
        }
        tablaUsuarios.setItems(data);
        rs.close(); stmt.close();
    } catch (Exception e) { e.printStackTrace(); }
}
```

#### 7.1.2 handleNuevoUsuario()

### ¿Que hace?
Abre un `Dialog<ObservableList<String>>` con campos para nombre, apellido, usuario, contrasena, telefono, email y un ComboBox de rol (ADMIN/VENDEDOR/DEPOSITARIO). Valida campos obligatorios y ejecuta INSERT.

### Codigo SQL exacto

```sql
INSERT INTO usuarios (nombre, apellido, usuario, contrasena, telefono, email, rol)
VALUES (?,?,?,?,?,?,?)
```

### Codigo Java relevante

```java
@FXML private void handleNuevoUsuario() {
    Dialog<ObservableList<String>> dialog = new Dialog<>();
    dialog.setTitle("Nuevo Usuario");
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
    TextField txtNombre = new TextField(); TextField txtApellido = new TextField();
    TextField txtUsuario = new TextField(); PasswordField txtContrasena = new PasswordField();
    TextField txtTelefono = new TextField(); TextField txtEmail = new TextField();
    ComboBox<String> cmbRol = new ComboBox<>(FXCollections.observableArrayList("ADMIN", "VENDEDOR", "DEPOSITARIO"));
    cmbRol.setValue("VENDEDOR");

    Validaciones.configurarSoloLetras(txtNombre);
    Validaciones.configurarSoloLetras(txtApellido);
    Validaciones.configurarSoloNumeros(txtTelefono);
    Validaciones.limitarCaracteres(txtUsuario, 50);
    Validaciones.limitarCaracteres(txtEmail, 100);

    grid.addRow(0, new Label("Nombre:"), txtNombre);
    grid.addRow(1, new Label("Apellido:"), txtApellido);
    grid.addRow(2, new Label("Usuario:"), txtUsuario);
    grid.addRow(3, new Label("Contrasena:"), txtContrasena);
    grid.addRow(4, new Label("Telefono:"), txtTelefono);
    grid.addRow(5, new Label("Email:"), txtEmail);
    grid.addRow(6, new Label("Rol:"), cmbRol);
    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(btn -> {
        if (btn == ButtonType.OK) {
            return FXCollections.observableArrayList(
                txtNombre.getText(), txtApellido.getText(), txtUsuario.getText(),
                txtContrasena.getText(), txtTelefono.getText(), txtEmail.getText(),
                cmbRol.getValue());
        }
        return null;
    });

    dialog.showAndWait().ifPresent(d -> {
        if (!Validaciones.validarNoVacio(d.get(0)) || !Validaciones.validarNoVacio(d.get(1))
            || !Validaciones.validarNoVacio(d.get(2)) || !Validaciones.validarNoVacio(d.get(3))) {
            Validaciones.mostrarError("Campos requeridos", "Nombre, apellido, usuario y contrasena son obligatorios");
            return;
        }
        try {
            Connection conn = ConexionDB.getConexion();
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO usuarios (nombre, apellido, usuario, contrasena, telefono, email, rol) VALUES (?,?,?,?,?,?,?)");
            stmt.setString(1, d.get(0)); stmt.setString(2, d.get(1));
            stmt.setString(3, d.get(2)); stmt.setString(4, d.get(3));
            stmt.setString(5, d.get(4).isEmpty() ? null : d.get(4));
            stmt.setString(6, d.get(5).isEmpty() ? null : d.get(5));
            stmt.setString(7, d.get(6));
            stmt.executeUpdate(); stmt.close();
            Validaciones.mostrarInfo("Exito", "Usuario creado correctamente");
            cargarUsuarios();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                Validaciones.mostrarError("Duplicado", "Ya existe un registro con ese valor");
            } else {
                Validaciones.mostrarError("Error", e.getMessage());
            }
        }
    });
}
```

#### 7.1.3 handleEditarUsuario()

### ¿Que hace?
Obtiene la fila seleccionada, abre dialog pre-poblado. Si se ingresa contrasena nueva, hace UPDATE con contrasena; si no, UPDATE sin contrasena.

### Codigo SQL exacto

```sql
-- Con contrasena:
UPDATE usuarios SET nombre=?, apellido=?, usuario=?, contrasena=?, telefono=?, email=?, rol=? WHERE id=?

-- Sin contrasena:
UPDATE usuarios SET nombre=?, apellido=?, usuario=?, telefono=?, email=?, rol=? WHERE id=?
```

### Codigo Java relevante (logica de decision)

```java
if (Validaciones.validarNoVacio(d.get(3))) {
    sql = "UPDATE usuarios SET nombre=?, apellido=?, usuario=?, contrasena=?, telefono=?, email=?, rol=? WHERE id=?";
    stmt = conn.prepareStatement(sql);
    stmt.setString(1, d.get(0)); stmt.setString(2, d.get(1));
    stmt.setString(3, d.get(2)); stmt.setString(4, d.get(3));
    stmt.setString(5, d.get(4).isEmpty() ? null : d.get(4));
    stmt.setString(6, d.get(5).isEmpty() ? null : d.get(5));
    stmt.setString(7, d.get(6));
    stmt.setInt(8, Integer.parseInt(fila.get(0)));
} else {
    sql = "UPDATE usuarios SET nombre=?, apellido=?, usuario=?, telefono=?, email=?, rol=? WHERE id=?";
    stmt = conn.prepareStatement(sql);
    stmt.setString(1, d.get(0)); stmt.setString(2, d.get(1));
    stmt.setString(3, d.get(2));
    stmt.setString(4, d.get(4).isEmpty() ? null : d.get(4));
    stmt.setString(5, d.get(5).isEmpty() ? null : d.get(5));
    stmt.setString(6, d.get(6));
    stmt.setInt(7, Integer.parseInt(fila.get(0)));
}
```

#### 7.1.4 handleEliminarUsuario()

### ¿Que hace?
Pide confirmacion y establece `activo=0` (borrado logico, no fisico).

### Codigo SQL exacto

```sql
UPDATE usuarios SET activo=0 WHERE id=?
```

### FXML relacionado (panel usuarios)

```xml
<VBox fx:id="panelUsuarios">
  <HBox>
    <Text text="Usuarios" />
    <TextField fx:id="txtBuscarUsuario" onKeyReleased="#filtrarUsuarios" />
    <Button onAction="#handleNuevoUsuario" text="Nuevo" />
  </HBox>
  <TableView fx:id="tablaUsuarios">
    <columns>
      <TableColumn fx:id="colUsuarioId" text="ID" />
      <TableColumn fx:id="colUsuarioNombre" text="Nombre" />
      <TableColumn fx:id="colUsuarioApellido" text="Apellido" />
      <TableColumn fx:id="colUsuarioUsuario" text="Usuario" />
      <TableColumn fx:id="colUsuarioTelefono" text="Telefono" />
      <TableColumn fx:id="colUsuarioEmail" text="Email" />
      <TableColumn fx:id="colUsuarioRol" text="Rol" />
      <TableColumn fx:id="colUsuarioActivo" text="Estado" />
    </columns>
  </TableView>
  <HBox>
    <Button onAction="#handleEditarUsuario" text="Editar" />
    <Button onAction="#handleEliminarUsuario" text="Eliminar" />
  </HBox>
</VBox>
```

---

### 7.2 ADMIN - CRUD Clientes

#### 7.2.1 cargarClientes()

### Codigo SQL exacto

```sql
SELECT id, nombre, apellido, dni, telefono, email, direccion
FROM clientes WHERE activo=1
```

#### 7.2.2 mostrarDialogoCliente()

### ¿Que hace?
Dialogo COMPARTIDO para nuevo y editar. Si `valores == null` → INSERT; si `valores != null` → UPDATE con el ID de la fila.

### Codigo SQL exacto

```sql
-- INSERT:
INSERT INTO clientes (nombre, apellido, dni, telefono, email, direccion) VALUES (?,?,?,?,?,?)

-- UPDATE:
UPDATE clientes SET nombre=?, apellido=?, dni=?, telefono=?, email=?, direccion=? WHERE id=?
```

### Codigo Java relevante

```java
private void mostrarDialogoCliente(String titulo, ObservableList<String> valores) {
    Dialog<ObservableList<String>> dialog = new Dialog<>();
    dialog.setTitle(titulo);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    TextField txtNombre = new TextField(); TextField txtApellido = new TextField();
    TextField txtDni = new TextField(); TextField txtTelefono = new TextField();
    TextField txtEmail = new TextField(); TextField txtDireccion = new TextField();

    Validaciones.configurarSoloLetras(txtNombre);
    Validaciones.configurarSoloLetras(txtApellido);
    Validaciones.configurarSoloNumeros(txtDni);
    Validaciones.configurarSoloNumeros(txtTelefono);
    Validaciones.limitarCaracteres(txtEmail, 100);
    Validaciones.limitarCaracteres(txtDireccion, 255);

    if (valores != null && valores.size() >= 6) {
        txtNombre.setText(valores.get(1));
        txtApellido.setText(valores.get(2));
        txtDni.setText(valores.get(3));
        txtTelefono.setText(valores.get(4));
        txtEmail.setText(valores.get(5));
        txtDireccion.setText(valores.size() > 6 ? valores.get(6) : "");
    }

    grid.addRow(0, new Label("Nombre:"), txtNombre);
    grid.addRow(1, new Label("Apellido:"), txtApellido);
    grid.addRow(2, new Label("DNI:"), txtDni);
    grid.addRow(3, new Label("Telefono:"), txtTelefono);
    grid.addRow(4, new Label("Email:"), txtEmail);
    grid.addRow(5, new Label("Direccion:"), txtDireccion);
    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(btn -> {
        if (btn == ButtonType.OK) {
            return FXCollections.observableArrayList("", txtNombre.getText(), txtApellido.getText(),
                txtDni.getText(), txtTelefono.getText(), txtEmail.getText(), txtDireccion.getText());
        }
        return null;
    });

    dialog.showAndWait().ifPresent(datos -> {
        if (!Validaciones.validarNoVacio(datos.get(1)) || !Validaciones.validarNoVacio(datos.get(2))) {
            Validaciones.mostrarError("Campos requeridos", "Nombre y apellido son obligatorios");
            return;
        }
        if (Validaciones.validarNoVacio(datos.get(4)) && !Validaciones.validarTelefono(datos.get(4))) {
            Validaciones.mostrarError("Telefono invalido", "Debe contener solo numeros (6-20 digitos)"); return;
        }
        if (Validaciones.validarNoVacio(datos.get(5)) && !Validaciones.validarEmail(datos.get(5))) {
            Validaciones.mostrarError("Email invalido", "Ingrese un email valido"); return;
        }
        try {
            Connection conn = ConexionDB.getConexion();
            if (valores == null) {
                // INSERT
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO clientes (nombre, apellido, dni, telefono, email, direccion) VALUES (?,?,?,?,?,?)");
                stmt.setString(1, datos.get(1)); stmt.setString(2, datos.get(2));
                stmt.setString(3, datos.get(3)); stmt.setString(4, datos.get(4));
                stmt.setString(5, datos.get(5)); stmt.setString(6, datos.get(6));
                stmt.executeUpdate(); stmt.close();
            } else {
                // UPDATE
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE clientes SET nombre=?, apellido=?, dni=?, telefono=?, email=?, direccion=? WHERE id=?");
                stmt.setString(1, datos.get(1)); stmt.setString(2, datos.get(2));
                stmt.setString(3, datos.get(3)); stmt.setString(4, datos.get(4));
                stmt.setString(5, datos.get(5)); stmt.setString(6, datos.get(6));
                stmt.setInt(7, Integer.parseInt(valores.get(0))); stmt.executeUpdate(); stmt.close();
            }
            Validaciones.mostrarInfo("Exito", "Cliente guardado correctamente");
            cargarClientes();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                Validaciones.mostrarError("Duplicado", "Ya existe un registro con ese valor");
            } else {
                Validaciones.mostrarError("Error", e.getMessage());
            }
        }
    });
}
```

#### 7.2.3 handleEliminarCliente()

### Codigo SQL exacto

```sql
UPDATE clientes SET activo=0 WHERE id=?
```

---

### 7.3 ADMIN - CRUD Proveedores

#### 7.3.1 cargarProveedores()

### Codigo SQL exacto

```sql
SELECT id, nombre_empresa, nombre_contacto, telefono, email, direccion FROM proveedores WHERE activo=1
```

#### 7.3.2 mostrarDialogoProveedor()

Mismo patron que clientes. Campos: empresa, contacto, telefono, email, direccion.

### Codigo SQL exacto

```sql
-- INSERT:
INSERT INTO proveedores (nombre_empresa, nombre_contacto, telefono, email, direccion) VALUES (?,?,?,?,?)

-- UPDATE:
UPDATE proveedores SET nombre_empresa=?, nombre_contacto=?, telefono=?, email=?, direccion=? WHERE id=?
```

#### 7.3.3 handleEliminarProveedor()

### Codigo SQL exacto

```sql
UPDATE proveedores SET activo=0 WHERE id=?
```

---

### 7.4 ADMIN - CRUD Productos

#### 7.4.1 cargarProductos()

### ¿Que hace?
SELECT con LEFT JOIN a proveedores para mostrar 10 columnas. No hay filtro WHERE activo=1, se muestra el estado.

### Codigo SQL exacto

```sql
SELECT p.id, p.codigo, p.nombre, p.categoria, p.precio_compra, p.precio_venta,
       p.descuento, p.stock_actual, p.stock_minimo,
       COALESCE(pr.nombre_empresa, 'Sin proveedor') as proveedor, p.activo
FROM productos p
LEFT JOIN proveedores pr ON p.proveedor_id = pr.id
```

#### 7.4.2 handleNuevoProducto()

### ¿Que hace?
Dialog con 11 campos. Carga ComboBox de proveedores desde BD. Valida con `configurarSoloDecimales` para precios y `configurarSoloEnteros` para stock.

### Codigo SQL exacto

```sql
INSERT INTO productos (codigo, nombre, descripcion, categoria, precio_compra, precio_venta,
       descuento, stock_actual, stock_minimo, proveedor_id, activo)
VALUES (?,?,?,?,?,?,?,?,?,?,?)
```

#### 7.4.3 handleEditarProducto()

### ¿Que hace?
Obtiene datos completos del producto con un SELECT por codigo, pre-puebla el dialog, ejecuta UPDATE.

### Codigo SQL exacto

```sql
-- SELECT para obtener datos:
SELECT p.*, COALESCE(pr.nombre_empresa, 'Sin proveedor') as nom_prov, pr.id as prov_id
FROM productos p LEFT JOIN proveedores pr ON p.proveedor_id = pr.id
WHERE p.codigo = ?

-- UPDATE:
UPDATE productos SET codigo=?, nombre=?, descripcion=?, categoria=?, precio_compra=?,
       precio_venta=?, descuento=?, stock_actual=?, stock_minimo=?, proveedor_id=?, activo=?
WHERE id=?
```

**Nota:** No hay metodo handleEliminarProducto.

---

### 7.5 ADMIN - Facturacion

#### 7.5.1 mostrarFacturacion()

### ¿Que hace?
Muestra el panel, carga clientes y productos en ComboBox, limpia detalle, resetea totales y configura validacion de enteros en cantidad.

```java
@FXML private void mostrarFacturacion() {
    mostrarPanel(panelFacturacion);
    cargarClientesFactura();
    cargarProductosFactura();
    detalleFactura.clear();
    actualizarTotales();
    configurarCantidadField();
}
```

#### 7.5.2 cargarClientesFactura()

### Codigo SQL exacto

```sql
SELECT id, nombre, apellido, dni FROM clientes WHERE activo=1
```

Almacena en `clientesFactura` (ObservableList<String[]>) donde [0]=id, [1]=info mostrada.

#### 7.5.3 cargarProductosFactura()

### Codigo SQL exacto

```sql
SELECT id, codigo, nombre, precio_venta, descuento, stock_actual
FROM productos WHERE activo=1 AND stock_actual > 0
```

Almacena en `productosFactura` (ObservableList<String[]>) con indices: [0]=id, [1]=codigo, [2]=nombre, [3]=precio, [4]=stock, [5]=descuento%, [6]=info mostrada.

#### 7.5.4 buscarProductoFactura()

### ¿Que hace?
Filtro local sobre `productosFactura` cada vez que se suelta una tecla.

```java
@FXML private void buscarProductoFactura() {
    String filtro = txtBuscarProductoFactura.getText().toLowerCase();
    cmbProductoFactura.getItems().clear();
    for (String[] p : productosFactura) {
        if (p[6].toLowerCase().contains(filtro)) {
            cmbProductoFactura.getItems().add(p[6]);
        }
    }
}
```

#### 7.5.5 configurarDescuentos()

```java
private void configurarDescuentos() {
    cmbDescuentoGlobal.setItems(FXCollections.observableArrayList("0%", "5%", "10%", "15%", "20%", "Personalizado"));
    cmbDescuentoGlobal.setValue("0%");
    cmbDescuentoGlobal.setOnAction(e -> actualizarTotales());
}
```

#### 7.5.6 configurarIVA()

```java
private void configurarIVA() {
    cmbIVA.setItems(FXCollections.observableArrayList("0%", "5%", "10%", "15%", "20%"));
    cmbIVA.setValue("10%");
    cmbIVA.setOnAction(e -> actualizarTotales());
}
```

#### 7.5.7 obtenerPorcentajeIVA()

```java
private double obtenerPorcentajeIVA() {
    String val = cmbIVA.getValue();
    if (val == null) return 0.10;
    try { return Double.parseDouble(val.replace("%", "")) / 100.0; }
    catch (Exception e) { return 0.10; }
}
```

#### 7.5.8 agregarProductoFactura()

### ¿Que hace?
Valida seleccion, stock, duplicados, calcula subtotal = cantidad * precio * (1 - descuento/100) y agrega al detalle.

### Flujo completo paso a paso

```
1. Obtener indice seleccionado del combo producto
2. Si no hay seleccion → ERROR
3. Obtener String[] producto de productosFactura
4. Parsear cantidad (default 1)
5. Validar cantidad < 1 → ERROR
6. Validar cantidad > stock → ERROR
7. Validar producto duplicado en detalle → ERROR
8. Calcular: subtotal = cantidad * precio * (1 - descuento/100)
9. Agregar String[]{prodId, codigo, nombre, cantidadStr, precioStr, descuentoStr, subtotalStr}
10. actualizarTablaDetalle()
11. actualizarTotales()
```

### Codigo Java relevante

```java
@FXML private void agregarProductoFactura() {
    int idxProd = cmbProductoFactura.getSelectionModel().getSelectedIndex();
    if (idxProd < 0) { Validaciones.mostrarError("Seleccionar", "Seleccione un producto"); return; }

    String[] producto = productosFactura.get(idxProd);
    int cantidad = 1;
    try { cantidad = Integer.parseInt(txtCantidadFactura.getText()); } catch (Exception e) {}

    int stockDisponible = Integer.parseInt(producto[4]);
    if (cantidad > stockDisponible) {
        Validaciones.mostrarError("Stock insuficiente", "Stock disponible: " + stockDisponible);
        return;
    }

    for (String[] det : detalleFactura) {
        if (det[1].equals(producto[1])) {
            Validaciones.mostrarError("Producto duplicado",
                "Ya agregaste este producto. Eliminalo y agregalo de nuevo con otra cantidad.");
            return;
        }
    }

    double precio = Double.parseDouble(producto[3]);
    double descuento = Double.parseDouble(producto[5]);
    double subtotal = cantidad * precio * (1 - descuento / 100.0);

    detalleFactura.add(new String[]{
        producto[0], producto[1], producto[2],
        String.valueOf(cantidad), String.format("%.2f", precio),
        String.format("%.2f", descuento), String.format("%.2f", subtotal)
    });

    actualizarTablaDetalle();
    actualizarTotales();
}
```

#### 7.5.9 actualizarTotales()

### ¿Que hace?
Recorre detalle sumando subtotales, aplica descuento global (incluyendo "Personalizado" que abre TextInputDialog), calcula IVA y total.

### Flujo de calculo

```
subtotal = sum(detalle[6])  ← subtotal de cada linea
descuentoGlobal% = leer cmbDescuentoGlobal
  └── "Personalizado" → TextInputDialog → ingresar %
  └── otro → parsear reemplazando "%"
descuentoMonto = subtotal * (descuentoGlobal / 100)
baseImponible = subtotal - descuentoMonto
iva = baseImponible * obtenerPorcentajeIVA()
total = baseImponible + iva
```

#### 7.5.10 eliminarProductoFactura()

```java
@FXML private void eliminarProductoFactura() {
    int idx = tablaDetalleFactura.getSelectionModel().getSelectedIndex();
    if (idx < 0) { Validaciones.mostrarError("Seleccionar", "Seleccione un producto del detalle"); return; }
    String codigo = tablaDetalleFactura.getItems().get(idx).get(0);
    detalleFactura.removeIf(d -> d[1].equals(codigo));
    actualizarTablaDetalle();
    actualizarTotales();
}
```

#### 7.5.11 handleEmitirFactura()

### ¿Que hace?
TRANSACCION completa: INSERT factura → obtener ID generado → INSERT detalle (batch) → UPDATE stock (batch) → INSERT auditoria → commit. Si falla algo → rollback.

### Codigo SQL exacto ejecutado en la transaccion

```sql
-- 1. INSERT factura:
INSERT INTO facturas (numero_factura, fecha, hora, usuario_id, cliente_id,
       subtotal, descuento_global, iva, total)
VALUES (?,?,?,?,?,?,?,?,?)

-- 2. INSERT detalle (por cada producto, batch):
INSERT INTO detalle_factura (factura_id, producto_id, cantidad, precio_unitario,
       descuento_porcentaje, descuento_monto, subtotal_linea)
VALUES (?,?,?,?,?,?,?)

-- 3. UPDATE stock (por cada producto, batch):
UPDATE productos SET stock_actual = stock_actual - ? WHERE id = ?

-- 4. INSERT auditoria:
INSERT INTO auditoria (usuario_id, accion, tabla_afectada, registro_id, detalle)
VALUES (?,?,?,?,?)
```

### Flujo completo paso a paso

```
1. Validar detalle no vacio
2. Obtener clienteId del combo (0 si no selecciono)
3. conn.setAutoCommit(false)
4. TRY:
   ├── generarNumeroFactura()
   ├── Calcular subtotal, descuentoGlobal%, iva, total
   ├── PreparedStatement INSERT INTO facturas
   │   └── stmtFactura.executeUpdate()
   │   └── stmtFactura.getGeneratedKeys() → facturaId
   ├── PreparedStatement INSERT INTO detalle_factura (batch)
   │   └── stmtDetalle.setInt(1, facturaId) ... setDouble(7, sub)
   │   └── stmtDetalle.addBatch() por cada producto
   ├── PreparedStatement UPDATE productos SET stock_actual = stock_actual - ? (batch)
   │   └── stmtStock.setInt(1, cantidad); setInt(2, prodId); addBatch()
   ├── stmtDetalle.executeBatch()
   ├── stmtStock.executeBatch()
   ├── PreparedStatement INSERT INTO auditoria
   │   └── stmtAudit.executeUpdate()
   ├── conn.commit()
   ├── mostrarInfo("Factura emitida")
   ├── Limpiar detalle, refrescar tabla, recargar productos
   └── CATCH → conn.rollback() → throw e
   FINALLY → conn.setAutoCommit(true)
```

#### 7.5.12 generarNumeroFactura()

### Codigo SQL exacto

```sql
SELECT COUNT(*) + 1 FROM facturas
```

Formato: `FAC-YYYYMM-NNNN` (ej: `FAC-202606-0001`)

```java
private String generarNumeroFactura(Connection conn) throws SQLException {
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) + 1 FROM facturas");
    rs.next();
    int count = rs.getInt(1);
    rs.close(); stmt.close();
    String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    return "FAC-" + fecha + "-" + String.format("%04d", count);
}
```

### FXML facturacion (admin)

```xml
<VBox fx:id="panelFacturacion">
  <HBox>
    <Text text="Facturacion" />
    <Label text="Fecha:" /> <Label fx:id="lblFechaActual" />
    <Label text="Hora:" />  <Label fx:id="lblHoraActual" />
  </HBox>
  <HBox>
    <Label text="Cliente:" />
    <ComboBox fx:id="cmbClienteFactura" prefWidth="400.0" visibleRowCount="6" />
  </HBox>
  <HBox>
    <TextField fx:id="txtBuscarProductoFactura" onKeyReleased="#buscarProductoFactura" />
    <ComboBox fx:id="cmbProductoFactura" prefWidth="300.0" visibleRowCount="8" />
    <TextField fx:id="txtCantidadFactura" prefWidth="60.0" text="1" />
    <Button onAction="#agregarProductoFactura" text="Agregar" />
  </HBox>
  <TableView fx:id="tablaDetalleFactura">
    <columns>
      <TableColumn fx:id="colDetCodigo" text="Codigo" />
      <TableColumn fx:id="colDetNombre" text="Producto" />
      <TableColumn fx:id="colDetCantidad" text="Cant" />
      <TableColumn fx:id="colDetPrecio" text="Precio" />
      <TableColumn fx:id="colDetDescuento" text="Dto %" />
      <TableColumn fx:id="colDetSubtotal" text="Subtotal" />
    </columns>
  </TableView>
  <HBox>
    <Label text="Subtotal:" /> <Label fx:id="lblSubtotal" />
    <Label text="Descuento Global:" /> <ComboBox fx:id="cmbDescuentoGlobal" />
    <Label text="IVA:" /> <ComboBox fx:id="cmbIVA" />
    <Label fx:id="lblIVA" />
    <Label text="TOTAL:" /> <Label fx:id="lblTotal" styleClass="label-total-grande" />
  </HBox>
  <HBox>
    <Button onAction="#eliminarProductoFactura" text="Eliminar Producto" />
    <Button onAction="#handleEmitirFactura" text="Emitir Factura" />
  </HBox>
</VBox>
```

### Diagrama de flujo de facturacion

```
[Facturacion Panel]
  ├── cargarClientesFactura()  → SELECT clientes WHERE activo=1
  ├── cargarProductosFactura() → SELECT productos WHERE activo=1 AND stock>0
  │
  ├── [buscarProductoFactura()] → filtro local sobre productosFactura[]
  │
  ├── [agregarProductoFactura()]
  │     ├── validar seleccion, stock, duplicado
  │     └── agregar a detalleFactura[], recalcular totales
  │
  ├── [actualizarTotales()]
  │     ├── sum(subtotales linea)
  │     ├── aplicar descuento global (% fijo o personalizado)
  │     ├── calcular IVA
  │     └── mostrar total
  │
  ├── [eliminarProductoFactura()] → remover del detalle, recalcular
  │
  └── [handleEmitirFactura()]
        ├── conn.setAutoCommit(false)
        ├── INSERT facturas ← generarNumeroFactura()
        ├── INSERT detalle_factura (batch)
        ├── UPDATE productos SET stock_actual = stock_actual - ? (batch)
        ├── INSERT auditoria
        ├── conn.commit() o conn.rollback() si error
        └── conn.setAutoCommit(true)
```

---

### 7.6 ADMIN - Historial Facturas

#### 7.6.1 cargarFacturas()

### Codigo SQL exacto

```sql
SELECT f.numero_factura, f.fecha, f.hora,
       CONCAT(u.nombre, ' ', u.apellido) as vendedor,
       COALESCE(CONCAT(c.nombre, ' ', c.apellido), 'Consumidor Final') as cliente,
       f.total, f.estado
FROM facturas f
JOIN usuarios u ON f.usuario_id = u.id
LEFT JOIN clientes c ON f.cliente_id = c.id
ORDER BY f.id DESC
```

#### 7.6.2 filtrarFacturas()

Recarga la tabla completa (filtro local no implementado realmente, solo recarga).

```java
@FXML private void filtrarFacturas() { cargarFacturas(); }
```

#### 7.6.3 handleExportarPDF()

### ¿Que hace?
Stub/funcionalidad placeholder. Solo muestra un mensaje informativo.

```java
@FXML private void handleExportarPDF() {
    Validaciones.mostrarInfo("Exportar PDF", "Funcionalidad de exportacion a PDF disponible.");
}
```

---

### 7.7 ADMIN - Estadisticas

#### 7.7.1 cargarEstadisticas()

### ¿Que hace?
Ejecuta 4 consultas independientes y muestra los resultados en 4 tarjetas de estadisticas.

### Codigo SQL exacto

```sql
-- Total de facturas emitidas:
SELECT COUNT(*) as total FROM facturas

-- Suma total de ventas:
SELECT COALESCE(SUM(total), 0) as total FROM facturas

-- Total de clientes activos:
SELECT COUNT(*) as total FROM clientes WHERE activo=1

-- Productos con stock bajo o igual al minimo:
SELECT COUNT(*) as total FROM productos WHERE activo=1 AND stock_actual <= stock_minimo
```

### Codigo Java relevante

```java
private void cargarEstadisticas() {
    try {
        Connection conn = ConexionDB.getConexion();
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM facturas");
        if (rs.next()) lblTotalFacturas.setText(String.valueOf(rs.getInt("total")));
        rs.close();

        rs = stmt.executeQuery("SELECT COALESCE(SUM(total), 0) as total FROM facturas");
        if (rs.next()) lblTotalVentas.setText("$" + String.format("%.2f", rs.getDouble("total")));
        rs.close();

        rs = stmt.executeQuery("SELECT COUNT(*) as total FROM clientes WHERE activo=1");
        if (rs.next()) lblTotalClientes.setText(String.valueOf(rs.getInt("total")));
        rs.close();

        rs = stmt.executeQuery("SELECT COUNT(*) as total FROM productos WHERE activo=1 AND stock_actual <= stock_minimo");
        if (rs.next()) lblTotalBajoStock.setText(String.valueOf(rs.getInt("total")));
        rs.close();

        stmt.close();
    } catch (Exception e) { e.printStackTrace(); }
}
```

### FXML estadisticas

```xml
<VBox fx:id="panelEstadisticas">
  <Text text="Estadisticas" />
  <HBox>
    <VBox styleClass="stat-card">
      <Text text="Total Facturas" styleClass="stat-label" />
      <Text fx:id="lblTotalFacturas" text="0" styleClass="stat-value" />
    </VBox>
    <VBox styleClass="stat-card">
      <Text text="Total Ventas" styleClass="stat-label" />
      <Text fx:id="lblTotalVentas" text="0.00" styleClass="stat-value" />
    </VBox>
    <VBox styleClass="stat-card">
      <Text text="Total Clientes" styleClass="stat-label" />
      <Text fx:id="lblTotalClientes" text="0" styleClass="stat-value" />
    </VBox>
    <VBox styleClass="stat-card">
      <Text text="Stock Bajo" styleClass="stat-label" />
      <Text fx:id="lblTotalBajoStock" text="0" styleClass="stat-value" />
    </VBox>
  </HBox>
</VBox>
```

---

### 7.8 ADMIN - Movimientos Deposito

#### 7.8.1 cargarMovimientos()

### Codigo SQL exacto

```sql
SELECT m.id, m.fecha, m.hora,
       CONCAT(u.nombre, ' ', u.apellido) as usuario,
       m.tipo_movimiento, p.nombre as producto, m.cantidad, m.observacion
FROM movimientos_deposito m
JOIN usuarios u ON m.usuario_id = u.id
JOIN productos p ON m.producto_id = p.id
ORDER BY m.id DESC
```

---

## 8. Panel Vendedor

### 8.0 VendedorController.inicializar()

```java
public void inicializar(String nombreUsuario, int usuarioId) {
    this.nombreUsuario = nombreUsuario;
    this.usuarioId = usuarioId;
    lblUsuario.setText("Usuario: " + nombreUsuario);
    mostrarPanel(panelBienvenida);
    Timeline reloj = new Timeline(new KeyFrame(Duration.seconds(1), e -> actualizarReloj()));
    reloj.setCycleCount(Timeline.INDEFINITE);
    reloj.play();
    configurarDescuentos();
    configurarIVA();
    cargarClientesFactura();
    cargarProductosFactura();
    cargarClientes();
    cargarStock();
    cargarMisFacturas();
}
```

### 8.1 VENDEDOR - Facturacion

#### 8.1.1 mostrarFacturacion()

```java
@FXML private void mostrarFacturacion() {
    mostrarPanel(panelFacturacion);
    cargarClientesFactura();
    cargarProductosFactura();
    detalleFactura.clear();
    actualizarTotales();
    Validaciones.configurarSoloEnteros(txtCantidadFactura);
}
```

El flujo de facturacion del vendedor es IDENTICO al del admin pero con estas diferencias:
- No tiene `configurarCantidadField()` propio (usa `configurarSoloEnteros` directo)
- El metodo `buscarClienteFactura()` no existe en VendedorController (solo en Admin)
- El filtro de facturas por cliente NO existe en vendedor

**Todos los metodos compartidos:** `configurarDescuentos()`, `configurarIVA()`, `obtenerPorcentajeIVA()`, `cargarClientesFactura()`, `cargarProductosFactura()`, `buscarProductoFactura()`, `agregarProductoFactura()`, `actualizarTablaDetalle()`, `actualizarTotales()`, `eliminarProductoFactura()`, `handleEmitirFactura()`, `generarNumeroFactura()`.

### 8.2 VENDEDOR - Clientes (solo lectura)

### Codigo SQL exacto

```sql
SELECT nombre, apellido, dni, telefono, email, direccion FROM clientes WHERE activo=1
```

Tabla con 6 columnas de solo lectura. No hay botones de crear/editar/eliminar.

```java
private void cargarClientes() {
    try {
        Connection conn = ConexionDB.getConexion();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT nombre, apellido, dni, telefono, email, direccion FROM clientes WHERE activo=1");

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (int i = 0; i < 6; i++) configurarColumna(getColCliente(i), i);
        while (rs.next()) {
            data.add(FXCollections.observableArrayList(
                rs.getString("nombre"), rs.getString("apellido"), rs.getString("dni"),
                rs.getString("telefono") != null ? rs.getString("telefono") : "",
                rs.getString("email") != null ? rs.getString("email") : "",
                rs.getString("direccion") != null ? rs.getString("direccion") : ""));
        }
        tablaClientes.setItems(data);
        rs.close(); stmt.close();
    } catch (Exception e) { e.printStackTrace(); }
}
```

### 8.3 VENDEDOR - Stock (solo lectura)

### Codigo SQL exacto

```sql
SELECT codigo, nombre, categoria, precio_venta, stock_actual, stock_minimo
FROM productos WHERE activo=1
```

### 8.4 VENDEDOR - Mis Facturas

### Codigo SQL exacto

```sql
SELECT f.numero_factura, f.fecha, f.hora,
       COALESCE(CONCAT(c.nombre, ' ', c.apellido), 'Consumidor Final') as cliente,
       f.total, f.estado
FROM facturas f
LEFT JOIN clientes c ON f.cliente_id = c.id
WHERE f.usuario_id = ?
ORDER BY f.id DESC
```


### FXML vendedor

```xml
<BorderPane styleClass="dashboard-background">
  <top>
    <HBox styleClass="top-bar">
      <Text text="Panel de Vendedor" styleClass="top-bar-title" />
      <Label fx:id="lblUsuario" styleClass="top-bar-user" />
      <Button onAction="#handleCerrarSesion" text="Cerrar Sesion" />
    </HBox>
  </top>
  <left>
    <VBox styleClass="sidebar">
      <Text text="Menu" styleClass="sidebar-title" />
      <Button onAction="#mostrarFacturacion" text="Facturacion" />
      <Button onAction="#mostrarClientes" text="Clientes" />
      <Button onAction="#mostrarStock" text="Stock" />
      <Button onAction="#mostrarMisFacturas" text="Mis Facturas" />
    </VBox>
  </left>
  <center>
    <ScrollPane>
      <VBox fx:id="contenidoPrincipal">
        <!-- panelBienvenida -->
        <!-- panelFacturacion (misma estructura que admin) -->
        <!-- panelClientes (solo tabla, sin botones CRUD) -->
        <!-- panelStock (solo tabla) -->
        <!-- panelMisFacturas (tabla + Exportar PDF) -->
      </VBox>
    </ScrollPane>
  </center>
</BorderPane>
```

---

## 9. Panel Deposito

### 9.0 DepositoController.inicializar()

```java
public void inicializar(String nombreUsuario, int usuarioId) {
    this.nombreUsuario = nombreUsuario;
    this.usuarioId = usuarioId;
    lblUsuario.setText("Usuario: " + nombreUsuario);
    mostrarPanel(panelBienvenida);
    Validaciones.configurarSoloEnteros(txtCantidadIngreso);
    Validaciones.configurarSoloEnteros(txtCantidadEgreso);
    cargarProductos();
    cargarMovimientos();
    cargarStock();
}
```

### 9.1 DEPOSITO - Ingreso de Mercaderia

#### 9.1.1 mostrarIngreso()

```java
@FXML private void mostrarIngreso() { mostrarPanel(panelIngreso); cargarProductos(); }
```

#### 9.1.2 handleRegistrarIngreso()

### ¿Que hace?
TRANSACCION: INSERT en movimientos_deposito (tipo=INGRESO) + UPDATE stock (+ cantidad) + INSERT auditoria. Commit/Rollback.

### Codigo SQL exacto

```sql
-- 1. INSERT movimiento:
INSERT INTO movimientos_deposito (fecha, hora, usuario_id, tipo_movimiento, producto_id, cantidad, observacion)
VALUES (?,?,?,?,?,?,?)

-- 2. UPDATE stock:
UPDATE productos SET stock_actual = stock_actual + ? WHERE id = ?

-- 3. INSERT auditoria:
INSERT INTO auditoria (usuario_id, accion, tabla_afectada, detalle)
VALUES (?,?,?,?)
```

### Flujo completo paso a paso

```
1. Seleccionar producto del ComboBox
2. Ingresar cantidad
3. Click "Registrar Ingreso"
4. handleRegistrarIngreso()
   └── getProductoIdSeleccionado() → parsea "ID - CODIGO - NOMBRE [Stock: N]"
   └── Si ID < 0 → ERROR
   └── validarCantidad(cantText) → si no → ERROR
   └── conn.setAutoCommit(false)
   └── TRY:
   │     ├── INSERT INTO movimientos_deposito (INGRESO)
   │     ├── UPDATE productos SET stock_actual = stock_actual + ?
   │     ├── INSERT INTO auditoria
   │     ├── conn.commit()
   │     ├── mostrarInfo("Ingreso registrado")
   │     ├── Limpiar campos, recargar productos
   │     └── CATCH → conn.rollback()
   └── FINALLY → conn.setAutoCommit(true)
```

### Codigo Java relevante

```java
@FXML
private void handleRegistrarIngreso() {
    int productoId = getProductoIdSeleccionado(cmbProductoIngreso);
    if (productoId < 0) {
        Validaciones.mostrarError("Seleccionar", "Seleccione un producto");
        return;
    }

    String cantText = txtCantidadIngreso.getText().trim();
    if (!Validaciones.validarCantidad(cantText)) {
        Validaciones.mostrarError("Cantidad invalida", "Ingrese una cantidad valida (numero entero positivo)");
        return;
    }

    int cantidad = Integer.parseInt(cantText);
    String observacion = txtObservacionIngreso.getText().trim();

    try {
        Connection conn = ConexionDB.getConexion();
        conn.setAutoCommit(false);

        try {
            // Registrar movimiento
            PreparedStatement stmtMov = conn.prepareStatement(
                "INSERT INTO movimientos_deposito (fecha, hora, usuario_id, tipo_movimiento, producto_id, cantidad, observacion) VALUES (?,?,?,?,?,?,?)");
            stmtMov.setDate(1, Date.valueOf(LocalDate.now()));
            stmtMov.setTime(2, Time.valueOf(LocalTime.now()));
            stmtMov.setInt(3, usuarioId);
            stmtMov.setString(4, "INGRESO");
            stmtMov.setInt(5, productoId);
            stmtMov.setInt(6, cantidad);
            stmtMov.setString(7, observacion.isEmpty() ? null : observacion);
            stmtMov.executeUpdate();
            stmtMov.close();

            // Actualizar stock
            PreparedStatement stmtStock = conn.prepareStatement(
                "UPDATE productos SET stock_actual = stock_actual + ? WHERE id = ?");
            stmtStock.setInt(1, cantidad);
            stmtStock.setInt(2, productoId);
            stmtStock.executeUpdate();
            stmtStock.close();

            // Auditoria
            PreparedStatement stmtAudit = conn.prepareStatement(
                "INSERT INTO auditoria (usuario_id, accion, tabla_afectada, detalle) VALUES (?,?,?,?)");
            stmtAudit.setInt(1, usuarioId);
            stmtAudit.setString(2, "INGRESO STOCK");
            stmtAudit.setString(3, "movimientos_deposito");
            stmtAudit.setString(4, "Ingreso de " + cantidad + " unidades (prod ID: " + productoId + ")");
            stmtAudit.executeUpdate();
            stmtAudit.close();

            conn.commit();

            Validaciones.mostrarInfo("Ingreso registrado",
                "Se ingresaron " + cantidad + " unidades correctamente");

            txtCantidadIngreso.clear();
            txtObservacionIngreso.clear();
            cargarProductos();

        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }

    } catch (Exception e) {
        Validaciones.mostrarError("Error", "No se pudo registrar el ingreso: " + e.getMessage());
    }
}
```

### 9.2 DEPOSITO - Nuevo Producto

#### 9.2.1 handleNuevoProducto()

### ¿Que hace?
Dialog simple con codigo, nombre y cantidad. TRANSACCION: INSERT producto + INSERT movimiento (INGRESO). Captura Duplicate en codigo.

### Codigo SQL exacto

```sql
-- 1. INSERT producto:
INSERT INTO productos (codigo, nombre, precio_compra, precio_venta, descuento, stock_actual, stock_minimo, activo)
VALUES (?,?,0,0,0,?,0,1)

-- 2. INSERT movimiento:
INSERT INTO movimientos_deposito (fecha, hora, usuario_id, tipo_movimiento, producto_id, cantidad, observacion)
VALUES (?,?,?,?,?,?,?)
```

### Codigo Java relevante

```java
@FXML
private void handleNuevoProducto() {
    Dialog<ObservableList<String>> dialog = new Dialog<>();
    dialog.setTitle("Nuevo Producto");
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
    TextField txtCodigo = new TextField();
    TextField txtNombre = new TextField();
    TextField txtCantidad = new TextField("1");

    Validaciones.configurarSoloEnteros(txtCantidad);
    Validaciones.limitarCaracteres(txtCodigo, 50);
    Validaciones.limitarCaracteres(txtNombre, 150);

    grid.addRow(0, new Label("Codigo:"), txtCodigo);
    grid.addRow(1, new Label("Nombre:"), txtNombre);
    grid.addRow(2, new Label("Cantidad Ingreso:"), txtCantidad);
    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(btn -> {
        if (btn == ButtonType.OK) {
            return FXCollections.observableArrayList(
                txtCodigo.getText(), txtNombre.getText(), txtCantidad.getText());
        }
        return null;
    });

    dialog.showAndWait().ifPresent(d -> {
        if (!Validaciones.validarNoVacio(d.get(0)) || !Validaciones.validarNoVacio(d.get(1))) {
            Validaciones.mostrarError("Requerido", "Codigo y nombre son obligatorios"); return;
        }
        int cantidad;
        try { cantidad = Integer.parseInt(d.get(2)); if (cantidad < 1) throw new Exception(); }
        catch (Exception e) { Validaciones.mostrarError("Cantidad", "Ingrese una cantidad valida"); return; }

        try {
            Connection conn = ConexionDB.getConexion();
            conn.setAutoCommit(false);
            try {
                PreparedStatement stmtProd = conn.prepareStatement(
                    "INSERT INTO productos (codigo, nombre, precio_compra, precio_venta, descuento, stock_actual, stock_minimo, activo) VALUES (?,?,0,0,0,?,0,1)",
                    Statement.RETURN_GENERATED_KEYS);
                stmtProd.setString(1, d.get(0));
                stmtProd.setString(2, d.get(1));
                stmtProd.setInt(3, cantidad);
                stmtProd.executeUpdate();

                ResultSet rs = stmtProd.getGeneratedKeys();
                int prodId = 0;
                if (rs.next()) prodId = rs.getInt(1);
                rs.close(); stmtProd.close();

                PreparedStatement stmtMov = conn.prepareStatement(
                    "INSERT INTO movimientos_deposito (fecha, hora, usuario_id, tipo_movimiento, producto_id, cantidad, observacion) VALUES (?,?,?,?,?,?,?)");
                stmtMov.setDate(1, Date.valueOf(LocalDate.now()));
                stmtMov.setTime(2, Time.valueOf(LocalTime.now()));
                stmtMov.setInt(3, usuarioId);
                stmtMov.setString(4, "INGRESO");
                stmtMov.setInt(5, prodId);
                stmtMov.setInt(6, cantidad);
                stmtMov.setString(7, "Producto nuevo");
                stmtMov.executeUpdate(); stmtMov.close();

                conn.commit();
                Validaciones.mostrarInfo("Exito", "Producto creado con " + cantidad + " unidades");
                cargarProductos();
            } catch (Exception e) {
                conn.rollback(); throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                Validaciones.mostrarError("Duplicado", "Ya existe un producto con ese codigo");
            } else {
                Validaciones.mostrarError("Error", e.getMessage());
            }
        }
    });
}
```

### 9.3 DEPOSITO - Egreso de Mercaderia

#### 9.3.1 handleRegistrarEgreso()

### Codigo SQL exacto

```sql
-- Verificacion previa:
SELECT stock_actual FROM productos WHERE id = ?

-- 1. INSERT movimiento:
INSERT INTO movimientos_deposito (fecha, hora, usuario_id, tipo_movimiento, producto_id, cantidad, observacion)
VALUES (?,?,?,?,?,?,?)

-- 2. UPDATE stock:
UPDATE productos SET stock_actual = stock_actual - ? WHERE id = ?

-- 3. INSERT auditoria:
INSERT INTO auditoria (usuario_id, accion, tabla_afectada, detalle)
VALUES (?,?,?,?)
```

### Flujo completo paso a paso

```
1. Seleccionar producto del ComboBox
2. Ingresar cantidad
3. Click "Registrar Egreso"
4. handleRegistrarEgreso()
   └── getProductoIdSeleccionado() → validar seleccion
   └── validarCantidad(cantText) → si no → ERROR
   └── conn = ConexionDB.getConexion()
   └── SELECT stock_actual FROM productos WHERE id = ?
   └── Si cantidad > stockActual → ERROR "Stock insuficiente"
   └── conn.setAutoCommit(false)
   └── TRY:
   │     ├── INSERT INTO movimientos_deposito (EGRESO)
   │     ├── UPDATE productos SET stock_actual = stock_actual - ?
   │     ├── INSERT INTO auditoria
   │     ├── conn.commit()
   │     └── CATCH → conn.rollback()
   └── FINALLY → conn.setAutoCommit(true)
```

### 9.4 DEPOSITO - Historial

### Codigo SQL exacto

```sql
SELECT m.fecha, m.hora, m.tipo_movimiento, p.nombre as producto,
       m.cantidad, m.observacion
FROM movimientos_deposito m
JOIN productos p ON m.producto_id = p.id
ORDER BY m.id DESC
```

### 9.5 DEPOSITO - Stock

### Codigo SQL exacto

```sql
SELECT codigo, nombre, categoria, stock_actual, stock_minimo
FROM productos WHERE activo=1
```

### FXML deposito

```xml
<BorderPane styleClass="dashboard-background">
  <top>
    <HBox styleClass="top-bar">
      <Text text="Panel de Deposito" styleClass="top-bar-title" />
      <Label fx:id="lblUsuario" styleClass="top-bar-user" />
      <Button onAction="#handleCerrarSesion" text="Cerrar Sesion" />
    </HBox>
  </top>
  <left>
    <VBox styleClass="sidebar">
      <Text text="Menu" styleClass="sidebar-title" />
      <Button onAction="#mostrarIngreso" text="Ingreso Mercaderia" />
      <Button onAction="#mostrarEgreso" text="Egreso Mercaderia" />
      <Button onAction="#mostrarHistorial" text="Historial Movimientos" />
      <Button onAction="#mostrarStock" text="Stock Actual" />
    </VBox>
  </left>
  <center>
    <ScrollPane>
      <VBox fx:id="contenidoPrincipal">
        <!-- panelBienvenida -->
        <!-- panelIngreso: ComboBox producto + cantidad + observacion + Registrar + Nuevo Producto -->
        <!-- panelEgreso: ComboBox producto + cantidad + observacion + Registrar -->
        <!-- panelHistorial: tabla movimientos -->
        <!-- panelStock: tabla stock -->
      </VBox>
    </ScrollPane>
  </center>
</BorderPane>
```

---

## 10. Modelos de Datos

### 10.1 Usuario.java

```java
public class Usuario {
    private int id;
    private String nombre;
    private String apellido;
    private String usuario;
    private String contrasena;
    private String telefono;
    private String email;
    private String rol;       // ADMIN, VENDEDOR, DEPOSITARIO
    private boolean activo;
}
```

### 10.2 Cliente.java

```java
public class Cliente {
    private int id;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String email;
    private String direccion;
    private boolean activo;
}
```

### 10.3 Proveedor.java

```java
public class Proveedor {
    private int id;
    private String nombreEmpresa;
    private String nombreContacto;
    private String telefono;
    private String email;
    private String direccion;
    private boolean activo;
}
```

### 10.4 Producto.java

```java
public class Producto {
    private int id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String categoria;
    private double precioCompra;
    private double precioVenta;
    private double descuento;
    private int stockActual;
    private int stockMinimo;
    private int proveedorId;
    private String nombreProveedor; // Para mostrar
    private boolean activo;
}
```

### 10.5 Factura.java

```java
public class Factura {
    private int id;
    private String numeroFactura;
    private LocalDate fecha;
    private LocalTime hora;
    private int usuarioId;
    private String nombreVendedor;
    private int clienteId;
    private String nombreCliente;
    private double subtotal;
    private double descuentoGlobal;
    private double iva;
    private double total;
    private String estado;
}
```

### 10.6 DetalleFactura.java

```java
public class DetalleFactura {
    private int id;
    private int facturaId;
    private int productoId;
    private String nombreProducto;
    private String codigoProducto;
    private int cantidad;
    private double precioUnitario;
    private double descuentoPorcentaje;
    private double descuentoMonto;
    private double subtotalLinea;
}
```

### 10.7 MovimientoDeposito.java

```java
public class MovimientoDeposito {
    private int id;
    private LocalDate fecha;
    private LocalTime hora;
    private int usuarioId;
    private String nombreUsuario;
    private String tipoMovimiento;  // INGRESO o EGRESO
    private int productoId;
    private String nombreProducto;
    private String codigoProducto;
    private int cantidad;
    private String observacion;
}
```

---

## 11. Esquema de Base de Datos

### 11.1 Tabla: usuarios

| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | INT | AUTO_INCREMENT, PRIMARY KEY |
| nombre | VARCHAR(100) | NOT NULL |
| apellido | VARCHAR(100) | NOT NULL |
| usuario | VARCHAR(50) | NOT NULL, UNIQUE |
| contrasena | VARCHAR(255) | NOT NULL |
| telefono | VARCHAR(20) | nullable |
| email | VARCHAR(100) | nullable |
| rol | ENUM('ADMIN','VENDEDOR','DEPOSITARIO') | NOT NULL, DEFAULT 'VENDEDOR' |
| activo | TINYINT(1) | NOT NULL, DEFAULT 1 |
| fecha_creacion | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

### 11.2 Tabla: clientes

| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | INT | AUTO_INCREMENT, PRIMARY KEY |
| nombre | VARCHAR(100) | NOT NULL |
| apellido | VARCHAR(100) | NOT NULL |
| dni | VARCHAR(20) | NOT NULL, UNIQUE |
| telefono | VARCHAR(20) | nullable |
| email | VARCHAR(100) | nullable |
| direccion | VARCHAR(255) | nullable |
| activo | TINYINT(1) | NOT NULL, DEFAULT 1 |
| fecha_creacion | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

### 11.3 Tabla: proveedores

| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | INT | AUTO_INCREMENT, PRIMARY KEY |
| nombre_empresa | VARCHAR(150) | NOT NULL |
| nombre_contacto | VARCHAR(100) | nullable |
| telefono | VARCHAR(20) | nullable |
| email | VARCHAR(100) | nullable |
| direccion | VARCHAR(255) | nullable |
| activo | TINYINT(1) | NOT NULL, DEFAULT 1 |
| fecha_creacion | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

### 11.4 Tabla: productos

| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | INT | AUTO_INCREMENT, PRIMARY KEY |
| codigo | VARCHAR(50) | NOT NULL, UNIQUE |
| nombre | VARCHAR(150) | NOT NULL |
| descripcion | TEXT | nullable |
| categoria | VARCHAR(100) | nullable |
| precio_compra | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 |
| precio_venta | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 |
| descuento | DECIMAL(5,2) | NOT NULL, DEFAULT 0.00 |
| stock_actual | INT | NOT NULL, DEFAULT 0 |
| stock_minimo | INT | NOT NULL, DEFAULT 0 |
| proveedor_id | INT | FK → proveedores(id) ON DELETE SET NULL |
| activo | TINYINT(1) | NOT NULL, DEFAULT 1 |
| fecha_creacion | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

### 11.5 Tabla: facturas

| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | INT | AUTO_INCREMENT, PRIMARY KEY |
| numero_factura | VARCHAR(20) | NOT NULL, UNIQUE |
| fecha | DATE | NOT NULL |
| hora | TIME | NOT NULL |
| usuario_id | INT | NOT NULL, FK → usuarios(id) |
| cliente_id | INT | nullable, FK → clientes(id) ON DELETE SET NULL |
| subtotal | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 |
| descuento_global | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 |
| iva | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 |
| total | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 |
| estado | VARCHAR(20) | NOT NULL, DEFAULT 'EMITIDA' |
| fecha_creacion | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

### 11.6 Tabla: detalle_factura

| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | INT | AUTO_INCREMENT, PRIMARY KEY |
| factura_id | INT | NOT NULL, FK → facturas(id) ON DELETE CASCADE |
| producto_id | INT | NOT NULL, FK → productos(id) |
| cantidad | INT | NOT NULL |
| precio_unitario | DECIMAL(10,2) | NOT NULL |
| descuento_porcentaje | DECIMAL(5,2) | NOT NULL, DEFAULT 0.00 |
| descuento_monto | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 |
| subtotal_linea | DECIMAL(10,2) | NOT NULL |

### 11.7 Tabla: movimientos_deposito

| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | INT | AUTO_INCREMENT, PRIMARY KEY |
| fecha | DATE | NOT NULL |
| hora | TIME | NOT NULL |
| usuario_id | INT | NOT NULL, FK → usuarios(id) |
| tipo_movimiento | ENUM('INGRESO','EGRESO') | NOT NULL |
| producto_id | INT | NOT NULL, FK → productos(id) |
| cantidad | INT | NOT NULL |
| observacion | TEXT | nullable |
| fecha_creacion | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

### 11.8 Tabla: auditoria

| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | INT | AUTO_INCREMENT, PRIMARY KEY |
| usuario_id | INT | nullable, FK → usuarios(id) ON DELETE SET NULL |
| accion | VARCHAR(255) | NOT NULL |
| tabla_afectada | VARCHAR(100) | nullable |
| registro_id | INT | nullable |
| detalle | TEXT | nullable |
| fecha | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

### Diagrama de Relaciones

```
usuarios ──────┐
               │
               ├──< facturas (usuario_id)
               │
               ├──< movimientos_deposito (usuario_id)
               │
               └──< auditoria (usuario_id)

clientes ──────┐
               └──< facturas (cliente_id)

proveedores ───┐
               └──< productos (proveedor_id)

productos ─────┐
               ├──< detalle_factura (producto_id)
               └──< movimientos_deposito (producto_id)

facturas ──────┐
               └──< detalle_factura (factura_id) ON DELETE CASCADE
```

---

## 12. CSS - Hoja de Estilos

### Estilos globales

```css
.root {
    -fx-font-family: "Segoe UI", "System", sans-serif;
    -fx-font-size: 13px;
    -fx-background-color: #f0f2f5;
}
```

### Login

| Clase CSS | Propiedades |
|-----------|-------------|
| `.login-background` | Degradado azul `linear-gradient(to right, #1a73e8, #1557b0)` |
| `.login-panel` | Fondo blanco, border-radius 10, sombreado, padding 40 |
| `.login-title` | Color azul `#1a73e8`, bold |
| `.login-subtitle` | Color gris `#666` |
| `.login-field` | Height 40, borde gris `#ddd`, al focus borde azul |

### Registro

| Clase CSS | Propiedades |
|-----------|-------------|
| `.registro-background` | Degradado verde `linear-gradient(to right, #34a853, #1e8e3e)` |
| `.registro-panel` | Fondo blanco, border-radius 10, sombreado, padding 30 |
| `.registro-title` | Color verde `#34a853`, bold |

### Dashboard

| Clase CSS | Propiedades |
|-----------|-------------|
| `.dashboard-background` | Fondo gris `#f0f2f5` |
| `.top-bar` | Fondo blanco, padding horizontal, sombra sutil |
| `.top-bar-title` | Color azul `#1a73e8`, bold |
| `.top-bar-user` | Padding, color gris `#555`, bold |
| `.sidebar` | Fondo oscuro `#2c3e50`, padding, ancho minimo 200 |
| `.sidebar-title` | Texto blanco, bold |
| `.sidebar .button` | Fondo transparente, texto blanco, hover `#34495e` |

### Botones

| Clase CSS | Fondo | Hover |
|-----------|-------|-------|
| `.btn-primary` | `#1a73e8` | `#1557b0` |
| `.btn-secondary` | `#6c757d` | `#5a6268` |
| `.btn-danger` | `#dc3545` | `#c82333` |
| `.btn-logout` | Transparente, borde rojo | Fondo rojo `#dc3545`, texto blanco |

### Tablas

```css
.table-view {
    -fx-background-color: white;
    -fx-border-color: #ddd;
    -fx-border-radius: 5px;
}
.table-view .column-header {
    -fx-background-color: #f8f9fa;
    -fx-font-weight: bold;
}
.table-row-cell:selected {
    -fx-background-color: #e8f0fe;
}
.table-cell {
    -fx-padding: 4px 8px;
    -fx-font-size: 12px;
}
```

### Tarjetas de estadisticas

```css
.stat-card {
    -fx-background-color: white;
    -fx-background-radius: 10px;
    -fx-padding: 20px;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);
}
.stat-label {
    -fx-fill: #888;
    -fx-font-size: 14px;
}
.stat-value {
    -fx-fill: #1a73e8;
    -fx-font-size: 24px;
    -fx-font-weight: bold;
}
```

### Etiquetas de totales

| Clase CSS | Font-size | Color |
|-----------|-----------|-------|
| `.label-total` | 14px, bold | `#333` |
| `.label-total-grande` | 18px, bold | `#1a73e8` |

### Bienvenida

```css
.welcome-text {
    -fx-fill: #333;
    -fx-font-size: 28px;
    -fx-font-weight: bold;
}
.welcome-subtext {
    -fx-fill: #888;
    -fx-font-size: 16px;
}
```

---

## 13. Configuracion del Proyecto (pom.xml)

### Dependencias

| Dependencia | Version | Proposito |
|-------------|---------|-----------|
| `mariadb-java-client` | 3.3.2 | Driver JDBC para MariaDB |
| `dotenv-java` | 3.0.0 | Lectura de archivo `.env` |
| `itext7-core` | 7.2.5 | Exportacion a PDF (funcionalidad futura) |
| `javafx-controls` | 21 | Controles JavaFX |
| `javafx-fxml` | 21 | Soporte FXML |

### Plugins

| Plugin | Version | Proposito |
|--------|---------|-----------|
| `maven-compiler-plugin` | 3.11.0 | Compilar con Java 17 (source/target) |
| `javafx-maven-plugin` | 0.0.8 | Ejecutar aplicacion JavaFX (`app.Main`) |

### Ejecucion

```bash
mvn clean javafx:run
```

---

## 14. Datos de Prueba

### Usuarios de prueba

| Usuario | Contrasena | Rol |
|---------|-----------|-----|
| admin | admin123 | ADMIN |
| vendedor1 | vendedor1 | VENDEDOR |
| vendedor2 | vendedor2 | VENDEDOR |
| deposito1 | deposito1 | DEPOSITARIO |

### Clientes de prueba

| Nombre | Apellido | DNI |
|--------|----------|-----|
| Pedro | Ramirez | 12345678 |
| Laura | Martinez | 23456789 |
| Roberto | Gimenez | 34567890 |
| Ana | Fernandez | 45678901 |

### Proveedores de prueba

| Empresa | Contacto |
|---------|----------|
| Distribuidora ABC | Jorge Benitez |
| Mayorista XYZ | Sofia Acosta |
| Importadora del Sur | Miguel Torres |
| Tech Solutions | Lucia Vega |

### Productos de prueba (8 productos)

| Codigo | Nombre | Precio Venta | Stock |
|--------|--------|--------------|-------|
| PROD-001 | Laptop HP | $5,200.00 | 15 |
| PROD-002 | Mouse Inalambrico | $150.00 | 50 |
| PROD-003 | Teclado Mecanico | $450.00 | 30 |
| PROD-004 | Monitor 24 Pulgadas | $1,800.00 | 20 |
| PROD-005 | Silla Ergo Pro | $1,400.00 | 10 |
| PROD-006 | Escritorio Ejecutivo | $1,100.00 | 8 |
| PROD-007 | Papel A4 5000 hojas | $250.00 | 100 |
| PROD-008 | Boligrafo x10 | $50.00 | 200 |

---

## Tabla de metodos CRUD por controlador

### AdminController

| Metodo | SQL | Accion |
|--------|-----|--------|
| cargarUsuarios() | SELECT | Read |
| handleNuevoUsuario() | INSERT | Create |
| handleEditarUsuario() | UPDATE | Update |
| handleEliminarUsuario() | UPDATE activo=0 | Soft Delete |
| cargarClientes() | SELECT ... WHERE activo=1 | Read |
| mostrarDialogoCliente(null) | INSERT | Create |
| mostrarDialogoCliente(valores) | UPDATE ... WHERE id=? | Update |
| handleEliminarCliente() | UPDATE activo=0 | Soft Delete |
| cargarProveedores() | SELECT ... WHERE activo=1 | Read |
| mostrarDialogoProveedor(null) | INSERT | Create |
| mostrarDialogoProveedor(valores) | UPDATE ... WHERE id=? | Update |
| handleEliminarProveedor() | UPDATE activo=0 | Soft Delete |
| cargarProductos() | SELECT (JOIN proveedores) | Read |
| handleNuevoProducto() | INSERT | Create |
| handleEditarProducto() | SELECT + UPDATE | Read + Update |
| handleEmitirFactura() | INSERT + INSERT batch + UPDATE batch + INSERT | Transaction |

### VendedorController

| Metodo | SQL | Accion |
|--------|-----|--------|
| cargarClientes() | SELECT ... WHERE activo=1 | Read only |
| cargarStock() | SELECT ... WHERE activo=1 | Read only |
| cargarMisFacturas() | SELECT ... WHERE usuario_id=? | Read only |
| handleEmitirFactura() | INSERT + INSERT batch + UPDATE batch + INSERT | Transaction |

### DepositoController

| Metodo | SQL | Accion |
|--------|-----|--------|
| cargarProductos() | SELECT id,codigo,nombre,stock FROM productos WHERE activo=1 | Read |
| handleRegistrarIngreso() | INSERT + UPDATE stock + INSERT audit | Transaction |
| handleRegistrarEgreso() | SELECT stock + INSERT + UPDATE stock + INSERT audit | Transaction |
| handleNuevoProducto() | INSERT producto + INSERT movimiento | Transaction |
| cargarMovimientos() | SELECT (JOIN productos) | Read |
| cargarStock() | SELECT ... WHERE activo=1 | Read |

---

## Tabla de transacciones SQL (autoCommit=false / commit / rollback)

| Funcion | Pasos en la transaccion | Tablas afectadas |
|---------|------------------------|------------------|
| handleEmitirFactura() (Admin/Vendedor) | INSERT factura → INSERT detalle batch → UPDATE stock batch → INSERT auditoria | facturas, detalle_factura, productos, auditoria |
| handleRegistrarIngreso() (Deposito) | INSERT movimiento → UPDATE stock → INSERT auditoria | movimientos_deposito, productos, auditoria |
| handleRegistrarEgreso() (Deposito) | SELECT stock (pre-check) → INSERT movimiento → UPDATE stock → INSERT auditoria | movimientos_deposito, productos, auditoria |
| handleNuevoProducto() (Deposito) | INSERT producto → INSERT movimiento | productos, movimientos_deposito |

---

## Tabla de validaciones por formulario/dialog

| Formulario | Campos con validacion |
|------------|----------------------|
| Login | usuario y contrasena: validarNoVacio |
| Registro | nombre, apellido: soloLetras (filtro). telefono: soloNumeros (filtro). contrasena: length>=8. email: email regex. telefono: telefono regex |
| Admin Nuevo Usuario | nombre, apellido: soloLetras (filtro). telefono: soloNumeros (filtro). usuario: limit 50. email: limit 100 |
| Admin Cliente Dialog | nombre, apellido: soloLetras. dni, telefono: soloNumeros. email: limit 100. direccion: limit 255 |
| Admin Proveedor Dialog | contacto: soloLetras. telefono: soloNumeros. empresa: limit 150. email: limit 100. direccion: limit 255 |
| Admin Producto Dialog | precios: soloDecimales. stock: soloEnteros. codigo: limit 50. nombre: limit 150. descripcion: limit 255. categoria: limit 100 |
| Admin Factura Cant | txtCantidadFactura: configurarCantidadField() → regex `[1-9][0-9]*` |
| Deposito Ingreso/Egreso | cantidad: soloEnteros + validarCantidad |
| Deposito Nuevo Producto | cantidad: soloEnteros. codigo: limit 50. nombre: limit 150 |
