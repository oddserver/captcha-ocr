package cn.z;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
/**
 * 生成字体
 * @author zhangyuxue
 * @data 2016年4月22日
 * @version v1.0.0
 */
public class MakeFont {

	public static void main(String[] args) throws Exception {
		final int size = 12;
		final int style = Font.ROMAN_BASELINE;
		final String text = "0123456789abcdefghijklmnopqrstuvwxyz".replace("", " ").trim();
		final String fontName = "Georgia";

		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = img.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		final Font font = new Font(fontName, style, size);
		g2.setFont(font);
		final FontMetrics fm = g2.getFontMetrics();
		img = new BufferedImage(fm.stringWidth(text), fm.getHeight()+4, BufferedImage.TYPE_INT_RGB);
		g2.dispose();
		g2 = img.createGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, img.getWidth(), img.getHeight());
		g2.setColor(Color.BLACK);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setFont(font);
		g2.drawString(text, 0, size);
		g2.dispose();
		ImageIO.write(img, "PNG", new File("makefontGeorgia.png"));
	}

}
