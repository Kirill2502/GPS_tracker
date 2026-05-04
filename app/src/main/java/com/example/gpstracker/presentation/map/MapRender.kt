package com.example.gpstracker.presentation.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.preference.PreferenceManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapRender(val mapView: MapView) {

    private var polyline: Polyline? = null
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null
    private var mLocOverlay: MyLocationNewOverlay? = null
    private var firstStart = true
    private var isDetached = false

    // ------------------------------------------------------------
    // 🎨 ОТРИСОВКА ТРЕКА
    // ------------------------------------------------------------

    /**
     * Отрисовка трека с указанием цвета
     */
    @SuppressLint("UseKtx")
    fun drawTrack(points: List<GeoPoint>, color: String) {
        if (points.isEmpty()) return

        if (polyline == null) {
            polyline = Polyline()
        }

        if (!mapView.overlays.contains(polyline)) {
            mapView.overlays.add(polyline)
        }

        polyline?.apply {
            setPoints(points)
            outlinePaint.color = Color.parseColor(color)
        }

        mapView.invalidate()
    }

    /**
     * Отрисовка трека с автоматическим чтением цвета из настроек
     */
    fun drawTrack(context: Context, points: List<GeoPoint>) {
        val color = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("color_key", "#14347C")!!
        drawTrack(points, color)
    }

    /**
     * Полная подготовка отображения маршрута (центрирование, маркеры, линия)
     * @return GeoPoint - начальная точка (для кнопки центрирования)
     */
    fun showRoute(context: Context, points: List<GeoPoint>): GeoPoint? {
        if (points.isEmpty()) return null

        drawTrack(context, points)
        updateStart(points.first())
        updateEnd(points.last())
        moveCamera(points.first())

        return points.first()
    }

    /**
     * Очистка маршрута (линия и маркеры)
     */
    fun clearRoute() {
        clear()
    }

    // ------------------------------------------------------------
    // 📍 МАРКЕРЫ
    // ------------------------------------------------------------

    fun updateStart(point: GeoPoint) {
        if (startMarker == null) {
            startMarker = Marker(mapView).apply {
                title = "Начало маршрута"
                mapView.overlays.add(this)
            }
        }
        startMarker?.position = point
        mapView.invalidate()
    }

    fun updateEnd(point: GeoPoint) {
        if (endMarker == null) {
            endMarker = Marker(mapView).apply {
                title = "Конец маршрута"
                mapView.overlays.add(this)
            }
        }
        endMarker?.position = point
        mapView.invalidate()
    }

    // ------------------------------------------------------------
    // 🗺️ УПРАВЛЕНИЕ КАРТОЙ
    // ------------------------------------------------------------

    fun moveCamera(point: GeoPoint, zoom: Double = 18.0) {
        mapView.controller.zoomTo(zoom)
        mapView.controller.animateTo(point)
    }

    fun centerMyLocation() {
        mapView.controller.animateTo(mLocOverlay?.myLocation)
        mLocOverlay?.enableFollowLocation()
    }

    // ------------------------------------------------------------
    // 🧹 ОЧИСТКА
    // ------------------------------------------------------------

    fun clear() {
        polyline?.let {
            mapView.overlays.remove(it)
        }
        startMarker?.let {
            mapView.overlays.remove(it)
        }
        endMarker?.let {
            mapView.overlays.remove(it)
        }

        polyline = null
        startMarker = null
        endMarker = null

        mapView.invalidate()
    }

    fun reset() {
        firstStart = true
    }

    // ------------------------------------------------------------
    // 🚀 ИНИЦИАЛИЗАЦИЯ
    // ------------------------------------------------------------

    @SuppressLint("UseKtx")
    fun initOSM(context: Context) {
        if (mLocOverlay != null) return
        polyline?.outlinePaint?.color = Color.parseColor(
            PreferenceManager.getDefaultSharedPreferences(context)
                .getString("color_key", "#14347C")
        )
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(18.0)
        mapView.controller.setCenter(GeoPoint(55.751244, 37.618423))
        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)

        val mLocProvider = GpsMyLocationProvider(context)
        mLocOverlay = MyLocationNewOverlay(mLocProvider, mapView)
        mLocOverlay?.enableMyLocation()
        mLocOverlay?.enableFollowLocation()
        mLocOverlay?.runOnFirstFix {
            if (isDetached) return@runOnFirstFix

            mapView.post {
                if (isDetached) return@post

                mapView.overlays.clear()

                mLocOverlay?.let { mapView.overlays.add(it) }
                polyline?.let { mapView.overlays.add(it) }
            }
        }
    }

    // ------------------------------------------------------------
    // 📈 ОБНОВЛЕНИЕ ПОЛИЛИНИИ (для живого трекинга)
    // ------------------------------------------------------------

    fun addPoint(list: List<GeoPoint>) {
        if (list.isNotEmpty()) polyline?.addPoint(list[list.size - 1])
    }

    fun fillPolyline(list: List<GeoPoint>) {
        list.forEach {
            polyline?.addPoint(it)
        }
    }

    fun updatePolyline(list: List<GeoPoint>) {
        if (list.size > 1 && firstStart) {
            fillPolyline(list)
            firstStart = false
        } else {
            addPoint(list)
        }
    }
    fun detach() {
        isDetached = true
        // Очищаем все оверлеи с карты (гарантированно)
        mapView.overlays.clear()
        mapView.onDetach()
        // Обнуляем ссылки, чтобы не было утечек
        polyline = null
        startMarker = null
        endMarker = null
        mLocOverlay = null
    }
}