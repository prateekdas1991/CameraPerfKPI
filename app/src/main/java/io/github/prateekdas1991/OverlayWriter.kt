package io.github.prateekdas1991

class OverlayWriter {
    companion object {
        init {
            System.loadLibrary("overlay_writer") // matches your .so filename
        }
    }

    external fun writeOverlayGrayFrame(rgbaFrame: ByteArray)
}
