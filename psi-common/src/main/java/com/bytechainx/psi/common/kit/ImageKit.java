package com.bytechainx.psi.common.kit;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;


public class ImageKit {
	private static final String[] IMG_SUFFIX = new String[]{".gif", ".bmp", ".jpg", ".jpeg", ".png"};
	
	public static void pressImage(File pressImg, File targetImg, int x, int y) {
		try {
			Image src = ImageIO.read(targetImg);
			int wideth = src.getWidth(null);
			int height = src.getHeight(null);
			BufferedImage image = new BufferedImage(wideth, height,BufferedImage.TYPE_INT_RGB);
			Graphics g = image.createGraphics();
			g.drawImage(src, 0, 0, wideth, height, null);

			// 水印文件
			Image src_biao = ImageIO.read(pressImg);
			int wideth_biao = src_biao.getWidth(null);
			int height_biao = src_biao.getHeight(null);
			g.drawImage(src_biao, wideth - wideth_biao - x, height - height_biao - y, wideth_biao, height_biao, null);
			// /
			g.dispose();
			FileOutputStream out = new FileOutputStream(targetImg);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
			param.setQuality(1, true);
			encoder.encode(image);
			out.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	/**
	 * 把图片印刷到图片上
	 * @param pressImg -- 水印文件
	 * @param targetImg --  目标文件
	 * @param x
	 * @param y
	 */
	public final static void pressImage(String pressImg, String targetImg, int x, int y) {
		try {
			if (!targetImgExists(targetImg)) {
				return;
			}
			pressImage(new File(pressImg), new File(targetImg), x, y);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean targetImgExists(String targetImg) {
		if (StringUtils.isEmpty(targetImg)) {
			return false;
		}
		
		//验证用户上传的文件是否是图片格式
		String[] imgSuffix = IMG_SUFFIX;
		for (String suff : imgSuffix) {
			if (targetImg.endsWith(suff)) {
				return true;
			}
		}
		return false;
	}

	/** */
	/**
	 * 打印文字水印图片
	 * @param pressText --文字
	 * @param targetImg --  目标图片
	 * @param fontName --  字体名
	 * @param fontStyle -- 字体样式
	 * @param color -- 字体颜色
	 * @param fontSize -- 字体大小
	 * @param x -- 偏移量
	 * @param y
	 */

	public static void pressText(String pressText, File targetImg, String fontName, int fontStyle, Color color, int fontSize, int x,int y) {
		try {
			if (targetImg == null) {
				return;
			}
			if(StringUtils.isEmpty(fontName)) {
				fontName = "宋体";
			}
			File _file = targetImg;
			Image src = ImageIO.read(_file);
			int wideth = src.getWidth(null);
			int height = src.getHeight(null);
			BufferedImage image = new BufferedImage(wideth, height, BufferedImage.TYPE_INT_RGB);
			Graphics g = image.createGraphics();
			g.drawImage(src, 0, 0, wideth, height, null);
			g.setColor(color);
			g.setFont(new Font(fontName, fontStyle, fontSize));

			g.drawString(pressText, wideth - fontSize - x, height - fontSize / 2 - y);
			g.dispose();
			FileOutputStream out = new FileOutputStream(targetImg);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			encoder.encode(image);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** * 图片文件读取 * * @param srcImgPath * @return 
	 * @throws IOException */
	private static BufferedImage InputImage(String srcImgPath) throws IOException {
		BufferedImage srcImage = null;
		try {
			FileInputStream in = new FileInputStream(srcImgPath);
			srcImage = javax.imageio.ImageIO.read(in);
		} catch (IOException e) {
			System.out.println("读取图片文件出错！" + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return srcImage;
	}
	
	private static BufferedImage InputImage(InputStream image) throws IOException {
		BufferedImage srcImage = null;
		try {
			//FileInputStream in = new FileInputStream(srcImgPath);
			srcImage = javax.imageio.ImageIO.read(image);
		} catch (IOException e) {
			System.out.println("读取图片文件出错！" + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		return srcImage;
	}

	/**
	 * * 将图片按照指定的图片尺寸压缩 * * @param srcImgPath :源图片路径 * @param outImgPath *
	 * :输出的压缩图片的路径 * @param new_w * :压缩后的图片宽 * @param new_h * :压缩后的图片高
	 * @throws IOException 
	 */
	public static void compressImage(String srcImgPath, String outImgPath, int new_w, int new_h) throws IOException {
		BufferedImage src = InputImage(srcImgPath);
		disposeImage(src, outImgPath, new_w, new_h);
	}
	
	public static void compressImage(InputStream image, String outImgPath, int new_w, int new_h) throws IOException {
		BufferedImage src = InputImage(image);
		disposeImage(src, outImgPath, new_w, new_h);
	}
	
	/**
	 * * 指定长或者宽的最大值来压缩图片 * * @param srcImgPath * :源图片路径 * @param outImgPath *
	 * :输出的压缩图片的路径 * @param maxLength * :长或者宽的最大值
	 * @throws IOException 
	 */
	public static void compressImage(String srcImgPath, String outImgPath, int maxLength) throws IOException {
		// 得到图片
		BufferedImage src = InputImage(srcImgPath);
		if (null != src) {
			int old_w = src.getWidth();
			// 得到源图宽
			int old_h = src.getHeight();
			// 得到源图长
			int new_w = old_w;
			// 新图的宽
			int new_h = old_h;
			// 新图的长
			// 根据图片尺寸压缩比得到新图的尺寸
			if (old_w > maxLength) {
				// 图片要缩放的比例
				new_w = maxLength;
				new_h = (int) Math.round(old_h * ((float) maxLength / old_w));
			} else if (old_h > maxLength) {
				new_w = (int) Math.round(old_w * ((float) maxLength / old_h));
				new_h = maxLength;
			}
			disposeImage(src, outImgPath, new_w, new_h);
		}
	}
	
	
	
	public static void compressImage(InputStream srcImage, String outImgPath, int maxLength) throws IOException {
		// 得到图片
		BufferedImage src = InputImage(srcImage);
		if (null != src) {
			
			if(src.getWidth() <= maxLength){
				/*File file = new File(outImgPath);
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}// 输出到文件流
*/				//srcImage.reset();
				/*byte[] bts = new byte[srcImage.available()];
				srcImage.read(bts);
				OutputStream out1 = new FileOutputStream(file);
				out1.write(bts);
				out1.flush();
				out1.close();*/
				
				disposeImage(src, outImgPath, src.getWidth(), src.getHeight());
				
				return;
			}
			
			int old_w = src.getWidth();
			// 得到源图宽
			int old_h = src.getHeight();
			// 得到源图长
			int new_w = 0;
			// 新图的宽
			int new_h = 0;
			// 新图的长
			// 根据图片尺寸压缩比得到新图的尺寸
			//if (old_w > old_h) {
				// 图片要缩放的比例
				new_w = maxLength;
				new_h = (int) Math.round(old_h * ((float) maxLength / old_w));
			/*} else {
				new_w = (int) Math.round(old_w * ((float) maxLength / old_h));
				new_h = maxLength;
			}*/
			disposeImage(src, outImgPath, new_w, new_h);
		}
	}

	/** * 处理图片 * * @param src * @param outImgPath * @param new_w * @param new_h */
	private synchronized static void disposeImage(BufferedImage src,
			String outImgPath, int new_w, int new_h) {
		// 得到图片
		int old_w = src.getWidth();
		// 得到源图宽
		int old_h = src.getHeight();
		// 得到源图长
		BufferedImage newImg = null;
		Graphics2D g;
		// 判断输入图片的类型
		if(src.getType() == 6 || src.getType() == 13) { // png,gif
			newImg = new BufferedImage(new_w, new_h, BufferedImage.TYPE_4BYTE_ABGR);
			g = newImg.createGraphics();
			newImg = g.getDeviceConfiguration().createCompatibleImage(new_w, new_h, Transparency.TRANSLUCENT); // 解决透明png黑底的问题
			g = newImg.createGraphics();
			
		} else {
			newImg = new BufferedImage(new_w, new_h, BufferedImage.TYPE_INT_RGB);
			g = newImg.createGraphics();
		}
		g.drawImage(src, 0, 0, old_w, old_h, null);
		g.dispose();
		
		// 根据图片尺寸压缩比得到新图的尺寸
		newImg.getGraphics().drawImage(src.getScaledInstance(new_w, new_h, Image.SCALE_SMOOTH), 0, 0, null);
		// 调用方法输出图片文件
		OutImage(outImgPath, newImg);
	}

	/**
	 * * 将图片文件输出到指定的路径，并可设定压缩质量 * * @param outImgPath * @param newImg * @param
	 * per
	 */
	private static void OutImage(String outImgPath, BufferedImage newImg) {
		// 判断输出的文件夹路径是否存在，不存在则创建
		File file = new File(outImgPath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}// 输出到文件流
		try {
			ImageIO.write(newImg, outImgPath.substring(outImgPath.lastIndexOf(".") + 1), 
					new File(outImgPath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public static void main(String[] args) {
//		pressImage("C://Users//Administrator//Desktop//c1.png", "C://Users//Administrator//Desktop//idcardimg.png", 10,10);
		pressText("今融网",new File("c:/tagimg.jpg"),"",2,Color.RED,12,100,10);
	}

	
}
