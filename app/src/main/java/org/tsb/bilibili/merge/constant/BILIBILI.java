package org.tsb.bilibili.merge.constant;

public interface BILIBILI {
    // 官方默认路径       `/storage/emulated/0` + "..."
    String DEFAULT_DOWNLOAD_PATH = ENV.SD_Path + "/Android/data/tv.danmaku.bili/download";
    // 概念版的缓存路径
    String DEFAULT_DOWNLOAD_BLUE_PATH = ENV.SD_Path + "/Android/data/com.bilibili.app.blue/download";
    // 视频名
    String VIDEO_FILENAME = "video.m4s";
    // 音频名
    String AUDIO_FILENAME = "audio.m4s";
    // 视频弹幕文件名
    String BULLET_FILENAME = "danmaku.xml";
    // 视频合并完成后存储路径
    String VIDEO_MERGE_DIR = ENV.SD_Path + getVideoMergeRelativePath();
    // 缓存文件前缀
    String TEMP_FILE_PREFIX = "video_icon_temp_";
    // 文件大小单位  1为B、2为KB、3为MB、4为GB
    int B = 1;
    int KB = 2;
    int MB = 3;
    int GB = 4;
    /*=============================视频json信息都有的key==============================*/
    // 存储B站视频信息的JSON文件名
    String ENTRY_JSON_FILENAME = "entry.json";
    String ENTRY_JSON_KEY_TITLE = "title";
    String ENTRY_JSON_KEY_SOURCE = "source";
    // 该视频能否播放? 比如视频为下载完成, 不能播放
    String ENTRY_JSON_KEY_CAN_PLAY_IN_ADVANCE = "can_play_in_advance";
    /*=============================子视频json信息才有的key==============================*/
    // 子视频信息 key
    String ENTRY_JSON_KEY_EP = "ep";
    String ENTRY_JSON_KEY_INDEX_TITLE = "index_title";

    // 处理视频用的广播action
    String FFMPEG_ACTION = "org.tsb.bilibili.merge.FFMPEG_VIDEO";

    // 视频合并状态
    int FFMPEG_DONE = 1;    // 成功
    int FFMPEG_ERROR = 0;   // 失败

    /**
     * 获取视频合并路径相对于SD卡挂载目录的路径
     */
    static String getVideoMergeRelativePath() {
        // todo 待以后新增菜单栏修改保存路径时来重新修改本方法
        return "/0-bili-video";
    }
}
