package org.helo.mew.service

/**
 * 音频服务接口
 */
interface AudioService {
    /**
     * 播放音频
     * @param audioPath 音频路径（本地或远程URL）
     * @param onComplete 播放完成回调
     */
    fun playAudio(audioPath: String, onComplete: () -> Unit = {})
    
    /**
     * 停止当前播放
     */
    fun stopAudio()
    
    /**
     * 设置音量
     * @param volume 音量大小 (0.0 - 1.0)
     */
    fun setVolume(volume: Float)
    
    /**
     * 当前是否正在播放
     */
    fun isPlaying(): Boolean
}
