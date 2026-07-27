# Ficha de Pedido — App Android

App sencilla en Kotlin que genera un PDF de "Ficha de Pedido" con los mismos campos del archivo `Ficha_de_Pedido.md`.

## Cómo obtener el archivo APK sin instalar Android Studio

Este proyecto incluye un workflow de GitHub Actions (`.github/workflows/build-apk.yml`) que compila el APK automáticamente en la nube. Pasos:

1. Crea un repositorio nuevo en GitHub (puede ser privado).
2. Sube el contenido de esta carpeta `FichaPedidoApp` a ese repositorio (arrastrando los archivos en la web de GitHub, o con `git push`).
3. Entra a la pestaña **Actions** del repositorio — se disparará solo y compilará el APK (tarda 2-4 minutos).
4. Cuando termine (círculo verde ✅), entra al workflow → sección **Artifacts** → descarga `FichaPedido-app-debug`. Ahí está el `app-debug.apk`.
5. Copia ese APK a tu celular (por USB, WhatsApp, Google Drive, etc.) y ábrelo para instalarlo. Android te pedirá permitir "instalar apps de orígenes desconocidos" la primera vez — es normal en apps que no vienen de la Play Store.

## Cómo abrir el proyecto en Android Studio (alternativa)

1. Descomprime el archivo `.zip`.
2. Abre **Android Studio** → `File > Open` → selecciona la carpeta `FichaPedidoApp`.
3. Espera a que Android Studio sincronice Gradle (si te pide instalar el "Gradle Wrapper", acepta — Android Studio lo genera automáticamente).
4. Conecta un celular Android (o usa un emulador) y presiona ▶ (Run).

Requisitos: Android Studio actualizado, Android 6.0 (API 23) o superior en el dispositivo.

## Qué hace la app

- Formulario con los campos: Fecha de emisión, Nombre, Teléfonos, Localizado, Artículos, Color, Precio, Fecha de entrega, Nota y Vendedor/a (pre-llenado con "Jose Manuel Santillan").
- Los campos **Fecha de emisión** y **Fecha de entrega** abren un calendario (DatePicker) al tocarlos — no se escriben a mano.
- Al presionar **Generar PDF**:
  - Se crea un PDF con el diseño de la ficha.
  - El nombre del archivo se genera automáticamente así:
    `FICHA DE PEDIDO(fecha_de_creación)-(últimos_4_dígitos_del_teléfono).pdf`
    Ejemplo: `FICHA DE PEDIDO(27-07-2026)-1234.pdf`
  - El PDF se guarda en una carpeta llamada **`pedidos`**.
  - La app intenta abrir el PDF automáticamente con el visor de PDF instalado en el teléfono.

## Sobre la carpeta "pedidos"

Por defecto, el PDF se guarda en el almacenamiento **propio de la app** (no requiere permisos especiales de Android ni pedir acceso a "Todos los archivos"):

```
Almacenamiento interno del teléfono / Android / data / com.planetaweb.fichapedido / files / pedidos /
```

Puedes ver esa carpeta con cualquier explorador de archivos (ej. "Mis Archivos" / "Files"), o simplemente tocar el botón "Generar PDF": la app abre el PDF directamente sin que tengas que buscarlo.

> Si prefieres que los PDF se guarden en una carpeta pública más fácil de encontrar (ej. `Documentos/pedidos` visible desde cualquier app), se puede ajustar el código para usar `MediaStore` — solo dime y lo actualizo. Esa opción requiere manejar el permiso de almacenamiento en Android 9 o anteriores.

## Personalizar

- Cambiar el nombre del vendedor por defecto: en `activity_main.xml`, busca `etVendedor` y cambia el texto `Jose Manuel Santillan`.
- Cambiar colores o el ícono de la app: en `res/values/themes.xml` y `res/drawable/ic_launcher_*.xml`.
