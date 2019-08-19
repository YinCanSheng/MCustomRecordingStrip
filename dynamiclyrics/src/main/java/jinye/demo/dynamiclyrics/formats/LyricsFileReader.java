package jinye.demo.dynamiclyrics.formats;

import android.util.Base64;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import jinye.demo.dynamiclyrics.model.LyricsInfo;

/**
 * @Description: 歌词文件读取器
 * @Author: zhangliangming
 * @Date: 2017/12/25 16:08
 * @Version:
 */
public abstract class LyricsFileReader {
    /**
     * 默认编码
     */
    private Charset defaultCharset = Charset.forName("utf-8");

    /**
     * 读取歌词文件
     *
     * @param file
     * @return
     */
    public LyricsInfo readFile(File file) throws Exception {
        if (file != null) {
            return readInputStream(new FileInputStream(file));
        }
        return null;
    }

    /**
     * 读取歌词文本内容
     *
     * @param base64FileContentString base64位文件内容
     * @param saveLrcFile             要保存的歌词文件
     * @return
     */
    public LyricsInfo readLrcText(String base64FileContentString,
                                  File saveLrcFile) throws Exception {
        byte[] fileContent = Base64.decode(base64FileContentString, Base64.NO_WRAP);

        if (saveLrcFile != null) {
            // 生成歌词文件
            FileOutputStream os = new FileOutputStream(saveLrcFile);
            os.write(fileContent);
            os.close();

            os = null;
        }

        return readInputStream(new ByteArrayInputStream(fileContent));
    }

    /**
     * 读取歌词文本内容
     *
     * @param base64ByteArray base64内容数组
     * @param saveLrcFile
     * @return
     */
    public LyricsInfo readLrcText(byte[] base64ByteArray,
                                  File saveLrcFile) throws Exception {
        if (saveLrcFile != null) {
            // 生成歌词文件
            FileOutputStream os = new FileOutputStream(saveLrcFile);
            os.write(base64ByteArray);
            os.close();

            os = null;
        }

        return readInputStream(new ByteArrayInputStream(base64ByteArray));
    }

    /**
     * @param dynamicContent  动感歌词内容
     * @param lrcContent      lrc歌词内容
     * @param extraLrcContent 额外歌词内容（翻译歌词、音译歌词）
     * @param lyricsFilePath 歌词文件保存路径
     * @return
     */
    public abstract LyricsInfo readLrcText(String dynamicContent, String lrcContent, String extraLrcContent, String lyricsFilePath) throws Exception;


    /**
     * 读取歌词文件
     *
     * @param in
     * @return
     */
    public abstract LyricsInfo readInputStream(InputStream in) throws Exception;

    /**
     * 支持文件格式
     *
     * @param ext 文件后缀名
     * @return
     */
    public abstract boolean isFileSupported(String ext);

    /**
     * 获取支持的文件后缀名
     *
     * @return
     */
    public abstract String getSupportFileExt();

    public void setDefaultCharset(Charset charset) {
        defaultCharset = charset;
    }

    public Charset getDefaultCharset() {
        return defaultCharset;
    }
}
