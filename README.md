# 🚀 MOD6 AE1-ABP1: Gestión de Notas Rápidas para Reuniones (MVVM + Room)

<p float="center">
  <img src="scrapbook/perasconmanzanas_icon.png" alt="Logo" width="200"/>
</p>

Aplicación nativa para Android, desarrollada en Kotlin, diseñada para registrar de forma rápida y simple notas, recordatorios o apuntes de reuniones. Utiliza el patrón de arquitectura Model-View-ViewModel (MVVM), estableciendo la separación de responsabilidades, escalabilidad y un flujo de datos completamente reactivo y estable.

El objetivo académico principal es la integración, de los componentes de Android Jetpack, utilizando Room para la persistencia local y LiveData y Kotlin Coroutines, para la gestión asíncrona y reactiva de los datos.

---

## 🎯 Requerimientos de Funcionalidad y su Implementación

| Requerimiento                   | Implementación en V5                                                                                                                                                  |
| ------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1. Creación/Edición de Notas    | El fragmento NoteDetailFragment aloja el formulario de entrada, permitiendo registrar (o editar) el título, el cliente y el contenido de la nota.                     |
| 2. Listado Dinámico (Home)      | El fragmento HomeFragment utiliza un RecyclerView con NoteAdapter. La lista se actualiza reactivamente al observar NotesViewModel.allNotes (LiveData).                |
| 3. Ciclo de Vida y Persistencia | La persistencia de los datos (INSERT, UPDATE, DELETE) se ejecuta de forma segura fuera del hilo principal usando Kotlin Coroutines para no bloquear la UI.            |
| 4. Menú Contextual              | En la creación de notas se muestra la opción Guardar/Cancelar. En la edición se añade la opción Eliminar, que solo es visible si se está editando una nota existente. |
| 5. Estado Vacío (Empty State)   | La lista de notas en HomeFragment muestra un mensaje informativo cuando no hay registros, indicando al usuario que use el botón "Añadir" (FAB).                       |

---

## 🧠 Arquitectura y Tecnología: MVVM y Jetpack

Se implementa el patrón MVVM para garantizar una arquitectura limpia, mantenible y escalable.

1. Modelo (Model) y Persistencia (Room)

| Componente                      | Descripción                                                                                                                              |
| ------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| Modelo de Datos (NoteEntity.kt) | Clase de datos que define la estructura de una nota (ID, Título, Cliente, Contenido, Fecha de Creación).                                 |
| Database (NoteDatabase.kt)      | Clase abstracta que gestiona la base de datos Room, incluyendo el TypeConverter para Date.                                               |
| DAO (NoteDao.kt)                | Interfaz que define las operaciones CRUD (@Insert, @Update, @Delete, @Query) y expone la lista de notas como LiveData<List<NoteEntity>>. |
| Repositorio (NoteRepository.kt) | Centraliza el acceso a los datos (NoteDao), encapsulando la lógica de I/O dentro de funciones suspend.                                   |

2. ViewModel (NotesViewModel.kt)

- Hereda de ViewModel.

- Utiliza viewModelScope.launch para ejecutar las operaciones del Repository de forma asíncrona.

- Mantiene la lógica de negocio (guardar, actualizar, eliminar, cargar nota por ID).

- Expone el estado de la aplicación a la Vista a través de LiveData:

  - allNotes: Lista de notas que alimenta el RecyclerView de forma reactiva.

  - currentNoteTitle, currentNoteClient, currentNoteContent: MutableLiveData que gestionan el estado temporal del formulario de edición/creación.

3. Vista (View)

- MainActivity: Contenedor de la aplicación y orquestador de la navegación mediante Jetpack Navigation.

- HomeFragment: Solo observa taskViewModel.allNotes y usa el NoteAdapter para actualizar el RecyclerView de forma reactiva.

- NoteDetailFragment:

  - Gestiona el formulario y los listeners.

  - Observa las propiedades currentNoteTitle, currentNoteClient, etc., del ViewModel.

  - Implementa el MenuProvider para manejar las opciones de menú dinámicas (Guardar y Eliminar).

## ✨ Reactividad y Flujo de Datos

El flujo de datos está diseñado para ser completamente reactivo:

1. Carga de Datos: **HomeFragment** llama a **`viewModel.loadNotes()`**. El ViewModel ejecuta la consulta a Room en un hilo de fondo.

