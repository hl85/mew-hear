package org.helo.mew.service

/**
 * 音频服务接口
 */
interface AudioService {
    /**
     * 播放音频
     * @param audioPath 音频文件路径或URL
     * @param onComplete 播放完成后的回调
     */
    fun playAudio(audioPath: String, onComplete: () -> Unit)
    
    /**
     * 停止音频播放
     */
    fun stopAudio()
    
    /**
     * 设置音量
     * @param volume 音量级别 (0.0f-1.0f)
     */
    fun setVolume(volume: Float)
    
    /**
     * 检查是否正在播放
     * @return 如果正在播放返回true，否则返回false
     */
    fun isPlaying(): Boolean
}
