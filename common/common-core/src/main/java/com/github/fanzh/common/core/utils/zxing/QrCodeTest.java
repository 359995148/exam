package com.github.fanzh.common.core.utils.zxing;

public class QrCodeTest {
 
	public static void main(String[] args) throws Exception {
		// 存放在二维码中的内容
		String text = "我是";
		// 嵌入二维码的图片路径
		String imgPath = "D:/qrCode/dog.jpg";
		// 生成的二维码的路径及名称
		String destPath = "D:/qrCode/qrcode/jam.jpg";
		//生成二维码
		QRCodeUtils.encode(text, imgPath, destPath, true);
		// 解析二维码
		String str = QRCodeUtils.decode(destPath);
		// 打印出解析出的内容
		System.out.println(str);
 
	}
 
}