2. Persistencia y Actualización: La consulta del **_DAO_** retorna un **LiveData**. Cuando se realiza un **INSERT/UPDATE/DELETE**, Room modifica automáticamente los datos de la base, y el LiveData se dispara.

3. Sincronización de UI: El observador en **HomeFragment** detecta el cambio en el LiveData y llama a **`adapter.submitList()`**, actualizando el RecyclerView sin intervención manual de recarga de la lista.

## 🛠️ Tecnologías usadas

- **IDE** : Android Studio
- **Plataforma** : Android Nativo
- **Lenguaje** : Kotlin (1.9.22)
- **Arquitectura**: MVVM (Model-View-ViewModel).
- **Persistencia**: Room Database (SQL Abstraction).
- **Concurrencia**: Kotlin Coroutines y viewModelScope (Dispatchers.IO).
- **Comunicación**: LiveData (Reactividad) y Data Binding (implícito a través de View Binding).
- **Navegación** : Jetpack Navigation Component.

---

## 🏗️ Funcionamiento de la Aplicación

El flujo base es el siguiente:

1. Inicio y Navegación: La aplicación muestra la pantalla de bienvenida y luego la MainActivity orquesta la navegación a través de la BottomNavigationView entre VerTareasFragment (Ver Agenda) y CrearTareaFragment (Agregar).
2. Vista Agenda (VerTareasFragment):
   - Observa el taskViewModel.allTasks (LiveData).
   - Cuando el ViewModel actualiza esta lista, el RecyclerView se redibuja automáticamente (reactividad).
   - Maneja la acción de eliminar o marcar como completada, llamando a los métodos correspondientes en el ViewModel.
3. Crear/Editar Evento (CrearTareaFragment):
   - El usuario ingresa o edita los datos.
   - Al presionar "Guardar" o "Actualizar":
     - Se realiza la validación de campos obligatorios.
     - Si se requiere alarma (Notificación), se verifica/solicita el permiso de Notificaciones (POST_NOTIFICATIONS) usando registerForActivityResult.
     - Se llama a taskViewModel.saveOrUpdateTask(), que ejecuta la lógica de persistencia en el TaskRepository fuera del hilo principal.
     - La vista (Fragment) observa el taskViewModel.statusMessage para mostrar un Toast de confirmación de forma segura.
     - Finalmente, la vista navega de vuelta a la Agenda.
4. Las tareas listadas, se puede seleccionar para ser editadas.
5. Cada tarea tiene un botón eliminar que permite proceder al borrado explícito, debiendo confirmar la acción.

---

## ⭐ Capturas de Pantalla

<table width="100%">
    <tr>
        <td align="center" width="33%">
            <img src="scrapbook/IconoApp.png" alt="Icono App" width="200"/>
        </td>
        <td align="center" width="33%">
            <img src="scrapbook/LanzarApp.png" alt="Al lanzar la app" width="200"/>
        </td>
        <td align="center" width="33%">
            <img src="scrapbook/InicialSinDatos.png" alt="Pantalla bienvenida" width="200"/>
        </td>
    </tr>
    <tr>
        <td align="center">App instalada</td>
        <td align="center">Al lanzar la App</td>
        <td align="center">Pantalla Inicial Sin Datos</td>
    </tr>
    <tr>
        <td align="center">
            <img src="scrapbook/Crear.png" alt="Formulario crear/ editar tarea" width="200"/>
        </td>
        <td align="center">
            <img src="scrapbook/Listado.png" alt="Selector de fecha" width="200"/>
        </td>
        <td align="center">
            <img src="scrapbook/Editar.png" alt="Selector de hora" width="200"/>
        </td>
    </tr>
    <tr>
        <td align="center">Crear Nota</td>
        <td align="center">Listado actualizado</td>
        <td align="center">Editar una Nota</td>
    </tr>
    <tr>
        <td align="center">
            <img src="scrapbook/ListaActualizada.png" alt="Selector de estados" width="200"/>
        </td>
        <td align="center">
            <img src="scrapbook/Eliminar.png" alt="Selector categorías" width="200"/>
        </td>
        <td align="center">
            <img src="scrapbook/Actualizada.png" alt="Toast guardar" width="200"/>
        </td>
    </tr>
    <tr>
        <td align="center">Lista actualizada</td>
        <td align="center">Eliminar Nota</td>
        <td align="center">Actualización </td>
    </tr>
</table>

---

## 🔎 Guía de Ejecución del Proyecto

