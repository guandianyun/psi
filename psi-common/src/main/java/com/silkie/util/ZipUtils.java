package com.silkie.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * zip压缩解压工具类，基于com.jcraft.jzlib
 * 
 * @author defier.lai 
 * 2012-7-12 下午05:39:17
 * @version 1.0
 */
public class ZipUtils {

	// 输入数据的最大长度
	private static final int MAXLENGTH = 102400;

	// 设置缓存大小
	private static final int BUFFERSIZE = 1024;

	/**
	 * GZip压缩数据
	 * 
	 * @param object
	 * @return
	 * @throws IOException
	 */
	public static byte[] gZip(byte[] bContent) throws IOException {

		byte[] data = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			GZIPOutputStream gOut = new GZIPOutputStream(out, bContent.length); // 压缩级别,缺省为1级
			DataOutputStream objOut = new DataOutputStream(gOut);
			objOut.write(bContent);
			objOut.flush();
			gOut.close();
			data = out.toByteArray();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		return data;
	}

	/**
	 * GZip解压数据
	 * 
	 * @param object
	 * @return
	 * @throws IOException
	 */
	public static byte[] unGZip(byte[] bContent) throws IOException {

		byte[] data = new byte[MAXLENGTH];
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(bContent);
			GZIPInputStream pIn = new GZIPInputStream(in);
			DataInputStream objIn = new DataInputStream(pIn);

			int len = 0;
			int count = 0;
			while ((count = objIn.read(data, len, len + BUFFERSIZE)) != -1) {
				len = len + count;
			}

			byte[] trueData = new byte[len];
			System.arraycopy(data, 0, trueData, 0, len);

			objIn.close();
			pIn.close();
			in.close();

			return trueData;

		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	/***
	 * 压缩Zip
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static byte[] zip(byte[] bContent) throws IOException {

		byte[] b = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ZipOutputStream zip = new ZipOutputStream(bos);
			ZipEntry entry = new ZipEntry("zip");
			entry.setSize(bContent.length);
			zip.putNextEntry(entry);
			zip.write(bContent);
			zip.closeEntry();
			zip.close();
			b = bos.toByteArray();
			bos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

	/***
	 * 解压Zip
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static byte[] unZip(byte[] bContent) throws IOException {
		byte[] b = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(bContent);
			ZipInputStream zip = new ZipInputStream(bis);
			while (zip.getNextEntry() != null) {
				byte[] buf = new byte[1024];
				int num = -1;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				while ((num = zip.read(buf, 0, buf.length)) != -1) {
					baos.write(buf, 0, num);
				}
				b = baos.toByteArray();
				baos.flush();
				baos.close();
			}
			zip.close();
			bis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}

}
