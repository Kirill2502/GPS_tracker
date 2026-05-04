# 🛰️ GPS Tracker Android App

Приложение для отслеживания маршрутов пользователя в реальном времени с визуализацией трека на карте.

---

## 🚀 Основной функционал

- 📍 Отслеживание геолокации в реальном времени
- 🗺 Отрисовка маршрута на карте (Polyline)
- 📌 Маркеры начала и конца маршрута
- 🎯 Центрирование камеры на пользователе
- ⏯ Управление трекингом (start / stop)
- 🎨 Настройка цвета трека
- 💾 Сохранение и отображение маршрутов

---

## 🧱 Архитектура проекта

Проект построен по принципам **Clean Architecture**:

data/
domain/
presentation/


### 📦 Data layer
- Работа с Room Database
- Реализация репозиториев
- Источники данных (LocationService, DB)
- Мапперы между слоями

### 🧠 Domain layer
- Бизнес-логика
- UseCases (StartTracking, StopTracking, GetTracks и др.)
- Абстракции репозиториев
- Чистые модели данных

### 🎨 Presentation layer
- UI (Fragments, Activity)
- ViewModel
- Map rendering (MapRender)
- Permissions handling
- Adapters

---

## 🗺 Работа с картой

Реализовано:
- Polyline для маршрута
- Marker для точек старта/финиша
- MyLocation overlay
- Управление камерой карты

---

## ⚙️ Технологии

- Kotlin
- Coroutines
- ViewModel + LiveData
- Room Database
- Hilt (DI)
- OSMDroid
- Android Location API

---

## 🧠 Архитектурные решения

В проекте реализовано:

- Разделение на слои (data/domain/presentation)
- Dependency Inversion (domain не зависит от data)
- UseCase-подход для бизнес-логики
- Изоляция работы с GPS в отдельном сервисе
- Централизованное управление картой через MapRender

---

## 📸 Скриншоты

<img width="408" height="881" alt="image" src="https://github.com/user-attachments/assets/897171ab-bae6-4420-99e3-601a1717c611" /><img width="406" height="862" alt="image" src="https://github.com/user-attachments/assets/09f2899a-0acf-47b4-9be3-44484381c185" />



---

## 📦 Установка

1.Клонировать репозиторий

2.Открыть в Android Studio

3.Запустить на эмуляторе или устройстве
