package org.tsb.bilibili.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 本app的版本信息, 方便软件更新
 *
 * {
 *     "apkSize": "2m",
 *     "apkUrl": "htttdscdc",
 *     "apkUrlPassword": "adak",
 *     "forceUpdate": "false",
 *     "isUpdate": "true",
 *     "md5": "hudecjdencjndj",
 *     "minVersion": "0",
 *     "newVersion": "6",
 *     "updateDescription": "--新增...."
 * }
 *
 */
@Data
@Accessors(chain = true)
public class AppVersion {
    private int newVersion;
    private int minVersion;
    private String versionName;
    private String apkUrl;
    private String apkUrlPassword;// 更新url的密码, 类似于百度云分享链接密码那种
    private boolean havePassword;
    private String updateDescription;
    private String isUpdate;
    private boolean forceUpdate;
    private String apkSize;
    private String md5;
}
