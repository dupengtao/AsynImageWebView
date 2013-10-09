package com.dpt.asynimagewebviewdeno;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.view.Menu;
import android.webkit.WebView;
import com.dpt.asynimagewebview.helper.*;

public class MainActivity extends Activity {

	private WebView mWebView;
	private Parser mParser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mWebView=(WebView)findViewById(R.id.webview);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// only a demo
		new Thread(){
			public void run() {
				mParser = new Parser(null, mWebView) {
					@Override
					public String downloadHtml() {
						String html="";
						new Thread(){
							@Override
							public void run() {
							}
							
						}.start();
						DefaultHttpClient httpClient = new DefaultHttpClient(); 
						HttpGet httpGet = new HttpGet("http://wcf.open.cnblogs.com/news/item/183201");
						try {
							 html = httpClient.execute(httpGet, new BasicResponseHandler());
						} catch (ClientProtocolException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						html=Html.fromHtml(html).toString();
						return html;
					}
				};
				new WebViewHelper(MainActivity.this).execute(mWebView, mParser);
			};
		}.start();
	}
}
