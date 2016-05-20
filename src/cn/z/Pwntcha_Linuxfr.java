package cn.z;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import javax.imageio.ImageIO;

import cn.z.util.FontGlyphs;
import cn.z.util.ImageUtil;

public class Pwntcha_Linuxfr {

	public static void main(String[] args) throws Exception {

		final String fontFile = "img/linuxfr/font.png";
		final HashMap<BufferedImage, FontGlyphs> fontMap = ImageUtil.loadFontFixed(fontFile,
				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
	BufferedImage newImg = new BufferedImage(100, 40, BufferedImage.TYPE_INT_RGB);
	for(int i=0;i<newImg.getHeight()-1;i++){
		for(int j=0;j<newImg.getWidth()-1;j++){
			newImg.setRGB(j, i, Color.WHITE.getRGB());
		}
	}
	
		for (int k = 0; k < 1; k++) {
			final StringBuilder result = new StringBuilder();
			final String picFile = String.format("img/linuxfr/linuxfr_%03d.png", k);
			BufferedImage img = ImageIO.read(new File(picFile));
			img = ImageUtil.filterThreshold(img, 150);
			int x, y;
			int r;
			int i, j, c;
			final int w = img.getWidth();
			final int h = img.getHeight();
			final int[] stats = new int[h];
			for (y = 0; y < h; y++) {
				int count = 0;
				for (x = 0; x < w; x++) {
					r = new Color(img.getRGB(x, y)).getRed();
					if (r == 0) {
						count++;
					}
				}
				stats[y] = count;
			}
			/*
			 * Find 7 consecutive lines that have at least 14 pixels; they're
			 * baseline candidates
			 */
			for (y = 0; y < h - 11; y++) {//29行
				int ycan = 1;
				System.out.println("第"+(y+1)+"行");
				for (j = 3; j < 10; j++) {
					int row=y + j;
					System.out.println("第"+(row+1)+"行"+"块数:"+stats[y + j]);
					if (stats[y + j] < 14) {//4 5 6 7 8 9 10 行 
						ycan = 0;
						y = y + j - 3;
						break;
					}
				}
				if (ycan == 0) {
					continue;
				}

				/*
				 * Find 7 consecutive cells that have at least 2 pixels on each
				 * line; they're base column candidates
				 */
				for (x = 0; x < w - 9 * 7 + 1; x++) {
					int goodx = 1;
					for (c = 0; c < 7 && goodx == 1; c++) {
						for (j = 3; j < 10; j++) {
							int count = 0;
							for (i = 0; i < 8; i++) {
								r = new Color(img.getRGB(x + c * 9 + i, y + j)).getRed();
								if (r == 0) {
									count++;
									if (count == 2) {
										break;
									}
								}
							}
							if (count < 2) {
								goodx = 0;
								break;
							}
						}
					}
					if (goodx == 0) {
						continue;
					}

					/*
					 * Now we have an (x,y) candidate - try to fit 7 characters
					 */
					for (c = 0; c < 1 && goodx == 1; c++) {
						int r2;
						int minerror = Integer.MAX_VALUE;
						for (final Entry<BufferedImage, FontGlyphs> entry : fontMap.entrySet()) {
							int error = 0, goodch = 1;
							final BufferedImage fontImg = entry.getKey();
							for (j = 0; j < 12 && goodch == 1; j++) {
								for (i = 0; i < 8; i++) {
									r = new Color(img.getRGB(x + c * 9 + i, y + j)).getRed();
									r2 = new Color(fontImg.getRGB(i, j)).getRed();
									newImg.setRGB( x + c * 9 + i,  y + j, img.getRGB(x + c * 9 + i, y + j));
									
									//img.setRGB(x + c * 9 + i, y + j, Color.BLACK.getRGB());
									/*
									 * Only die if font is black and image is
									 * white
									 */
									if (r > r2) {
										goodch = 0;
										break;
									} else if (r < r2) {
										error++;
									}
								}
							}
							
							if (goodch == 1 && error < minerror) {
								minerror = error;
								result.append(entry.getValue().c);
							}
						}
						if (minerror == Integer.MAX_VALUE) {
							goodx = 0;
						}
					}
					/* Wow, that was a good guess! Exit this loop */
					if (goodx == 1) {
						break;
					}
				}
			}
			ImageIO.write(newImg, "JPG", new File("result/" + "linux" + "/"  +""+k+".jpg"));
			ImageIO.write(img, "JPG", new File("result/" + "linux" + "/"  +"result"+k+".jpg"));
			System.out.println(result.toString());
		}
	}

}
