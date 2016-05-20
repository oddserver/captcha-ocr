package cn.z.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.security.spec.MGF1ParameterSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

import com.jhlabs.image.ScaleFilter;

import cn.z.svm.svm_predict;
import cn.z.svm.svm_train;
import cn.z.util.CommonUtil;

public class Ocr1 {

	private static String clazz = "Ocr1test";
	private static int whiteThreshold = 380;
	private static boolean useSvm = false;
	private static int noseLien=30;
	public static String getAllOcr(String file,Map<String, Integer> mapc,int k) throws Exception {
		final BufferedImage img = CommonUtil.removeBackgroud(file, whiteThreshold);
		final List<BufferedImage> listImg = splitImage(img,mapc);
		int i=0;
		for(BufferedImage list:listImg){
			ImageIO.write(list, "JPG", new File("result/" + clazz + "/" + k+"_"+i +".jpg"));
			i++;
		}
		
		final Map<BufferedImage, String> map = CommonUtil.loadTrainData("Ocr1test");
		String result = useSvm ? "svm_" : "";
		for (final BufferedImage bi : listImg) {
			result += getSingleCharOcr(bi, map,i);
		}
		ImageIO.write(img, "jpg", new File("result/" + clazz + "/" + k+"_"+result + ".jpg"));
		return result;
	}

