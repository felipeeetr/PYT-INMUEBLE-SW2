package inmuebles.adaptadores.entrada.javafx;

import inmuebles.aplicacion.dto.InmuebleDto;
import inmuebles.aplicacion.dto.PropietarioDto;
import inmuebles.dominio.modelo.EstadoInmueble;
import inmuebles.dominio.modelo.TipoUso;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class VistaInmuebles {
    private final ControladorInmuebles controlador;
    private final TableView<InmuebleDto> tabla = new TableView<>();
    private final TextField chip = new TextField();
    private final DatePicker fechaCompra = new DatePicker();
    private final TextField direccion = new TextField();
    private final ChoiceBox<EstadoInmueble> estado = new ChoiceBox<>();
    private final ChoiceBox<String> tipo = new ChoiceBox<>();
    private final ComboBox<PropietarioDto> propietario = new ComboBox<>();
    private final VBox camposEspecificos = new VBox(10);
    private final TextField numeroPiso = new TextField();
    private final TextField cantidadPisos = new TextField();
    private final CheckBox tieneSotano = new CheckBox("La casa tiene sótano");
    private final TextField areaSotano = new TextField();
    private final ChoiceBox<TipoUso> tipoUso = new ChoiceBox<>();
    private final Button guardar = new Button();
    private final Button eliminar = new Button("Eliminar seleccionado");
    private boolean editando;

    public VistaInmuebles(ControladorInmuebles controlador) {
        this.controlador = controlador;
        configurarControles();
        cargarPropietarios();
        recargar();
    }

    public Parent construir() {
        BorderPane raiz = new BorderPane();
        raiz.getStyleClass().add("raiz");
        raiz.setTop(crearEncabezado());
        raiz.setCenter(crearTabla());
        raiz.setRight(crearFormulario());
        BorderPane.setMargin(raiz.getRight(), new Insets(0, 24, 24, 18));
        return raiz;
    }

    private void configurarControles() {
        estado.setItems(FXCollections.observableArrayList(EstadoInmueble.values()));
        estado.setValue(EstadoInmueble.DISPONIBLE);
        tipo.setItems(FXCollections.observableArrayList("APARTAMENTO", "CASA"));
        tipo.setValue("APARTAMENTO");
        tipo.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevo) -> {
            if (!java.util.Objects.equals(anterior, nuevo)) {
                if ("APARTAMENTO".equals(nuevo)) {
                    cantidadPisos.clear();
                    tieneSotano.setSelected(false);
                    areaSotano.clear();
                } else if ("CASA".equals(nuevo)) {
                    numeroPiso.clear();
                }
            }
            actualizarCamposTipo();
        });
        tipoUso.setItems(FXCollections.observableArrayList(TipoUso.values()));
        tipoUso.setValue(TipoUso.BODEGA);
        guardar.setOnAction(evento -> guardarInmueble());
        eliminar.setOnAction(evento -> eliminarSeleccionado());
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                cargarEnFormulario(seleccionado);
            }
        });
        tieneSotano.selectedProperty().addListener((obs, anterior, seleccionado) -> {
            areaSotano.setDisable(!seleccionado);
            tipoUso.setDisable(!seleccionado);
        });
        actualizarCamposTipo();
    }

    private Parent crearEncabezado() {
        Label titulo = new Label("Gestión de inmuebles");
        titulo.getStyleClass().add("titulo");
        Label resumen = new Label("Inventario y datos de propiedad");
        resumen.getStyleClass().add("subtitulo");
        Button recargar = new Button("↻  Recargar");
        recargar.setOnAction(evento -> recargar());
        VBox titulos = new VBox(4, titulo, resumen);
        HBox encabezado = new HBox(titulos, recargar);
        encabezado.setSpacing(20);
        encabezado.setPadding(new Insets(24));
        HBox.setHgrow(titulos, Priority.ALWAYS);
        return encabezado;
    }

    private Parent crearTabla() {
        TableColumn<InmuebleDto, String> colChip = new TableColumn<>("CHIP");
        colChip.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().chip()));
        TableColumn<InmuebleDto, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().tipo()));
        TableColumn<InmuebleDto, String> colDireccion = new TableColumn<>("Dirección");
        colDireccion.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().direccion()));
        TableColumn<InmuebleDto, LocalDate> colFecha = new TableColumn<>("Fecha compra");
        colFecha.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().fechaCompra()));
        TableColumn<InmuebleDto, EstadoInmueble> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().estado()));
        TableColumn<InmuebleDto, String> colPropietario = new TableColumn<>("Propietario");
        colPropietario.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().nombrePropietario()));
        tabla.getColumns().setAll(colChip, colTipo, colDireccion, colFecha, colEstado, colPropietario);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPlaceholder(new Label("No hay inmuebles registrados"));
        VBox contenedor = new VBox(tabla);
        contenedor.setPadding(new Insets(0, 0, 24, 24));
        VBox.setVgrow(tabla, Priority.ALWAYS);
        return contenedor;
    }

    private Parent crearFormulario() {
        Label titulo = new Label("Datos del inmueble");
        titulo.getStyleClass().add("seccion");
        GridPane campos = new GridPane();
        campos.setHgap(10);
        campos.setVgap(10);
        int fila = 0;
        agregarCampo(campos, fila++, "CHIP", chip);
        agregarCampo(campos, fila++, "Fecha de compra", fechaCompra);
        agregarCampo(campos, fila++, "Dirección", direccion);
        agregarCampo(campos, fila++, "Estado", estado);
        agregarCampo(campos, fila++, "Tipo", tipo);
        agregarCampo(campos, fila++, "Propietario", propietario);
        GridPane.setColumnSpan(camposEspecificos, 2);
        campos.add(camposEspecificos, 0, fila);
        Button nuevo = new Button("Nuevo");
        nuevo.setOnAction(evento -> limpiarFormulario());
        HBox acciones = new HBox(10, guardar, nuevo, eliminar);
        VBox formulario = new VBox(14, titulo, campos, acciones);
        formulario.getStyleClass().add("formulario");
        formulario.setPadding(new Insets(18));
        formulario.setPrefWidth(340);
        return formulario;
    }

    private void actualizarCamposTipo() {
        camposEspecificos.getChildren().clear();
        if ("APARTAMENTO".equals(tipo.getValue())) {
            preparar(numeroPiso, "Número de piso");
            camposEspecificos.getChildren().add(numeroPiso);
        } else if ("CASA".equals(tipo.getValue())) {
            preparar(cantidadPisos, "Cantidad de pisos");
            preparar(areaSotano, "Área del sótano en m²");
            camposEspecificos.getChildren().setAll(cantidadPisos, tieneSotano, areaSotano, tipoUso);
            areaSotano.setDisable(!tieneSotano.isSelected());
            tipoUso.setDisable(!tieneSotano.isSelected());
        }
    }

    private static void preparar(TextField campo, String texto) {
        campo.setPromptText(texto);
        campo.setMaxWidth(Double.MAX_VALUE);
    }

    private static void agregarCampo(GridPane panel, int fila, String etiqueta, javafx.scene.Node control) {
        Label texto = new Label(etiqueta);
        panel.add(texto, 0, fila);
        panel.add(control, 1, fila);
        if (control instanceof javafx.scene.control.Control controlJavaFx) {
            controlJavaFx.setMaxWidth(Double.MAX_VALUE);
        }
    }

    private void cargarPropietarios() {
        propietario.setItems(FXCollections.observableArrayList(controlador.listarPropietarios()));
    }

    private void recargar() {
        try {
            tabla.setItems(FXCollections.observableArrayList(controlador.consultarInmuebles()));
            cargarPropietarios();
        } catch (RuntimeException e) {
            mostrarError("No fue posible consultar los inmuebles", e);
        }
    }

    private void guardarInmueble() {
        try {
            PropietarioDto seleccionado = propietario.getValue();
            if (seleccionado == null) {
                throw new IllegalArgumentException("Seleccione un propietario.");
            }
            Integer pisoApartamento = null;
            Integer pisosCasa = null;
            BigDecimal area = null;
            TipoUso uso = null;
            if ("APARTAMENTO".equals(tipo.getValue())) {
                pisoApartamento = entero(numeroPiso.getText(), "El número de piso");
            } else {
                pisosCasa = entero(cantidadPisos.getText(), "La cantidad de pisos");
                if (tieneSotano.isSelected()) {
                    try {
                        area = new BigDecimal(areaSotano.getText().trim());
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("El área del sótano debe ser un número válido.", e);
                    }
                    uso = tipoUso.getValue();
                }
            }
            InmuebleDto datos = new InmuebleDto(chip.getText(), fechaCompra.getValue(), direccion.getText(),
                    estado.getValue(), tipo.getValue(), seleccionado.identificacion(), seleccionado.nombre(),
                    pisoApartamento, pisosCasa, area, uso);
            if (editando) {
                controlador.actualizar(datos);
                mostrarInformacion("Inmueble actualizado correctamente.");
            } else {
                controlador.crear(datos);
                mostrarInformacion("Inmueble creado correctamente.");
            }
            recargar();
            limpiarFormulario();
        } catch (RuntimeException e) {
            mostrarError("No fue posible guardar el inmueble", e);
        }
    }

    private static Integer entero(String texto, String etiqueta) {
        try {
            return Integer.valueOf(texto.trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(etiqueta + " debe ser un entero válido.", e);
        }
    }

    private void eliminarSeleccionado() {
        InmuebleDto seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarInformacion("Seleccione un inmueble de la tabla.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el inmueble " + seleccionado.chip() + "?", ButtonType.CANCEL, ButtonType.OK);
        confirmacion.setHeaderText("Confirmar eliminación");
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                controlador.eliminar(seleccionado.chip());
                recargar();
                limpiarFormulario();
                mostrarInformacion("Inmueble eliminado correctamente.");
            } catch (RuntimeException e) {
                mostrarError("No fue posible eliminar el inmueble", e);
            }
        }
    }

    private void cargarEnFormulario(InmuebleDto inmueble) {
        editando = true;
        chip.setText(inmueble.chip());
        chip.setDisable(true);
        fechaCompra.setValue(inmueble.fechaCompra());
        direccion.setText(inmueble.direccion());
        estado.setValue(inmueble.estado());
        tipo.setValue(inmueble.tipo());
        propietario.getItems().stream()
                .filter(p -> p.identificacion().equals(inmueble.identificacionPropietario()))
                .findFirst().ifPresent(propietario::setValue);
        if ("APARTAMENTO".equals(inmueble.tipo())) {
            numeroPiso.setText(inmueble.numeroPiso().toString());
        } else {
            cantidadPisos.setText(inmueble.cantidadPisos().toString());
            tieneSotano.setSelected(inmueble.areaM2() != null);
            areaSotano.setText(inmueble.areaM2() == null ? "" : inmueble.areaM2().toPlainString());
            if (inmueble.tipoUso() != null) {
                tipoUso.setValue(inmueble.tipoUso());
            }
        }
        guardar.setText("Guardar cambios");
    }

    private void limpiarFormulario() {
        tabla.getSelectionModel().clearSelection();
        editando = false;
        chip.clear();
        chip.setDisable(false);
        fechaCompra.setValue(null);
        direccion.clear();
        estado.setValue(EstadoInmueble.DISPONIBLE);
        tipo.setValue("APARTAMENTO");
        numeroPiso.clear();
        cantidadPisos.clear();
        tieneSotano.setSelected(false);
        areaSotano.clear();
        tipoUso.setValue(TipoUso.BODEGA);
        propietario.setValue(null);
        guardar.setText("Crear inmueble");
    }

    private static void mostrarInformacion(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    private static void mostrarError(String encabezado, RuntimeException error) {
        String detalle = error.getMessage() == null ? "Revise los datos e intente de nuevo." : error.getMessage();
        Alert alerta = new Alert(Alert.AlertType.ERROR, detalle, ButtonType.OK);
        alerta.setHeaderText(encabezado);
        alerta.showAndWait();
    }
}