**Para ejecutar este proyecto en tu entorno de desarrollo, siga estos 'quick steps':**

    1.**Clonar el Repo:** Clona el proyecto en su máquina local.

    2.**Abrir en Android Studio:** Abra la carpeta del proyecto con Android Studio. El IDE detectará automáticamente la configuración de Gradle.

    3.**Sincronizar Gradle:** Haz clic en el botón "Sync Now" si Android Studio te lo solicita. Esto descargará todas las dependencias necesarias.

    4.**Ejecutar:** Conecta un dispositivo Android físico o inicia un emulador. Luego, haz clic en el botón "Run 'app'" (el ícono de la flecha verde) para desplegar la aplicación.

**Para ejecutar este proyecto en tu celular, sigue estos 'quick steps':**

    1.**Copiar la APK:** Copia la aplicación (APK) en tu celular.

    2.**Instalar:** Instala la aplicación, salta los avisos de advertencia, es normal si la aplicación no ha sido productivizada la plataforma de Android.

    3.**Abrir la App:** Haz doble clic en el ícono de _**Peras con Manzanas para abrir**_ "GesTarea V5".

    4.**Recorrer las opciones:** Cliquea en las opciones y podrás acceder al listado de eventos, editar cada evento, crear nuevos eventos, regresando a cualquier punto de la app.

---

## 🛑 Instalación y Configuración

a. **Clonar el repositorio:**

```bash

https://github.com/jcordovaj/ae1_abp1_Notes.git


```

b. **Abrir el Proyecto en Android Studio:**

b.1. Abrir Android Studio.

b.2. En la pantalla de bienvenida, seleccionar **"Open an existing Android Studio project"** (Abrir un proyecto de Android Studio existente).

b.3. Navegar a la carpeta donde se clonó el repositorio y seleccionarla. Android Studio detectará automáticamente el proyecto de Gradle y comenzará a indexar los archivos.

c. **Sincronizar Gradle:**

c.1. Este es el paso más importante. Después de abrir el proyecto, Android Studio intentará sincronizar la configuración de Gradle. Esto significa que descargará todas las librerías, dependencias y plugins necesarios para construir la aplicación. Normalmente, una barra de progreso se mostrará en la parte inferior de la consola de Android Studio con un mensaje como **"Gradle Sync in progress"**.

c.2. Si no se inicia, o si el proceso falla, intente con el botón **"Sync Project with Gradle Files"** en la barra de herramientas. Es el icono con el **"elefante" de Gradle**. Eso forzará la sincronización.

c.3. Esperar que el proceso de sincronización termine. De haber errores, puede ser por problemas en la configuración de Android u otros conflictos, la aplicación debe descargar lo que requiera y poder ser ejecutada "AS-IS".

d. **Configurar el Dispositivo o Emulador:**

Para ejecutar la aplicación, se requiere un dispositivo Android, puedes usarse el emulador virtual o un dispositivo físico.

d.1. Emulador: En la barra de herramientas, haga click en el botón del "AVD Manager" (Android Virtual Device Manager), que es el icono de un teléfono móvil con el logo de Android. Desde ahí, puedes crear un nuevo emulador con la versión de Android que prefiera (Nota: Debe considerar que cada celular emulado, puede requerir más de 1GB de espacio en disco y recursos de memoria).

d.2. Dispositivo físico: Conecte su teléfono Android a la computadora con un cable USB (también puede ser por WI-FI). Asegúrese de que las **Opciones de desarrollador y la Depuración por USB** estén habilitadas en su dispositivo. Consulte a su fabricante para activar estas opciones.

e. **Ejecutar la aplicación:**

e.1. Seleccione el dispositivo o emulador deseado en la barra de herramientas del emulador.

e.2. Haga click en el botón "Run 'app'" (el triángulo verde en la parte superior, o vaya al menu "RUN") para iniciar la compilación y el despliegue de la aplicación, puede tardar algunos minutos, dependiendo de su computador.

e.3. Si todo ha sido configurado correctamente, la aplicación se instalará en el dispositivo y se iniciará automáticamente, mostrando la pantalla de inicio.

---

## 🎉 Contribuciones (Things-To-Do)

Se puede contribuir reportando problemas o con nuevas ideas, por favor respetar el estilo de programación y no subir código basura. Puede utilizar: forking del repositorio, crear pull requests, etc. Toda contribución es bienvenida.

---

## 🔹 Licencia

Proyecto con fines educativos.
