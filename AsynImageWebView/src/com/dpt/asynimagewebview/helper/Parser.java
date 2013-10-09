package com.dpt.asynimagewebview.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Environment;
import android.webkit.WebView;
/**
 * 
 * @author pengtao.du@downjoy.com
 *
 */
public abstract class Parser {

	private WebView webView;
	public static String Js2JavaInterfaceName = "JsUseJava";
	public List<String> imgUrls = new ArrayList<String>();

	public Parser(String mUrl, WebView webView) {
		this.webView = webView;
	}
	
	public List<String> getImgUrls(){
		return imgUrls;
	}

	public void loadData(){
		String html=downloadHtml();
		Document doc = Jsoup.parse(html);
		imgUrls.clear();
		Elements es = doc.getElementsByTag("img");
		for (Element e : es) {
			String imgUrl = e.attr("src");
			imgUrls.add(imgUrl);
			String imgName;
			File file = new File(imgUrl);
			imgName = file.getName();
			if(imgName.endsWith(".gif")){
				e.remove();
			}else{
				File dir = new File(Environment.getExternalStorageDirectory() + "/test/"+imgName);
				String filePath = "file:///mnt/sdcard/test/" + imgName;
				e.attr("src","file:///android_asset/web_logo.png");
				e.attr("src_link", filePath);
				e.attr("ori_link",imgUrl);
				String str = "window." + Js2JavaInterfaceName + ".setImgSrc('"
						+ dir.getPath() + "')";
				e.attr("onclick", str);
			}
		}
		webView.loadDataWithBaseURL(null, doc.html(),"text/html", "utf-8", null);
	}
	
	public abstract String downloadHtml();
}
