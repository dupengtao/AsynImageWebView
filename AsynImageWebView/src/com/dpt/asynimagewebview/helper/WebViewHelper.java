package com.dpt.asynimagewebview.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dpt.asynimagewebview.R;
import com.dpt.asynimagewebview.widget.TouchImageView;
/**
 * 
 * @author pengtao.du@downjoy.com
 *
 */
public class WebViewHelper {

	private WebView mWebView;
	private Context mContext;
	private String TAG = "WebViewHelper";
	public WebViewHelper(Context context) {
		mContext = context;
	}

	public void execute(WebView webView, final Parser parser) {
		mWebView = webView;
		setupWebView();
		parser.loadData();
		webView.setWebViewClient(new WebViewClient() {

			public void onPageFinished(WebView view, String url) {

				DownloadWebImgTask downloadTask = new DownloadWebImgTask();
				List<String> urlStrs = parser.getImgUrls();
				String urlStrArray[] = new String[urlStrs.size() + 1];
				urlStrs.toArray(urlStrArray);
				downloadTask.execute(urlStrArray);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return true;
			}
		});
	}

	@SuppressLint("JavascriptInterface")
	private void setupWebView() {
		mWebView.addJavascriptInterface(new Js2JavaInterface(),
				Parser.Js2JavaInterfaceName);
		mWebView.getSettings()
				.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setBlockNetworkImage(true);
	}

	public class Js2JavaInterface {
		public void setImgSrc(String imgSrc) {
			Log.e(TAG, "setImgSrc : " + imgSrc);
			Dialog dialog = new Dialog(mContext, R.style.Dialog_Fullscreen);
			TouchImageView touch = new TouchImageView(mContext);
			try {
				touch.setImageBitmap(BitmapFactory
						.decodeStream(new FileInputStream(imgSrc)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			touch.setMaxZoom(4f); 
			dialog.setContentView(touch);
			dialog.show();
		}
	}

	public class DownloadWebImgTask extends AsyncTask<String, String, Void> {

		public static final String TAG = "DownloadWebImgTask";

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);

			mWebView.loadUrl("javascript:(function(){"
					+ "var objs = document.getElementsByTagName(\"img\"); "
					+ "for(var i=0;i<objs.length;i++)  "
					+ "{"
					+ "    var imgSrc = objs[i].getAttribute(\"src_link\"); "
					+ "    var imgOriSrc = objs[i].getAttribute(\"ori_link\"); "
					+ " if(imgOriSrc == \"" + values[0] + "\"){ "
					+ "    objs[i].setAttribute(\"src\",imgSrc);}" + "}"
					+ "})()");
		}

		@Override
		protected void onPostExecute(Void result) {
			mWebView.loadUrl("javascript:(function(){"
					+ "var objs = document.getElementsByTagName(\"img\"); "
					+ "for(var i=0;i<objs.length;i++)  " + "{"
					+ "    var imgSrc = objs[i].getAttribute(\"src_link\"); "
					+ "    objs[i].setAttribute(\"src\",imgSrc);" + "}"
					+ "})()");
			super.onPostExecute(result);
		}

		@Override
		protected Void doInBackground(String... params) {
			URL url = null;
			InputStream inputStream = null;
			OutputStream outputStream = null;
			HttpURLConnection urlCon = null;
			if (params.length == 0)
				return null;
			File dir = new File(Environment.getExternalStorageDirectory()
					+ "/test/");
			if (!dir.exists()) {
				dir.mkdir();
			}

			for (String urlStr : params) {
				try {
					if (urlStr == null) {
						break;
					}
					File tempFile = new File(urlStr);
					int index = urlStr.lastIndexOf("/");
					String fileName = urlStr.substring(index + 1,
							urlStr.length());
					Log.i(TAG, "file name : " + fileName
							+ " , tempFile name : " + tempFile.getName());
					Log.i(TAG, " url : " + urlStr);
					File file = new File(
							Environment.getExternalStorageDirectory()
									+ "/test/" + fileName);
					if (file.exists()) {
						continue;
					}
					try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}

					url = new URL(urlStr);
					urlCon = (HttpURLConnection) url.openConnection();
					urlCon.setRequestMethod("GET");
					urlCon.setDoInput(true);
					urlCon.connect();

					inputStream = urlCon.getInputStream();
					outputStream = new FileOutputStream(file);
					byte buffer[] = new byte[1024];
					int bufferLength = 0;
					while ((bufferLength = inputStream.read(buffer)) > 0) {
						outputStream.write(buffer, 0, bufferLength);
					}
					outputStream.flush();
					publishProgress(urlStr);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {

					try {
						if (inputStream != null) {
							inputStream.close();
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					try {
						if (outputStream != null) {
							outputStream.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return null;
		}
	}
}
