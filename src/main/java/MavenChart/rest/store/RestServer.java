/**
 * @history
 * date, author
 * 2022. 8. 23., CHO_JANG_HO
 */
package MavenChart.rest.store;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;

import MavenChart.rest.store.service.RestService;
import ch.qos.logback.classic.Logger;

/**
 * 
 * @date 2022. 8. 23.
 * @author CHO_JANG_HO
 * @version 0.1
 * @history
 * 	 build, no, date, author, description
 * - build, no, 2022. 8. 23., CHO_JANG_HO, 설명
 */
public class RestServer extends HttpServlet{

	/**
	 * 
	 * @history
	 * 	 build, no, date, author, description
	 * - build, no, 2022. 8. 23., CHO_JANG_HO, 변경comment
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger = (Logger) LoggerFactory.getLogger(RestServer.class);

	/**
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		
		if(logger.isInfoEnabled()) {
			logger.info("requested client IP => " + request.getLocalAddr());
			logger.info("requested client Method => " + request.getMethod());
			logger.info("requested client RequestURL => " + request.getRequestURL());
		}
		if(logger.isDebugEnabled()) {
			logger.debug("receive data from Collector : " + request.getParameter("data"));
		}
	}
	
	/**
	 * Collector로 데이터 받아 DB insert 처리
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// data를 받을 때 한글깨짐 방지를 위해 인코딩 설정
		request.setCharacterEncoding("utf-8");
		// Collector로 응답 메시지 보낼 때 한글깨짐 방지를 위해 인코딩 설정
		response.setCharacterEncoding("utf-8");
		
		if(logger.isInfoEnabled()) {
			logger.info("requested client IP => " + request.getLocalAddr());
			logger.info("requested client Method => " + request.getMethod());
			logger.info("requested client RequestURL => " + request.getRequestURL());
		}
		StringBuffer sb = new StringBuffer();
		String line;
		
		/**
		 * Collector에서 보낸 데이터 수신 
		 */
		try {
			BufferedReader br = request.getReader();
			while((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch(IOException e) {
			logger.error("cannot receive data : " + e.getMessage());
		}
		
		/**
		 * 받은 데이터를 DB에 넣을 데이터 타입 변환
		 */
		RestService restService = new RestService();
		List<Map<String, Object>> restoreData = restService.dataRestoring(sb.toString());
		
		if(logger.isDebugEnabled()) {
			logger.debug("restoring : " + sb.toString() + " => " + restoreData.toString());
		}
		
		/**
		 * 1.data 유/무 확인
		 * 2.DB insert 처리
		 * 3.DB insert 처리 성공시 -> 성공 메시지를 보게하고
		 *   DB insert 처리 실패시 -> 실패 메시지를 보게한다
		 */
		if(restService.checkData(restoreData)) {
//		if(false) {
//			response.getWriter().append("\nrequest client IP => " + request.getRemoteAddr() + "\n");
			if(restService.insertData(restoreData) <= 0) {
//				response.getWriter().append("DB insert data failed by => ").append(restService.getInsertErrMsg());
//				response.getWriter().append("\nDB insert is canceled");
				response.getWriter().append(restService.getRestCode());
				logger.error("DB insert data failed by => " + restService.getInsertErrMsg());
			} else {
				response.getWriter().append(restService.getRestCode());
				if(logger.isInfoEnabled()) {
					logger.info("request client IP => " + request.getRemoteAddr());
					
				}
			}
		} else {
//			response.getWriter().append("\nrequest client IP => " + request.getRemoteAddr() + "\n");
			response.getWriter().append(restService.getRestCode());
//			response.getWriter().append("\nDB insert is canceled");
			logger.error("data restoring is failed by => " + restService.getRestCode());
		}
		
	}
}
