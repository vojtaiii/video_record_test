
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.sarxos.webcam.Webcam
import com.github.sarxos.webcam.WebcamPanel
import com.github.sarxos.webcam.WebcamResolution
import javax.swing.JFrame


@Composable
fun RecordingScreen(model: RecordingModel){
    val cameras = remember { mutableStateOf(Webcam.getWebcams()) }
    val camera = remember { mutableStateOf(Webcam.getDefault()) }

    val textSize = 30f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly)
    {
        cameraChooser(cameras, camera, Modifier)
        Button(
            onClick = { model.startRecording(camera.value) },
            content = {
                Text("Record", fontSize = textSize.sp)
            }
        )
    }
}

@Composable
fun cameraChooser(
    cameras: State<List<Webcam>>,
    camera: MutableState<Webcam?>,
    modifier: Modifier,
) {
    Column(modifier) {
        Text(
            text = "Please Select a camera:",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(4.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            contentPadding = PaddingValues(4.dp)
        ) {
            items(cameras.value) { webcam ->
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { camera.value = webcam }
                ) {
                    Row() {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = webcam.name,
                            maxLines = 2,
                            modifier = modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

fun videoPanel(webcam: Webcam) {
    webcam.viewSize = WebcamResolution.VGA.size

    val panel = WebcamPanel(webcam)
    panel.isFPSDisplayed = true
    panel.isDisplayDebugInfo = false
    panel.isImageSizeDisplayed = true
    panel.isMirrored = true

    val window = JFrame("Test webcam panel")
    window.add(panel)
    window.isResizable = true
    window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    window.pack()
    window.isVisible = true
}
