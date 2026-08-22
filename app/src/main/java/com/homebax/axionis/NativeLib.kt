package com.homebax.axionis

class NativeLib {

    /**
     * A native method that is implemented by the 'axionis' native library,
     * which is packaged with this application.
     */
    external fun stringFromLlama(): String
    external fun stringFromWhisper(): String

    companion object {
        // Used to load the 'axionis' library on application startup.
        init {
            System.loadLibrary("axionis")
        }
    }
}
