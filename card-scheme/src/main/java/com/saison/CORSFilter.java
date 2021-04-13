package com.saison;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CORSFilter  implements Filter  {
	
	private static Logger logger = Logger.getLogger(CORSFilter.class.getName());
	SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
	
	public void init(FilterConfig filterConfig) throws ServletException {
		try {
			logger = CorsLogger.getInstance();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
//		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletRequest request = new RequestWrapper((HttpServletRequest) servletRequest);
		Exception ex = new Exception();
//		HttpServletResponse resp = (HttpServletResponse) servletResponse;
		ResponseWrapper resp = new ResponseWrapper((HttpServletResponse) servletResponse);
		int status = HttpServletResponse.SC_OK;

		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE, HEAD");
		resp.setHeader("Access-Control-Allow-Headers",
				"Access-Control-Allow-Headers, Origin,Accept, X-Requested-With, Content-Type, Accept, Access-Control-Request-Method, Access-Control-Request-Headers,Authorization,"
				+ " async,method,processData,mimeType,contentType,headers,crossDomain,processing,content,xhrFields,user-type");
		resp.setHeader("Access-Control-Expose-Headers",
				"Origin, Access-Control-Request-Method, Access-Control-Allow-Origin, Access-Control-Allow-Credentials");
		resp.setHeader("Access-Control-Max-Age", "4000");

		String ip = CORSHelper.getClientIpAddr(request);
		String log_param = "";
		if ("POST".equalsIgnoreCase(request.getMethod())) {
			String bodyOrParam = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
//			log_param = "] [REQUEST BODY:" + bodyOrParam;

		} else {
			Map<String, String> requestMap = CORSHelper.getTypesafeRequestMap(request);

			log_param = "] [REQUEST PARAMETERS:" + requestMap;
		}

		final StringBuilder logMessage = new StringBuilder("REST Request - ").append("[HTTP METHOD:")
				.append(request.getMethod()).append("] [PATH INFO:").append(request.getRequestURI()).append(log_param)
				.append("] [REMOTE ADDRESS:").append(ip);
		long before = System.currentTimeMillis();

		if (request.getMethod().equals("OPTIONS")) {
			resp.setStatus(HttpServletResponse.SC_OK);
			long after = System.currentTimeMillis();
			logMessage.append("] [RESPONSE:").append(resp.getStatus());
			logMessage.append("] [LOGGED AT:").append(sdf.format(after));
			logMessage.append("] [TIME TAKEN:").append(after - before + "ms]");
			logger.info(logMessage.toString());
			return;
		}
		try {
			chain.doFilter(request, resp);
		} catch (Exception e) {
			status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			ex = e;
		}
		long after = System.currentTimeMillis();
		logMessage.append("] [RESPONSE:").append(resp.getStatus() != 0 ? resp.getStatus() : status);
		logMessage.append("] [LOGGED AT:").append(sdf.format(after));
		logMessage.append("] [TIME TAKEN:").append(after - before + "ms]");
		logger.info(logMessage.toString());
		if (status == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
			throw new ServletException(ex.getMessage(), ex.getCause());

		}
	}



	public void destroy() {
	}

}
