package com.escom.silentnull.video

/** Interface to handle video playback across different platforms. */
interface IVideoPlayer {
    /**
     * Plays a video from the assets folder.
     * @param path The filename of the video in the assets folder.
     * @param onFinished A callback to be executed when the video finishes or is skipped.
     */
    fun playVideo(path: String, onFinished: () -> Unit)
}
