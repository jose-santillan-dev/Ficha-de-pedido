package com.planetaweb.fichapedido

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.planetaweb.fichapedido.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Formato que se muestra en los campos de fecha dentro del formulario
    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "DO"))

    // Formato usado para armar el nombre del archivo PDF
    private val fileDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("es", "DO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etFechaEmision.setOnClickListener { showDatePicker(binding.etFechaEmision) }
        binding.etFechaEntrega.setOnClickListener { showDatePicker(binding.etFechaEntrega) }

        binding.btnGenerarPdf.setOnClickListener { generarPdf() }
    }

    private fun showDatePicker(target: EditText) {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                target.setText(displayDateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    private fun generarPdf() {
        val nombre = binding.etNombre.text.toString().trim()
        val telefonos = binding.etTelefonos.text.toString().trim()
        val fechaEmision = binding.etFechaEmision.text.toString().trim()
        val localizado = binding.etLocalizado.text.toString().trim()
        val articulos = binding.etArticulos.text.toString().trim()
        val color = binding.etColor.text.toString().trim()
        val precio = binding.etPrecio.text.toString().trim()
        val fechaEntrega = binding.etFechaEntrega.text.toString().trim()
        val nota = binding.etNota.text.toString().trim()
        val vendedor = binding.etVendedor.text.toString().trim()

        if (nombre.isEmpty() || telefonos.isEmpty()) {
            Toast.makeText(this, "Por favor completa al menos Nombre y Teléfonos", Toast.LENGTH_SHORT).show()
            return
        }

        // Últimos 4 dígitos del teléfono (ignorando guiones, espacios, etc.)
        val soloDigitos = telefonos.filter { it.isDigit() }
        val ultimos4 = if (soloDigitos.length >= 4) soloDigitos.takeLast(4) else soloDigitos

        // Fecha de creación = fecha actual en que se genera el PDF
        val fechaCreacion = fileDateFormat.format(Calendar.getInstance().time)

        val nombreArchivo = "FICHA DE PEDIDO($fechaCreacion)-$ultimos4.pdf"

        try {
            val archivo = crearPdf(
                nombreArchivo = nombreArchivo,
                fechaEmision = fechaEmision,
                nombre = nombre,
                telefonos = telefonos,
                localizado = localizado,
                articulos = articulos,
                colorProducto = color,
                precio = precio,
                fechaEntrega = fechaEntrega,
                nota = nota,
                vendedor = vendedor
            )

            binding.tvResultado.text = "PDF guardado en:\n${archivo.absolutePath}"
            Toast.makeText(this, "PDF generado correctamente", Toast.LENGTH_LONG).show()
            abrirPdf(archivo)

        } catch (e: Exception) {
            Toast.makeText(this, "Error al generar el PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun crearPdf(
        nombreArchivo: String,
        fechaEmision: String,
        nombre: String,
        telefonos: String,
        localizado: String,
        articulos: String,
        colorProducto: String,
        precio: String,
        fechaEntrega: String,
        nota: String,
        vendedor: String
    ): File {
        val pdfDocument = PdfDocument()

        // Tamaño carta en puntos (612 x 792) a 72dpi
        val pageInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paintTitulo = Paint().apply {
            color = Color.parseColor("#2E5E4E")
            textSize = 22f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val paintSubtitulo = Paint().apply {
            color = Color.parseColor("#555555")
            textSize = 13f
            textAlign = Paint.Align.CENTER
        }
        val paintLabel = Paint().apply {
            color = Color.parseColor("#333333")
            textSize = 13f
            isFakeBoldText = true
        }
        val paintValor = Paint().apply {
            color = Color.BLACK
            textSize = 13f
        }
        val paintPie = Paint().apply {
            color = Color.parseColor("#777777")
            textSize = 11f
            textAlign = Paint.Align.CENTER
        }

        val centroX = 306f
        var y = 60f

        canvas.drawText("FICHA DE PEDIDO", centroX, y, paintTitulo)
        y += 22f
        canvas.drawText("Resumen de su pedido", centroX, y, paintSubtitulo)
        y += 40f

        val margenIzq = 60f
        val lineHeight = 34f

        fun dibujarCampo(etiqueta: String, valor: String) {
            canvas.drawText("$etiqueta:", margenIzq, y, paintLabel)
            y += 17f
            canvas.drawText(if (valor.isEmpty()) "-" else valor, margenIzq, y, paintValor)
            y += lineHeight
        }

        dibujarCampo("FECHA DE EMISIÓN", fechaEmision)
        dibujarCampo("NOMBRE", nombre)
        dibujarCampo("TELÉFONOS", telefonos)
        dibujarCampo("LOCALIZADO", localizado)
        dibujarCampo("ARTÍCULOS", articulos)
        dibujarCampo("COLOR", colorProducto)
        dibujarCampo("PRECIO", precio)
        dibujarCampo("FECHA DE ENTREGA", fechaEntrega)
        dibujarCampo("NOTA", nota)

        y += 10f
        canvas.drawText("VENDEDOR/A: $vendedor", margenIzq, y, paintLabel)

        canvas.drawText("Gracias por su compra — Ficha de pedido", centroX, 760f, paintPie)

        pdfDocument.finishPage(page)

        // Carpeta "pedidos" dentro del almacenamiento propio de la app
        val carpetaPedidos = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.parentFile, "pedidos")
        if (!carpetaPedidos.exists()) {
            carpetaPedidos.mkdirs()
        }

        val archivoPdf = File(carpetaPedidos, nombreArchivo)
        FileOutputStream(archivoPdf).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return archivoPdf
    }

    private fun abrirPdf(archivo: File) {
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "com.planetaweb.fichapedido.fileprovider",
            archivo
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "PDF guardado. No se encontró un visor de PDF instalado.", Toast.LENGTH_LONG).show()
        }
    }
}
