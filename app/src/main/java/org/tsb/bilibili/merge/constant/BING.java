package org.tsb.bilibili.merge.constant;

/**
 * {
 *      "images": [
 *          {
 *              "startdate": "20200712",
 *              "fullstartdate": "202007121600",
 *              "enddate": "20200713",
 *              "url": "/th?id=OHR.SunnyRainforest_ZH-CN1412617420_1920x1080.jpg&rf=LaDigue_1920x1080.jpg&pid=hp",
 *              "urlbase": "/th?id=OHR.SunnyRainforest_ZH-CN1412617420",
 *              "copyright": "奥林匹克国家公园中的可可西里雨林，华盛顿州 (© Jorge Romano/Offset by Shutterstock)",
 *              "copyrightlink": "https://www.bing.com/search?q=%E5%8F%AF%E5%8F%AF%E8%A5%BF%E9%87%8C%E9%9B%A8%E6%9E%97&form=hpcapt&mkt=zh-cn",
 *              "title": "",
 *              "quiz": "/search?q=Bing+homepage+quiz&filters=WQOskey:%22HPQuiz_20200712_SunnyRainforest%22&FORM=HPQUIZ",
 *              "wp": true,
 *              "hsh": "e037a3dc2def08a891b41f208172e90a",
 *              "drk": 1,
 *              "top": 1,
 *              "bot": 1,
 *              "hs": []
 *          }
 *      ],
 *      "tooltips": {
 *          "loading": "正在加载...",
 *          "previous": "上一个图像",
 *          "next": "下一个图像",
 *          "walle": "此图片不能下载用作壁纸。",
 *          "walls": "下载今日美图。仅限用作桌面壁纸。"
 *      }
 * }
 */
public interface BING {
    /**
     * 目录的后缀都用`/`结尾
     */
    // 启动页面背景图片下载存储的路径
    String PIC_DOWNLOAD_PATH = ENV.SD_Path + "/Android/data/org.tsb.bilibili.merge/files/";
    // 必应 url
    String BASE_URL = "https://cn.bing.com";
    // bing壁纸接口url
    String IMAGE_API_URL = "https://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1&mkt=zh-CN";
    /*=========================================bing壁纸JSON-key================================================*/
    String IMAGES = "images";
    String IMAGE_URL = "url";
    String IMAGE_URLBASE = "urlbase";
}
