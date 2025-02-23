import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() {
    application {
        val windowState = rememberWindowState(width = 800.dp, height = 600.dp)
        Window(
            onCloseRequest = {exitApplication()},
            state = windowState,
            title = "Video Recorder Test"
        ){
            val model = RecordingModel()
            RecordingScreen(model)
        }
    }
}