	private static void deal2img(BufferedImage img) {
		final int width = img.getWidth();
		final int height = img.getHeight();
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				if (isWhite(img.getRGB(x, y), 1) == 0) {
					if(y>1&&x>1&&y<height-1&&x<width-1&&(isWhite(img.getRGB(x, y-1), 600) == 1)&&(isWhite(img.getRGB(x, y+1), 600) == 1)&&(isWhite(img.getRGB(x-1, y), 600) == 1)&&(isWhite(img.getRGB(x+1, y), 600) == 1)){
						img.setRGB(x, y, Color.WHITE.getRGB());
					}
				}
			}
		}
	}

	private static int isWhite(int colorInt, int whiteThreshold2) {
		final Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() > whiteThreshold2) {
			return 1;
		}
		return 0;
	}

	private static String getSingleCharOcr(BufferedImage img, Map<BufferedImage, String> map,int name) throws Exception {
		if (useSvm) {
			final String input = new File("img/" + "Ocr1test" + "/input.txt").getAbsolutePath();
			final String output = new File("result/" + "Ocr1test" + "/output.txt").getAbsolutePath();
			CommonUtil.imgToSvmInput(img, input, whiteThreshold);
			svm_predict.main(
					new String[] { input, new File("train/" + "Ocr1test" + "/data.txt.model").getAbsolutePath(), output });
			final List<String> predict = IOUtils.readLines(new FileInputStream(output));
			if (predict.size() > 0 && predict.get(0).length() > 0) {
				return predict.get(0).substring(0, 1);
			}
			return "#";
		}
	
		String result = "";
	final int width = img.getWidth();
	final int height = img.getHeight();
	int min = width * height;//最小不相当数
	
	for (final BufferedImage bi : map.keySet()) {
		int count = 0;
		final int widthmin = width < bi.getWidth() ? width : bi.getWidth();
		final int heightmin = height < bi.getHeight() ? height : bi.getHeight();
		Label1: for (int x = 0; x < widthmin; ++x) {
			for (int y = 0; y < heightmin; ++y) {
				if (CommonUtil.isWhite(img.getRGB(x, y), 600) != CommonUtil.isWhite(bi.getRGB(x, y),
						600)) {
					count++;
					if (count >= min) {
						break Label1;
					}
				}
				
			}
		}
		
		//System.out.println("第"+name+"张图 与 图片"+map.get(bi)+"不相等数count:"+count+"初始不相当数:"+min);
		if (count < min) {
			min = count;
			result = map.get(bi);
			if(count==0){
				return result;
			}
		}
	}
	return result;
		}

	private static List<BufferedImage> splitImage(BufferedImage img,Map<String, Integer> map) throws Exception {
		int k=0;
		final List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
		final int width = img.getWidth();
		final int height = img.getHeight();
		final List<Integer> weightlist = new ArrayList<Integer>();
		for (int x = 0; x < width; ++x) {
			int count = 0;
			for (int y = 0; y < height; ++y) {
				if (CommonUtil.isWhite(img.getRGB(x, y), whiteThreshold) == 0) {
					count++;
				}
			}
			weightlist.add(count);
		}
		
		for (int i = 0; i < weightlist.size(); i++) {
			int length = 0;
			while (i < weightlist.size() && weightlist.get(i) > 0) {
				i++;
				length++;
			}
			if(length>0){
				Integer ms = map.get(length+"");
				if(ms==null){
					 map.put(length+"", 1);
				}else{
					 map.put(length+"", map.get(length+"")+1);
				}
				k+=length;
			}
			if  (length > 3&&length<=12) {
				
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - length, 0, length, height), 600, 0));
			}
			else if (length > 12&&length<=22) {
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - length, 0, length/2, height), 600, 0));
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - length/2, 0, length/2, height), 600, 0));
			}
			else if (length > 22&&length<=33) {
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - length-1, 0, length/3, height), 600, 0));
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - (length/3)*2-1, 0, length/3, height), 600, 0));
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - length/3-1, 0, length/3, height), 600, 0));
			}
			else if (length>33&&length <= 43) {
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - length-1, 0, length/4, height), 600, 0));
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - (length/4)*3-1, 0, length/4, height), 600, 0));
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - (length/4)*2-1, 0, length/4, height), 600, 0));
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - (length/4)-1, 0, length/4, height), 600, 0));
			}
			else if (length>43&&length<53) {
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - length-1, 0, length/4, height), 600, 0));
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - (length/5)*4-1, 0, length/4, height), 600, 0));
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - (length/5)*3-1, 0, length/4, height), 600, 0));
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - (length/5)*2-1, 0, length/4, height), 600, 0));
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - (length/5)-1, 0, length/4, height), 600, 0));
			}
		}
		//System.out.print("count "+k+"\n");
		return subImgs;
	}
	private static List<BufferedImage> splitImage(BufferedImage img) throws Exception {
		int k=0;
		final List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
		final int width = img.getWidth();
		final int height = img.getHeight();
		final List<Integer> weightlist = new ArrayList<Integer>();
		for (int x = 0; x < width; ++x) {
			int count = 0;
			for (int y = 0; y < height; ++y) {
				if (CommonUtil.isWhite(img.getRGB(x, y), 600) == 0) {
					count++;
				}
			}
			weightlist.add(count);
		}
		for (int i = 0; i < weightlist.size(); i++) {
			int length = 0;
			while (i < weightlist.size() && weightlist.get(i) > 0) {
				i++;
				length++;
			}
			//System.out.print(length);
			if  (length > 0&&length<=30) {
				
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - length, 0, length, height), 600, 0));
			}
			else if (length > 40) {
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - length, 0, length/2, height), 600, 0));
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - length/2, 0, length/2, height), 600, 0));
			}
		}
		return subImgs;
	}

	public static String doujia(int k)throws Exception{
		final BufferedImage img = ImageIO.read(new File("img/" + clazz + "/"+k+".png"));
		doujiaRm(img);
		final List<BufferedImage> listImg = splitImage(img);
		final List<BufferedImage> comImg = new ArrayList<BufferedImage>();
		int i=0;
		for(BufferedImage list:listImg){
			final ScaleFilter sf = new ScaleFilter(10, 10);
			BufferedImage imgdest = new BufferedImage(10, 10, list.getType());
			imgdest = sf.filter(list, imgdest);
			comImg.add(imgdest);
			//ImageIO.write(imgdest, "PNG", new File("result/" + clazz + "/" +k+"_"+i +".PNG"));
			i++;
		}
		final Map<BufferedImage, String> mapt = CommonUtil.loadTrainData("/svm/doujia");
		String result = useSvm ? "svm_" : "";
		for (final BufferedImage bi : comImg) {
			result += getSingleCharOcr(bi, mapt,i);
		}
		ImageIO.write(img, "PNG", new File("result/" + clazz + "/" +k+"_"+result+ ".png"));
		return result;
	}
	
	
	public static BufferedImage doujiaRm(BufferedImage img) throws Exception{
		Map co=new HashMap<String, String>();
		Map<Object,ArrayList<HashMap<String, String>>> map = new HashMap<Object,ArrayList<HashMap<String, String>>>();
		
		final int width = img.getWidth();
		final int height = img.getHeight();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int color=img.getRGB(x, y);
				ArrayList<HashMap<String, String>>am=map.get(color);
				if(am==null){
					am=new ArrayList<HashMap<String, String>>();
				}
				HashMap<String, String> flag = new HashMap<String,String>();
				flag.put(x+"", y+"");
				am.add(flag);
				map.put(color, am);
				
			}
		}
		for(Entry<Object, ArrayList<HashMap<String, String>>> m:map.entrySet()){
			int a=0;
			int b=0;
			int min=width;
			int max=0;
			HashMap<String, String> s1=m.getValue().get(0);
			for( Entry<String, String> v1:s1.entrySet()){
				a=Integer.valueOf(v1.getKey());
			}
			ArrayList<HashMap<String, String>> mm = m.getValue();
			for(HashMap<String, String> vm:mm){
				for(Entry<String, String> minm:vm.entrySet()){
					int mn=Math.abs(width-Integer.valueOf(minm.getKey()));
					int mx=Math.abs(width-Integer.valueOf(minm.getKey()));
					if(mn<min){
						min=mn;
						b=Integer.valueOf(minm.getKey());
					}
					if(mx>max){
					max=mx;
					a=Integer.valueOf(minm.getKey());
					}
				}
			}
			if(Math.abs(b-a)>noseLien||b-a==0){
				continue;
			}
			//System.out.println("b="+b+"a="+a+" b-a="+(b-a));
			//System.out.println(m.getKey()+" :" +m.getValue().size());
			co.put(m.getKey(), +m.getValue().size());
		}
		
		List<Map.Entry<String, Integer>> infoIds =
			    new ArrayList<Map.Entry<String, Integer>>(co.entrySet());
		Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() {   
		    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {      
		        return (o2.getValue() - o1.getValue());
		    }
		});
		
		List<Map.Entry<String, Integer>> vccodEntries=infoIds.subList(0, 4);
		Map set=new HashMap<String, String>();
		for(Entry<String, Integer> v:vccodEntries){
			set.put(v.getKey(), v.getValue());
		}
		//System.out.println(vccodEntries);
		//System.out.println(map);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int color=img.getRGB(x, y);
				if(set.containsKey(color)){
					img.setRGB(x, y, Color.BLACK.getRGB());
				}else{
					img.setRGB(x, y, Color.WHITE.getRGB());
				}
			}
		}
		return img;
	}
	public static void main(String[] args) throws Exception {
		// ---step1 downloadImage
		String url = "https://ipcrs.pbccrc.org.cn/imgrc.do?0.208045296021239";
		// 下载图片
		int num=1;
		 CommonUtil.downloadImage(url, clazz);
		new File("img/" + clazz).mkdirs();
		new File("train/" + clazz).mkdirs();
		new File("result/" + clazz).mkdirs();
		// 先删除result/ocr目录，开始识别
		Map<String, Integer> map = new TreeMap<String, Integer>();
		for (int i = 0; i < num; ++i) {
			final String text = getAllOcr("img/" + clazz + "/" + i + ".jpg",map,i);
			System.out.println(i + ".jpg = " + text);
		}
		for(Entry<String, Integer> m:map.entrySet()){
			System.out.println(m.getKey()+" :"+m.getValue());
		}

		 //CommonUtil.scaleTraindata("doujia", 0);
		 /*svm_train train = new svm_train();
		 train.run(new String[] { new File("train/" + clazz +
		 "/data.txt").getAbsolutePath(),
		 new File("train/" + clazz + "/data.txt.model").getAbsolutePath() });*/
		/*for (int i = 0; i < num; ++i) {
			final String text = doujia(i);
			System.out.println(i + ".jpg = " + text);
		}*/
		
		
		

	}
	


}
