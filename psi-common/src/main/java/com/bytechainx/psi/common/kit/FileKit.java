package com.bytechainx.psi.common.kit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.jfinal.upload.UploadFile;

public final class FileKit {

	private static final Log LOG = Log.getLog(FileKit.class);
	
	public static final char[] N36_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
	
	public static String randomFileName() {
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		return String.format("%s%d", f.format(new Date()), new Random().nextInt(100));
	}

	public static String dateFileName() {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(new Date());
	}

	/**
	 * 获取最终重命名后的文件路径
	 * 
	 * @param uploadPath
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static String getFinalPath(String uploadPath, String fileName, String suffix) throws Exception {
		StringBuilder finalPath = new StringBuilder();
		finalPath.append(uploadPath).append(File.separator).append(fileName).append(suffix);
		return finalPath.toString();
	}

	public static String fileName(String fileNamePrefix) {
		return String.format("%s%s", fileNamePrefix, randomFileName());
	}

	public static String suffix(String oldFileName) {
		return oldFileName.substring(oldFileName.lastIndexOf("."));
	}

	/**
	 * 文件上传并返回最终上传的文件路径
	 * 
	 * @param file
	 * @param path
	 * @param name
	 * @return
	 * @return
	 * @throws Exception
	 */
	public static Object[] upload(File file, String fileNamePrefix, String uploadPath, String oldFileName)
			throws Exception {
		String finalPath = "";
		int availableSize = 0;
		String filename = fileName(fileNamePrefix);
		String suffix = suffix(oldFileName);
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			finalPath = getFinalPath(uploadPath, filename, suffix);
			inputStream = new FileInputStream(file);
			availableSize = inputStream.available();
			outputStream = new FileOutputStream(finalPath);
			byte[] buf = new byte[1024];
			int length = 0;
			while ((length = inputStream.read(buf)) != -1) {
				outputStream.write(buf, 0, length);
			}
		} catch (Exception e) {
			finalPath = "";
		} finally {
			outputStream.close();
			inputStream.close();
		}
		return new Object[] { finalPath, availableSize, filename, suffix };
	}

	public static void directory(String path) {
		if (StringUtils.isEmpty(path))
			return;
		File f = new File(path);
		if (!f.exists() || (!f.isDirectory())) {
			f.mkdir();
		}
	}
	
	/***
	 * 上传并压缩图片
	 * @param uploadFile
	 * @param width
	 * @param height
	 * @return
	 */
	public static Ret uploadImage(UploadFile uploadFile, String resourceUploadPath, int width, int height) {
		String thumbPath = ""; // 略缩图路径
		String filePath = ""; // 原文件路径
		try {
			File file = uploadFile.getFile();
			String oriName = uploadFile.getFileName();
			String ext = oriName.substring(oriName.indexOf("."), oriName.length());
			String dateFileName = dateFileName();
			String _filePath = resourceUploadPath + File.separator + getDiskImagePath(dateFileName);
			File outFile = new File(_filePath);
			if (!outFile.exists()) {
				outFile.mkdirs();
			}
			
			String newName = getRandFileName() + ext;
			File fileName = new File(_filePath + File.separator + newName);
			FileUtils.copyFile(file, fileName);
			
			thumbPath = getRandFileName() + ext;
			//压缩图片
			ImageKit.compressImage(file.getPath(), _filePath + File.separator + thumbPath , width);
			
			file.delete();
			
			thumbPath = String.format("/%s/%s", getDomainImagePath(dateFileName), thumbPath);
			filePath = String.format("/%s/%s", getDomainImagePath(dateFileName), newName);
		} catch (Exception e) {
			LOG.error("上传图片异常", e);
			return Ret.fail("上传图片异常");
		}
		return Ret.ok().set("filePath", filePath).set("thumbPath", thumbPath);
	}
	
	
	public static Ret uploadImage(UploadFile uploadFile, String resourceUploadPath) {
		return uploadImage(uploadFile, resourceUploadPath, 800, 0);
	}
	
	public static File getWriteFile(String resourceUploadPath) {
		String fileUrl = FileKit.getDiskImagePath(FileKit.dateFileName());
		String filePath = resourceUploadPath + File.separator +fileUrl;
		File outDir = new File(filePath);
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		return new File(filePath+File.separator+FileKit.getRandFileName()+".png");
	}
	
	/**
	 * 
	 * TODO 获取硬盘图片路径
	 * 
	 * @param date
	 * @return
	 */
	public static String getDiskImagePath(String date) {
		StringBuilder fileName = new StringBuilder("");
		for (String d : date.split("-")) {
			fileName.append(d).append(File.separator);
		}
		return fileName.toString().substring(0, fileName.length() - 1);
	}
	
	public static String getFilePath(String targetPath) {
		String fileUrl = FileKit.getDomainImagePath(FileKit.dateFileName());
		String filePath = targetPath + "/" +fileUrl;
		File outDir = new File(filePath);
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		return "/" + fileUrl + "/";
	}

	/**
	 * 
	 * TODO 获取域名图片路径
	 * 
	 * @param date
	 * @return
	 */
	public static String getDomainImagePath(String date) {
		StringBuilder fileName = new StringBuilder("");
		for (String d : date.split("-")) {
			fileName.append(d).append("/");
		}
		return fileName.toString().substring(0, fileName.length() - 1);
	}

	public static String getRandFileName() {
		String name = longToN36(System.currentTimeMillis());
		return RandomStringUtils.random(4, N36_CHARS) + name;
	}
	
	public static String longToN36(long l) {
		return longToNBuf(l, N36_CHARS).reverse().toString();
	}
	
	private static StringBuilder longToNBuf(long l, char[] chars) {
		int upgrade = chars.length;
		StringBuilder result = new StringBuilder();
		int last;
		while (l > 0) {
			last = (int) (l % upgrade);
			result.append(chars[last]);
			l /= upgrade;
		}
		return result;
	}
	
	

	public static Ret uploadFile(FileItem fileItem, String resourceUploadPath) {
		String thumbPath = ""; // 略缩图路径
		String filePath = ""; // 原文件路径
		String oriName = fileItem.getName();
		String ext = oriName.substring(oriName.indexOf("."), oriName.length());
		String dateFileName = dateFileName();
		String _filePath = resourceUploadPath + File.separator + getDiskImagePath(dateFileName);
		File outFile = new File(_filePath);
		if (!outFile.exists()) {
			outFile.mkdirs();
		}
		
		String newName = getRandFileName() + ext;
		thumbPath = getRandFileName() + ext;
		FileOutputStream target = null;
		try {
			InputStream in = fileItem.getInputStream();
			//FileUtils.copyFile(file, target);
			target = new FileOutputStream(outFile+ File.separator +newName);
			IOUtils.copy(in, target);
			target.flush();
			in.available(); //FileUtils.sizeOf(target);
			
			//压缩图片
			ImageKit.compressImage(outFile+ File.separator +newName, _filePath + File.separator + thumbPath , 800);
			
			thumbPath = String.format("/%s/%s", getDomainImagePath(dateFileName), thumbPath);
			filePath = String.format("/%s/%s", getDomainImagePath(dateFileName), newName);
			
		} catch (IOException e) {
			LOG.error("上传图片异常", e);
			return Ret.fail("上传图片异常");
			
		}finally{
			IOUtils.closeQuietly(target);
		}
		return Ret.ok().set("filePath", filePath).set("thumbPath", thumbPath);
	}
	
	public static void main(String[] args) {
		String name = "/2014/10/12/2223223.jpg";
		name = name.replaceAll("/", "\\" + File.separator);
		System.out.println(name);
	}

}
