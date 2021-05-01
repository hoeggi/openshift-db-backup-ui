package io.github.hoeggi.openshiftdb.ui.composables.pathchooser

import androidx.compose.desktop.LocalAppWindow
import androidx.compose.desktop.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.FilePresent
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hoeggi.openshiftdb.ui.theme.Theme
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

private typealias FileFilter = (File) -> Boolean

class FileChooser(
    initialPath: String = System.getProperty("user.home"),
    private val showFiles: Boolean = true,
) {
    private val currentPath = mutableStateOf(File(initialPath))
    private val selectedPath = mutableStateOf<File?>(null)
    private val showHiddenFiles = mutableStateOf(false)

    fun show(onFileChosen: (File) -> Unit) = Window(
        title = "Choose a File",
        size = IntSize(600, 400),
        undecorated = false
    ) {

        val openedDir by currentPath
        val selectedDir by selectedPath
        val showHidden by showHiddenFiles

        val filter = mutableListOf<FileFilter>().apply {
            if (!showHidden) add(hideHiddenFilter)
            if (!showFiles) add(hideFilesFilter)
        }

        val list: List<File> = (
            openedDir.listFiles()
                ?.filter { file ->
                    filter.all { it(file) }
                }?.toMutableList() ?: mutableListOf()
            )
            .apply { sortWith(fileSorter) }

        val tree = mutableListOf<File>().apply {
            var current: File? = openedDir
            do {
                add(current!!)
                current = current.parentFile
            } while (current != null)
        }.reversed()

        val window = LocalAppWindow.current
        val fileChosen = { file: File ->
            onFileChosen(file)
            window.close()
        }
        Theme {
            Box(modifier = Modifier.fillMaxSize()) {
                Column {
                    LazyRow(state = LazyListState(tree.size, 0)) {
                        stickyHeader {
                            Row(
                                modifier = Modifier.background(MaterialTheme.colors.background)
                            ) {
                                Icon(
                                    image = Icons.Outlined.ArrowUpward,
                                    enabled = currentPath.value.parentFile != null
                                ) {
                                    setCurrent(currentPath.value.parentFile)
                                }
                                Spacer(modifier = Modifier.size(4.dp))
                                Icon(
                                    image = Icons.Outlined.Home,
                                ) {
                                    setCurrent(File(System.getProperty("user.home")))
                                }
                                Spacer(modifier = Modifier.size(4.dp))
                            }
                        }
                        items(tree) {
                            if (fileSystemView.isRoot(it)) {
                                Icon(image = Icons.Outlined.Computer) {
                                    setCurrent(it)
                                }
                            } else {
                                Text(
                                    it.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.body2,
                                    modifier = Modifier
                                        .border(Dp.Hairline, MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
                                        .padding(6.dp)
                                        .clickable {
                                            setCurrent(it)
                                        }
                                )
                            }
                        }
                    }
                    LazyVerticalGrid(
                        cells = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f, true).padding(4.dp)
                    ) {
                        items(list) {
                            val icon = when (it.isDirectory) {
                                true -> Icons.Outlined.Folder
                                else -> Icons.Outlined.FilePresent
                            }
                            val backgroundColor = if (it == selectedDir) {
                                MaterialTheme.colors.primary
                            } else Color.Transparent
                            Row(
                                modifier = Modifier.height(IntrinsicSize.Min)
                                    .background(backgroundColor)
                                    .combinedClickable(
                                        onDoubleClick = {
                                            if (it.isDirectory) {
                                                setCurrent(it)
                                            } else {
                                                fileChosen(it)
                                            }
                                        }
                                    ) {
                                        selectedPath.value = it
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    "",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(horizontal = 4.dp)
                                )
                                Text(
                                    it.name,
                                    style = MaterialTheme.typography.body2.copy(fontSize = 12.sp),
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showHiddenFiles.value = !showHidden }
                        ) {
                            Checkbox(
                                checked = showHidden,
                                onCheckedChange = {
                                    showHiddenFiles.value = !showHidden
                                }
                            )
                            Text(
                                "show hidden files",
                                modifier = Modifier.padding(horizontal = 4.dp),
                                style = MaterialTheme.typography.body2
                            )
                        }
                        Button(
                            onClick = {
                                fileChosen(selectedDir ?: openedDir)
                            },
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Icon(image: ImageVector, enabled: Boolean = true, onClick: () -> Unit) {
        Icon(
            imageVector = image,
            "",
            modifier = Modifier
                .size(28.dp)
                .border(Dp.Hairline, MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
                .padding(4.dp)
                .clickable(enabled) {
                    onClick()
                }
        )
    }

    private fun setCurrent(path: File?) {
        if (path != null) {
            currentPath.value = path
            selectedPath.value = null
        }
    }

    companion object {

        private const val showSystem = false
        operator fun invoke(
            initialPath: String? = null,
            showFiles: Boolean = true,
            onFileChosen: (File) -> Unit,
        ) {
            if (showSystem) {
                val chooser = JFileChooser(initialPath).apply {
                    fileSelectionMode = if (showFiles) JFileChooser.FILES_ONLY else JFileChooser.DIRECTORIES_ONLY
                }
                val returnVal = chooser.showOpenDialog(null)
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    onFileChosen(chooser.selectedFile)
                }
            } else {
                FileChooser(folderPath(initialPath), showFiles).show(onFileChosen)
            }
        }

        private fun folderPath(path: String?) = when {
            path.isNullOrEmpty() -> fileSystemView.defaultDirectory.absolutePath
            else -> {
                val file = File(path)
                when {
                    file.isDirectory -> file.absolutePath
                    else -> file.parent
                }
            }
        }

        private val fileSystemView = FileSystemView.getFileSystemView()
        private val hideFilesFilter: FileFilter = { file -> file.isDirectory }
        private val hideHiddenFilter: FileFilter = { file -> !file.isHidden }
        private val fileSorter = { first: File, second: File ->
            when {
                first.isDirectory && !second.isDirectory -> -1
                second.isDirectory && !first.isDirectory -> 1
                else -> first.compareTo(second)
            }
        }
    }
}
