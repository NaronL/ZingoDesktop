package ru.igrakov.services

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.igrakov.models.*
import java.io.File

/**
 * @author Andrey Igrakov
 */
object FileService {

    private val json = Json { prettyPrint = true }
    private val workspaceDir = File(System.getProperty("user.home"), ".zingo")
    private val settingsFile = File(workspaceDir, "settings.json")

    init {
        if (!workspaceDir.exists()) workspaceDir.mkdirs()
        if (!settingsFile.exists()) saveSettings(defaultSettings())
    }

    fun defaultSettings() = SettingsModel(
        folderPath = System.getProperty("user.home"),
        darkTheme = false,
        locale = "ru",
    )

    fun loadWorkspaces(): List<WorkspaceModel> {
        if (!workspaceDir.exists()) return emptyList()
        return workspaceDir.listFiles { file ->
            file.name.startsWith("workspace_") && file.extension == "json"
        }?.mapNotNull { file ->
            runCatching {
                json.decodeFromString<WorkspaceModel>(file.readText())
            }.getOrNull()
        } ?: emptyList()
    }

    fun saveWorkspace(workspace: WorkspaceModel) {
        File(workspaceDir, "workspace_${workspace.id}.json").writeText(json.encodeToString(workspace))
    }

    fun deleteWorkspace(workspaceId: String) {
        File(workspaceDir, "workspace_$workspaceId.json").takeIf { it.exists() }?.delete()
    }

    fun loadBoard(boardId: String): BoardModel? =
        File(workspaceDir, "board_$boardId.json").takeIf { it.exists() }
            ?.readText()?.let { json.decodeFromString(it) }

    fun saveBoard(board: BoardModel) {
        File(workspaceDir, "board_${board.id}.json").writeText(json.encodeToString(board))
    }

    fun saveSettings(settings: SettingsModel) {
        settingsFile.writeText(json.encodeToString(settings))
    }

    fun loadSettings(): SettingsModel? {
        return if (settingsFile.exists()) {
            runCatching {
                json.decodeFromString<SettingsModel>(settingsFile.readText())
            }.getOrNull()
        } else {
            defaultSettings().also { saveSettings(it) }
        }
    }

}
