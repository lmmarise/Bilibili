package org.tsb.bilibili.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SY on 2016/5/9.
 * 文件读写工具类
 * >==================================Modify-by-Tang_Arise-2020/7/13==============================<
 */
public class FileUtils {

    private Context context;
    public static String SDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();//sd卡路径
    public static String SDState = Environment.getExternalStorageState();//SD卡状态

    public FileUtils(Context context) {
        this.context = context;
    }


    /**
     * 文件存储到/data/data/<packagename>/files/默认目录下
     *
     * @param fileName
     * @param bytes
     * @return
     */
    public boolean write2CacheFile(String fileName, byte[] bytes) {
        FileOutputStream out = null;
        BufferedOutputStream bos = null;
        try {
            out = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            bos = new BufferedOutputStream(out);// 将指定的数组中的数据写入输出流
            bos.write(bytes);
            bos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return false;
    }

    /**
     * InputStream转为byte数组
     */
    public static byte[] readInputStream(InputStream inStream) {
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();// new byte[]不需要关闭
            //创建一个Buffer字符串
            byte[] buffer = new byte[1024];
            //每次读取的字符串长度，如果为-1，代表全部读取完毕
            int len = 0;
            //使用一个输入流从buffer里把数据读取出来
            while ((len = inStream.read(buffer)) != -1) {
                //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
                outStream.write(buffer, 0, len);
            }
            //关闭输入流
            inStream.close();
            //把outStream里的数据写入内存
            return outStream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据文件名在/data/data/<packagename>/files/目录下查找一个文件, 返回该文件的字节数组
     *
     * @param fileName
     * @return
     */
    public byte[] readFromCacheFile(String fileName) {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        byte[] bytes = null;
        try {
            fis = context.openFileInput(fileName);
            bis = new BufferedInputStream(fis);
            bytes = new byte[bis.available()];
            fis.read(bytes);
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return null;
    }

    /**
     * 向SD卡里写字节
     *
     * @param path 文件夹目录
     * @param file 文件名
     * @param data 写入的字节数组
     * @return
     */
    public static boolean writeBytes(String path, String file, byte[] data) {
        FileOutputStream fos = null;
        try {
            // 拥有足够的容量
            if (data.length < getSDFreeSize()) {
                createDirectoryIfNotExist(path);
                createFileIfNotExist(path + file);

                fos = new FileOutputStream(path + File.separator + file);
                fos.write(data);
                fos.flush();
                return true;
            }
        } catch (Exception e) {
            Log.e("writeBytes", e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }

    /**
     * 从SD卡里读取字节数组
     *
     * @param path     目录
     * @param fileName 文件名
     * @return 返回字节数组，文件不存在返回null
     */
    public static byte[] readBytes(String path, String fileName) {
        File file = new File(path + File.separator + fileName);
        if (!file.exists()) {
            return null;
        }
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 将一个字节流写入到SD卡文件
     *
     * @param path     目录路径
     * @param fileName 文件名
     * @param input    字节流
     * @return
     */

    public static Boolean write2SDFromInput(String path, String fileName, InputStream input) {
        File file = null;
        OutputStream output = null;
        try {
            int size = input.available();
            // 拥有足够的容量
            if (size < getSDFreeSize()) {
                createDirectoryIfNotExist(path);
                createFileIfNotExist(path + File.separator + fileName);
                file = new File(path + File.separator + fileName);
                output = new BufferedOutputStream(new FileOutputStream(file));
                byte buffer[] = new byte[1024];
                int temp;
                while ((temp = input.read(buffer)) != -1) {
                    output.write(buffer, 0, temp);
                }
                output.flush();
                return true;

            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 判断SD卡是否存在
     *
     * @ return
     */
    private static boolean SDCardisExist() {
        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }

    /**
     * 获取SD卡剩余容量大小（单位Byte）
     *
     * @return
     */
    public static long getSDFreeSize() {
        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //空闲的数据块的数量
        long freeBlocks = sf.getAvailableBlocks();
        //返回SD卡空闲大小
        return freeBlocks * blockSize;  //单位Byte
        //return (freeBlocks * blockSize)/1024;   //单位KB
//        return (freeBlocks * blockSize) / 1024 / 1024; //单位MB
    }

    /**
     * 获取SD卡总容量大小（单位Byte）
     *
     * @return
     */
    public static long getSDAllSize() {
        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //获取所有数据块数
        long allBlocks = sf.getBlockCount();
        //返回SD卡大小
        return allBlocks * blockSize; //单位Byte
        //return (allBlocks * blockSize)/1024; //单位KB
//        return (allBlocks * blockSize) / 1024 / 1024; //单位MB
    }

    /**
     * 如果目录不存在，就创建目录
     *
     * @param path 目录
     * @return
     */
    public static boolean createDirectoryIfNotExist(String path) {
        File file = new File(path);
        //如果文件夹不存在则创建
        if (!file.exists() && !file.isDirectory()) {
            return file.mkdirs();
        } else {
            Log.e("目录", "目录存在！");
            return false;
        }

    }

    /**
     * 如果文件不存在，就创建文件
     *
     * @param path 文件路径
     * @return
     */
    public static boolean createFileIfNotExist(String path) {
        File file = new File(path);
        try {
            if (!file.exists()) {
                return file.createNewFile();
            } else {
                Log.e("文件", "文件存在！");
                return false;
            }
        } catch (Exception e) {
            Log.e("error", e.getMessage());
            return false;
        }
    }

    /**
     * 文件的复制
     */
    public static void copyFile(String parentDir, String fileName, File originFile, CopyListener copyListener) {
        try {
            FileInputStream fileInputStream = new FileInputStream(originFile);
            copyFile(parentDir, fileName, fileInputStream, originFile.length(), copyListener);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件的复制
     */
    public static void copyFile(String parentDir, String fileName, InputStream inputStream, long totalLenth, CopyListener copyListener) {
        try {
            copyListener.startCopy();
            File newFile = new File(parentDir + File.separator + fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(newFile);
            byte[] data = new byte[2048];
            int len = 0;
            long currentLenght = 0;
            while ((len = inputStream.read(data)) != -1) {
                fileOutputStream.write(data, 0, len);
                currentLenght += len;
                copyListener.progress((int) (currentLenght * 100 / totalLenth));
            }
            copyListener.finish(newFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface CopyListener {
        void startCopy();

        void progress(int progress);

        void finish(File file);
    }

    /**
     * 创建文件
     *
     * @param path     文件所在目录的目录名，如/java/test/0.txt,要在当前目录下创建一个文件名为1.txt的文件，<br>
     *                 则path为/java/test，fileName为1.txt
     * @param fileName 文件名
     * @return 文件新建成功则返回true
     */
    public static boolean createFile(@NotNull String path, @NotNull String fileName) {
        File file = new File(path + File.separator + fileName);
        if (file.exists()) {
            LogUtils.e("新建文件失败：file.exist()=" + file.exists());
            return false;
        } else {
            try {
                boolean isCreated = file.createNewFile();
                return isCreated;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 删除单个文件
     *
     * @param path     文件所在路径名
     * @param fileName 文件名
     * @return 删除成功则返回true
     */
    public static boolean deleteFile(@NotNull String path, @NotNull String fileName) {
        File file = new File(path + File.separator + fileName);
        if (file.exists()) {
            boolean isDeleted = file.delete();
            return isDeleted;
        } else {
            return false;
        }
    }


    /**
     * 根据文件名获得文件的扩展名
     *
     * @param fileName 文件名
     * @return 文件扩展名（不带点）
     */
    public static String getFileSuffix(@NotNull String fileName) {
        int index = fileName.lastIndexOf(".");
        String suffix = fileName.substring(index + 1, fileName.length());
        return suffix;
    }

    /**
     * 重命名文件
     *
     * @param oldPath 旧文件的绝对路径
     * @param newPath 新文件的绝对路径
     * @return 文件重命名成功则返回true
     */
    public static boolean renameTo(@NotNull String oldPath, @NotNull String newPath) {
        if (oldPath.equals(newPath)) {
            LogUtils.w("文件重命名失败：新旧文件名绝对路径相同！");
            return false;
        }
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);

        boolean isSuccess = oldFile.renameTo(newFile);
        LogUtils.w("文件重命名是否成功：" + isSuccess);
        return isSuccess;
    }

    /**
     * 重命名文件
     *
     * @param oldFile 旧文件对象
     * @param newFile 新文件对象
     * @return 文件重命名成功则返回true
     */
    public static boolean renameTo(File oldFile, File newFile) {
        if (oldFile.equals(newFile)) {
            LogUtils.w("文件重命名失败：旧文件对象和新文件对象相同！");
            return false;
        }
        boolean isSuccess = oldFile.renameTo(newFile);
        LogUtils.w("文件重命名是否成功：" + isSuccess);
        return isSuccess;
    }

    /**
     * 重命名文件
     *
     * @param oldFile 旧文件对象，File类型
     * @param newName 新文件的文件名，String类型
     * @return 重命名成功则返回true
     */
    public static boolean renameTo(File oldFile, String newName) {
        File newFile = new File(oldFile.getParentFile() + File.separator + newName);
        boolean flag = oldFile.renameTo(newFile);
        return flag;
    }


    /**
     * 文件大小的格式化
     *
     * @param size 文件大小，单位为byte
     * @return 文件大小格式化后的文本
     */
    public static String formatSize(long size) {
        DecimalFormat df = new DecimalFormat("####.00");
        if (size < 1024) // 小于1KB
        {
            return size + "Byte";
        } else if (size < 1024 * 1024) // 小于1MB
        {
            float kSize = size / 1024f;
            return df.format(kSize) + "KB";
        } else if (size < 1024 * 1024 * 1024) // 小于1GB
        {
            float mSize = size / 1024f / 1024f;
            return df.format(mSize) + "MB";
        } else if (size < 1024L * 1024L * 1024L * 1024L) // 小于1TB
        {
            float gSize = size / 1024f / 1024f / 1024f;
            return df.format(gSize) + "GB";
        } else {
            return "size: error";
        }
    }


    /**
     * 获取某个路径下的文件列表
     *
     * @param path 文件路径
     * @return 文件列表File[] files
     */
    public static File[] getFileList(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                return files;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 获取某个目录下的文件列表
     *
     * @param directory 目录
     * @return 文件列表File[] files
     */
    public static File[] getFileList(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            return files;
        } else {
            return null;
        }
    }

    /**
     * 取得文件或文件夹大小
     */
    public static long getFileSize(File file) {
        long size = 0;
        if (!file.isDirectory()) { // 文件
            return file.length();
        }
        File files[] = file.listFiles(); // 文件夹（递归）
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                size = size + getFileSize(files[i]);
            } else {
                size = size + files[i].length();
            }
        }
        return size;
    }

    /**
     * 删除文件
     **/
    public void deleteFile(File f) {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; ++i) {
                    deleteFile(files[i]);
                }
            }
        }
        f.delete();
    }

    /**
     * 在指定目录中查找包含关键字的文件(或查找后缀名为XXX的文件)，返回包含指定关键字的文件路径.
     *
     * @param folder   指定某个目录下
     * @param keyword  关键字
     * @param accurate 是否精确查找
     * @return
     */
    public static List<File> searchFiles(File folder, final String keyword, final boolean accurate) {
        List<File> result = new ArrayList<File>();
        if (folder.isFile())
            result.add(folder);

        File[] subFolders = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                if (accurate) {
                    // 精确查找
                    return file.getName().toLowerCase().equals(keyword);
                }
                // 模糊查找
                return file.getName().toLowerCase().contains(keyword);
            }
        });

            /*查找后缀名
             * File[] subFolders = folder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (file.isDirectory()) {
                        return true;
                    }
                    if (file.getName().toLowerCase().endsWith(keyword)) {
                        return true;
                    }
                    return false;
                }
            });*/

        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isFile()) {
                    // 如果是文件则将文件添加到结果列表中
                    result.add(file);
                } else {
                    // 如果是文件夹，则递归调用本方法，然后把所有的文件加到结果列表中
                    result.addAll(searchFiles(file, keyword, accurate));
                }
            }
        }
        return result;
    }

    /**
     * 读取字符类型的文件到JVM
     *
     * @param filePath 文件绝对路径
     * @param charset  文件字符集, 不传默认位utf-8
     * @return 返回String对象
     * @throws IOException if an I/O error occurs.
     */
    public static String readFileAsString(@NotNull String filePath, @Nullable Charset charset)
            throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        FileInputStream inputStream = new FileInputStream(file);
        int length = inputStream.available();
        byte[] bytes = new byte[length];
        inputStream.read(bytes);
        inputStream.close();
        return new String(bytes, charset != null ? charset : StandardCharsets.UTF_8);
    }
}