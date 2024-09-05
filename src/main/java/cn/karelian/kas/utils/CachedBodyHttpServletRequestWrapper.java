package cn.karelian.kas.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class CachedBodyHttpServletRequestWrapper extends HttpServletRequestWrapper {
	public final String body;

	public CachedBodyHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
		super(request);
		InputStream inputStream = request.getInputStream();
		byte[] contents = inputStream.readAllBytes();
		body = new String(contents, "utf-8");
	}

	@Override
	public BufferedReader getReader() throws UnsupportedEncodingException {
		return new BufferedReader(new InputStreamReader(this.getInputStream()));
	}

	@Override
	public ServletInputStream getInputStream() throws UnsupportedEncodingException {
		final ByteArrayInputStream byteArrayIns = new ByteArrayInputStream(body.getBytes("utf-8"));
		ServletInputStream servletIns = new ServletInputStream() {
			@Override
			public boolean isFinished() {
				return false;
			}

			@Override
			public boolean isReady() {
				return false;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
			}

			@Override
			public int read() {
				return byteArrayIns.read();
			}
		};
		return servletIns;
	}
}