package itreact.tutorup.server.servlets;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import itreact.tutorup.server.db.UserDto;
import itreact.tutorup.server.service.UserManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthFilter implements Filter {
	
	private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);
	
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            String token = getTokenFromRequest(httpRequest);
            log.debug("userToken={}", token);
            
            try {
                UserDto user = UserManager.getInstance().authenticate(getUserToken(token));
                log.debug("user for token={} :: {}", token, user);
                if (user != null) {
                    request.setAttribute("user", user);
                }else{
                	log.info("No user found for token: [{}]", token);
                }
            } catch (SQLException e) {
            	log.error("", e);
            }
            
        }
        filterChain.doFilter(request, response);
    }
    
    private static String getTokenFromRequest(HttpServletRequest httpRequest) throws UnsupportedEncodingException {
    	Map<String, List<String>> queryParams = getUrlParameters(httpRequest.getQueryString());
        log.debug("queryParams: {}", queryParams);
        String token = getFirstParameter("token", queryParams);
        if (token == null) {
        	token = httpRequest.getHeader("auth-token");
        	log.debug("Auth token from header: {}", token);
        }
        return token;
    }
    
    private String getUserToken(String token) {
    	if (token != null && token.length() > 0) {
            return token;
        } else {
        	return "anon-token";
        }
    }

    @Override
    public void destroy() {

    }

    private static String getFirstParameter(String name, Map<String, List<String>> params) {
        String result = null;
        List<String> values = params.get(name);
        if (values != null && values.size() > 0) {
            result = values.get(0);
        }
        return result;
    }

    private static Map<String, List<String>> getUrlParameters(String url)
            throws UnsupportedEncodingException {
        Map<String, List<String>> params = new HashMap<>();
        if (url != null && url.length() > 0) {
            String[] urlParts = url.split("\\?");
            String query = (urlParts.length > 1) ? urlParts[1] : url;
            for (String param : query.split("&")) {
                String pair[] = param.split("=");
                String key = URLDecoder.decode(pair[0], "UTF-8");
                String value = "";
                if (pair.length > 1) {
                    value = URLDecoder.decode(pair[1], "UTF-8");
                }
                List<String> values = params.get(key);
                if (values == null) {
                    values = new ArrayList<>();
                    params.put(key, values);
                }
                values.add(value);
            }
        }
        return params;
    }
}
