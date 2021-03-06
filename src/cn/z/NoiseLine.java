package cn.z;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import cn.z.util.CommonUtil;

public class NoiseLine {
	private final int M = 40;
	private final int N = 150;
	int a[][] = new int[this.M][this.N];
	int[] b = new int[this.M];
	int threshold = 500;
	int offset = 11;

	public NoiseLine() {
		int x = 0;
		int y = 0;
		try {
			final BufferedImage img = ImageIO.read(new File("D:/Dev_Doc/Project/Java/app/Captcha/img/Ocr1test/1.png"));
			this.renderImg = img;
			final int width = img.getWidth();
			final int height = img.getHeight();
			System.out.println(width + ":" + height);
			for (; x < height; ++x) {
				for (y = 0; y < width; ++y) {
					this.a[x][y] = CommonUtil.isBlack(img.getRGB(y, x), 700);
					//System.out.print(this.a[x][y] + " ");
				}
				//System.out.println(" ");
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private BufferedImage renderImg;

	public void genLine(int n) {
		if (n < this.offset) {
			this.b[n] = -1;
			this.genLine(n + 1);
		}
		if (n == this.M) {
			for (int i = 0; i < this.M; i++) {
				System.out.print(this.b[i] + " ");

			}
			System.out.println("");
		}
		if (n == this.offset) {
			for (int j = 0; j < this.N; j++) {
				if (this.a[this.offset][j] == 1) {
					this.b[this.offset] = j;
					this.genLine(n + 1);
				}
			}
		}
		if (n > 0 && n < this.M) {
			int hasMore = 0;
			if (this.b[n - 1] > 0 && this.b[n - 1] < this.N && this.a[n][this.b[n - 1]] == 1) {
				this.b[n] = this.b[n - 1];
				hasMore = 1;
				this.genLine(n + 1);
			} else {
				if (this.b[n - 1] > 0 && this.a[n][this.b[n - 1] - 1] == 1) {
					this.b[n] = this.b[n - 1] - 1;
					hasMore = 1;
					this.genLine(n + 1);
				}
				if (this.b[n - 1] < this.N - 1 && this.a[n][this.b[n - 1] + 1] == 1) {
					this.b[n] = this.b[n - 1] + 1;
					hasMore = 1;
					this.genLine(n + 1);
				}
			}
			if (n - this.offset > this.threshold && hasMore == 0) {
				for (int i = 0; i < n; i++) {
					if (this.b[i] > 0) {
						this.renderImg.setRGB(this.b[i], i, Color.RED.getRGB());
					}
				}
			}
		}

	}

	public void saveImg() {
		try {
			ImageIO.write(this.renderImg, "PNG", new File("img/0.png"));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		final NoiseLine line = new NoiseLine();
		line.genLine(0);
		line.saveImg();
		System.out.println("处理后图片在img/noiseRender.jpg");
	}

}
