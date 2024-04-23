import com.github.sarxos.webcam.Webcam
import com.github.sarxos.webcam.WebcamResolution
import io.humble.video.*
import io.humble.video.Codec.findEncodingCodec
import io.humble.video.Codec.findEncodingCodecByName
import io.humble.video.awt.MediaPictureConverter
import io.humble.video.awt.MediaPictureConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.awt.Rectangle
import java.awt.image.BufferedImage


class RecordingModel() {

    fun startRecording(webcam: Webcam)  {
        val duration = 10; // how long in seconds
        val snaps = 20; // snaps per second
        val codecName = null; // null => guess from file name
        val formatName = null; // null => guess from file name
        val filename = "filename.mp4";

        recordScreen(webcam,filename, formatName, codecName, duration, snaps)
    }

    /**
     * Records the video
     * Taken from webcam-capture GitHub page:
     * https://github.com/sarxos/webcam-capture/blob/master/webcam-capture-examples/
     * webcam-capture-video-recording-humble/src/main/java/RecordAndEncodeVideo.java
     */
    private fun recordScreen(
        webcam: Webcam,
        filename: String,
        formatName: String?,
        codecName: String?,
        duration: Int,
        snapsPerSecond: Int
    ) {
        /**
         * Set up the AWT infrastructure to take screenshots of the desktop.
         */
        webcam.viewSize = WebcamResolution.VGA.size

        val size = Rectangle(webcam.viewSize)
        val framerate: Rational = Rational.make(1, snapsPerSecond)

        /** First we create a muxer using the passed in filename and formatname if given.  */
        val muxer: Muxer = Muxer.make(filename, null, formatName)

        /**
         * Now, we need to decide what type of codec to use to encode video. Muxers have limited
         * sets of codecs they can use. We're going to pick the first one that works, or if the user
         * supplied a codec name, we're going to force-fit that in instead.
         */
        val format = muxer.format
        val codec: Codec = if (codecName != null) {
            findEncodingCodecByName(codecName)
        } else {
            findEncodingCodec(format.defaultVideoCodecId)
        }

        /**
         * Now that we know what codec, we need to create an encoder
         */
        val encoder: Encoder = Encoder.make(codec)

        /**
         * Video encoders need to know at a minimum: width height pixel format Some also need to
         * know frame-rate (older codecs that had a fixed rate at which video files could be written
         * needed this). There are many other options you can set on an encoder, but we're going to
         * keep it simpler here.
         */
        encoder.width = size.width
        encoder.height = size.height
        // We are going to use 420P as the format because that's what most video formats these days
        // use
        val pixelformat: PixelFormat.Type = PixelFormat.Type.PIX_FMT_YUV420P
        encoder.pixelFormat = pixelformat
        encoder.timeBase = framerate

        /**
         * An annoynace of some formats is that they need global (rather than per-stream) headers,
         * and in that case you have to tell the encoder. And since Encoders are decoupled from
         * Muxers, there is no easy way to know this beyond
         */
        encoder.setFlag(Coder.Flag.FLAG_GLOBAL_HEADER, true);


        /** Open the encoder.  */
        encoder.open(null, null)

        /** Add this stream to the muxer.  */
        muxer.addNewStream(encoder)

        /** And open the muxer for business.  */
        muxer.open(null, null)

        /**
         * Next, we need to make sure we have the right MediaPicture format objects to encode data
         * with. Java (and most on-screen graphics programs) use some variant of Red-Green-Blue
         * image encoding (a.k.a. RGB or BGR). Most video codecs use some variant of YCrCb
         * formatting. So we're going to have to convert. To do that, we'll introduce a
         * MediaPictureConverter object later. object.
         */
        var converter: MediaPictureConverter? = null
        val picture: MediaPicture = MediaPicture
            .make(
                encoder.width,
                encoder.height,
                pixelformat
            )
        picture.timeBase = framerate

        /**
         * Open webcam so we can capture video feed.
         */
        webcam.open()

        /**
         * Now begin our main loop of taking screen snaps. We're going to encode and then write out
         * any resulting packets.
         */
        val packet: MediaPacket = MediaPacket.make()
        println("Ongoing recording...")
        for (i in 0 until (duration / framerate.double).toInt()) {
            /**
             * Make the screen capture && convert image to TYPE_3BYTE_BGR
             */

            val image = webcam.image
            val frame: BufferedImage = convertToType(image, BufferedImage.TYPE_3BYTE_BGR)

            /**
             * This is LIKELY not in YUV420P format, so we're going to convert it using some handy
             * utilities.
             */
            if (converter == null) {
                converter = MediaPictureConverterFactory.createConverter(frame, picture.format)
            }
            converter?.toPicture(picture, frame, i.toLong())

            do {
                encoder.encode(packet, picture)
                if (packet.isComplete) {
                    muxer.write(packet, false)
                }
            } while (packet.isComplete)

            /** now we'll sleep until it's time to take the next snapshot.  */
            Thread.sleep((1000 * framerate.double).toLong())
        }

        /**
         * Encoders, like decoders, sometimes cache pictures, so it can do the right key-frame
         * optimizations. So, they need to be flushed as well. As with the decoders, the convention
         * is to pass in a null input until the output is not complete.
         */
        do {
            encoder.encode(packet, null)
            if (packet.isComplete) {
                muxer.write(packet, false)
            }
        } while (packet.isComplete)

        /**
         * Finally, let's clean up after ourselves.
         */
        webcam.close()
        muxer.close()

        println("Recording finished.")
    }

    /**
     * Convert a [BufferedImage] of any type, to [BufferedImage] of a specified type. If
     * the source image is the same type as the target type, then original image is returned,
     * otherwise new image of the correct type is created and the content of the source image is
     * copied into the new image.
     *
     * @param sourceImage the image to be converted
     * @param targetType the desired BufferedImage type
     * @return a BufferedImage of the specifed target type.
     * @see BufferedImage
     */
    private fun convertToType(sourceImage: BufferedImage, targetType: Int): BufferedImage {
        val image: BufferedImage

        // if the source image is already the target type, return the source image
        if (sourceImage.type == targetType) image = sourceImage
        else {
            image = BufferedImage(
                sourceImage.width,
                sourceImage.height, targetType
            )
            image.graphics.drawImage(sourceImage, 0, 0, null)
        }

        return image
    }


}