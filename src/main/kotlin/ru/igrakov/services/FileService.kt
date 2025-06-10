package ru.igrakov.services

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.igrakov.models.*
import java.io.File

/**
 * Сервис для работы с файлами.
 * Отвечает за загрузку и сохранение рабочих пространств, досок и настроек приложения.
 * Хранит данные в папке ".zingo" в домашней директории пользователя.
 * Использует сериализацию в JSON для хранения моделей.
 * @author Andrey Igrakov
 */
object FileService {

    private val json = Json { prettyPrint = true } // Сериализатор JSON с красивым форматированием
    private val workspaceDir = File(System.getProperty("user.home"), ".zingo") // Папка для хранения всех файлов приложения
    private val settingsFile = File(workspaceDir, "settings.json") // Файл для хранения настроек приложения

    init {
        // Инициализация: создаёт папку и файл настроек, если они не существуют
        if (!workspaceDir.exists()) workspaceDir.mkdirs()
        if (!settingsFile.exists()) saveSettings(defaultSettings())
    }

    /**
     * Возвращает модель настроек по умолчанию.
     */
    fun defaultSettings() = SettingsModel(
        folderPath = System.getProperty("user.home"), // Путь к домашней папке пользователя
        darkTheme = false, // Светлая тема по умолчанию
        locale = "ru", // Русская локализация по умолчанию
    )

    /**
     * Загружает все рабочие пространства из файлов.
     * @return Список рабочих пространств или пустой список, если ничего не найдено.
     */
    fun loadWorkspaces(): List<WorkspaceModel> {
        if (!workspaceDir.exists()) return emptyList()
        return workspaceDir.listFiles { file ->
            file.name.startsWith("workspace_") && file.extension == "json" // Фильтруем только файлы рабочих пространств
        }?.mapNotNull { file ->
            runCatching {
                json.decodeFromString<WorkspaceModel>(file.readText()) // Пробуем десериализовать JSON в WorkspaceModel
            }.getOrNull() // Если не удалось — пропускаем файл
        } ?: emptyList()
    }

    /**
     * Сохраняет рабочее пространство в файл.
     * @param workspace Рабочее пространство для сохранения.
     */
    fun saveWorkspace(workspace: WorkspaceModel) {
        File(workspaceDir, "workspace_${workspace.id}.json")
            .writeText(json.encodeToString(workspace)) // Сериализация модели в JSON и запись в файл
    }

    /**
     * Удаляет рабочее пространство по ID.
     * @param workspaceId Идентификатор рабочего пространства.
     */
    fun deleteWorkspace(workspaceId: String) {
        File(workspaceDir, "workspace_$workspaceId.json")
            .takeIf { it.exists() }?.delete() // Удаляет файл, если он существует
    }

    /**
     * Загружает доску по ID.
     * @param boardId Идентификатор доски.
     * @return Модель доски или null, если файл не найден.
     */
    fun loadBoard(boardId: String): BoardModel? =
        File(workspaceDir, "board_$boardId.json").takeIf { it.exists() }
            ?.readText()?.let { json.decodeFromString(it) }

    /**
     * Сохраняет доску в файл.
     * @param board Доска для сохранения.
     */
    fun saveBoard(board: BoardModel) {
        File(workspaceDir, "board_${board.id}.json")
            .writeText(json.encodeToString(board))
    }

    /**
     * Сохраняет настройки приложения.
     * @param settings Модель настроек.
     */
    fun saveSettings(settings: SettingsModel) {
        settingsFile.writeText(json.encodeToString(settings))
    }

    /**
     * Загружает настройки приложения.
     * @return Настройки, если файл найден и успешно прочитан, иначе возвращает настройки по умолчанию.
     */
    fun loadSettings(): SettingsModel? {
        return if (settingsFile.exists()) {
            runCatching {
                json.decodeFromString<SettingsModel>(settingsFile.readText())
            }.getOrNull()
        } else {
            // Если файл не найден, создаём файл с настройками по умолчанию
            defaultSettings().also { saveSettings(it) }
        }
    }
}
