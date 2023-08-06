package com.bytechainx.psi.common.kit;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.CommonConfig;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.jfinal.log.Log;

/**
 * 	二维码生成工具
 *
 */
public class ZxingUtil {
	
	private final static Log LOG = Log.getLog(ZxingUtil.class);
	
	private static final String CHARSET = "UTF-8";  
    private static final String FORMAT_NAME = "JPG";  
    // 二维码尺寸  
    private static final int QRCODE_SIZE = 220;  
    // LOGO宽度  
    private static final int WIDTH = 60;  
    // LOGO高度  
    private static final int HEIGHT = 60; 
    
    /**
     * @param content 二维码内容
     * @param logoImgPath Logo
     * @param needCompress 是否压缩Logo
     * @return 返回二维码图片
     * @throws WriterException
     * @throws IOException
     * BufferedImage
     * TODO 创建二维码图片
     */
    private static BufferedImage createImage(String content, String logoImgPath, boolean needCompress) throws WriterException, IOException {
    	Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();  
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);  
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);  
        hints.put(EncodeHintType.MARGIN, 1);  
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, QRCODE_SIZE, QRCODE_SIZE, hints);  
        int width = bitMatrix.getWidth();  
        int height = bitMatrix.getHeight();  
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);  
        for (int x = 0; x < width; x++) {  
            for (int y = 0; y < height; y++) {  
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);  
            }  
        }  
        if (logoImgPath == null || "".equals(logoImgPath)) {  
            return image;  
        }  
        
        // 插入图片  
        ZxingUtil.insertImage(image, logoImgPath, needCompress);  
        return image;  
    }
    
    /**
     * @param source 二维码图片
     * @param logoImgPath Logo
     * @param needCompress 是否压缩Logo
     * @throws IOException
     * void
     * TODO 添加Logo
     */
    private static void insertImage(BufferedImage source, String logoImgPath, boolean needCompress) throws IOException{
    	File file = new File(logoImgPath);  
        if (!file.exists()) {  
            return;  
        }  
        
        Image src = ImageIO.read(new File(logoImgPath));  
        int width = src.getWidth(null);  
        int height = src.getHeight(null);  
        if (needCompress) { // 压缩LOGO  
            if (width > WIDTH) {  
                width = WIDTH;  
            }  
            
            if (height > HEIGHT) {  
                height = HEIGHT;  
            }  
            
            Image image = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);  
            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);  
            Graphics g = tag.getGraphics();  
            g.drawImage(image, 0, 0, null); // 绘制缩小后的图  
            g.dispose();  
            src = image;  
        }  
        
        // 插入LOGO  
        Graphics2D graph = source.createGraphics();  
        int x = (QRCODE_SIZE - width) / 2;  
        int y = (QRCODE_SIZE - height) / 2;  
        graph.drawImage(src, x, y, width, height, null);  
        Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);  
        graph.setStroke(new BasicStroke(3f));  
        graph.draw(shape);  
        graph.dispose();  
    }
    
    /**
     * @param content 二维码内容
     * @param logoImgPath Logo
     * @param destPath 二维码输出路径
     * @param needCompress 是否压缩Logo
     * @throws Exception
     * void
     * TODO 生成带Logo的二维码
     */
    public static void encode(String content, String logoImgPath, String destPath, boolean needCompress) {
    	try {
    		BufferedImage image = ZxingUtil.createImage(content, logoImgPath, needCompress);  
            if(StringUtils.isEmpty(destPath)){
    			return;
    		}
    		File file = new File(destPath);
    		if(file.isDirectory()){
    			return;
    		} else {
    			file.mkdirs();
    		}
            ImageIO.write(image, FORMAT_NAME, new File(destPath));  
		} catch (Exception e) {
			LOG.error("生成二维码异常", e);
		}
    }   
    
    /**
     * 生成二维码
     * @param content
     * @return
     * @throws Exception
     */
    public static String encode(String content) {
    	String filePathBase = "/qrcode/" + DateUtil.getYearMonthStr(new Date()) + "/" + RandomUtil.genRandomNum(8);
		File outDir = new File(CommonConfig.resourceUploadPath + filePathBase);
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		String filePath =  filePathBase + "/" + System.currentTimeMillis() + ".jpg";
		String absolutePath = CommonConfig.resourceUploadPath + filePath;
        ZxingUtil.encode(content, null, absolutePath, false);  
        return filePath;
    } 
    
    /**
     * @param content 二维码内容
     * @param destPath 二维码输出路径
     * @throws Exception
     * void
     * TODO 生成不带Logo的二维码
     */
    public static void encode(String content, String destPath) {  
        ZxingUtil.encode(content, null, destPath, false);  
    }  
    
    /**
     * @param content 二维码内容
     * @param logoImgPath Logo
     * @param output 输出流
     * @param needCompress 是否压缩Logo
     * @throws Exception
     * void
     * TODO 生成带Logo的二维码，并输出到指定的输出流
     */
    public static void encode(String content, String logoImgPath, OutputStream output, boolean needCompress) {  
    	try {
    		BufferedImage image = ZxingUtil.createImage(content, logoImgPath, needCompress);  
            ImageIO.write(image, FORMAT_NAME, output);  
		} catch (Exception e) {
			LOG.error("生成二维码异常", e);
		}
    }  
    
    /**
     * @param content 二维码内容
     * @param output 输出流
     * @throws Exception
     * void
     * TODO 生成不带Logo的二维码，并输出到指定的输出流
     */
    public static void encode(String content, OutputStream output) {  
        ZxingUtil.encode(content, null, output, false);  
    }  
    
    /**
     * @param file 二维码
     * @return 返回解析得到的二维码内容
     * @throws Exception
     * String
     * TODO 二维码解析
     */
    public static String decode(File file) {
    	try {
    		BufferedImage image;  
            image = ImageIO.read(file);  
            if (image == null) {  
                return null;  
            }  
            BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(image);  
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));  
            Result result;  
            Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();  
            hints.put(DecodeHintType.CHARACTER_SET, CHARSET);  
            result = new MultiFormatReader().decode(bitmap, hints);  
            String resultStr = result.getText();  
            return resultStr;  
		} catch (Exception e) {
			LOG.error("生成二维码异常", e);
			return null;
		}
    }  
    
    /**
     * @param path 二维码存储位置
     * @return 返回解析得到的二维码内容
     * @throws Exception
     * String
     * TODO 二维码解析
     */
    public static String decode(String path) {  
        return ZxingUtil.decode(new File(path));  
    } 
    
    public static void main(String[] args) {
    	String content = "http://www.baidu.com";// 内容
    	String logoImgPath = "";// logo 路径
    	String destPath = "D:\\ss.png"; //
		try {
			ZxingUtil.encode(content, logoImgPath, destPath, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
