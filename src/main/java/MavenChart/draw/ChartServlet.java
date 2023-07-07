/**
 * @history
 * date, author
 * 2022. 8. 22., CHO_JANG_HO
 */
package MavenChart.draw;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;

import MavenChart.draw.service.ChartService;
import ch.qos.logback.classic.Logger;

/**
 * 
 * @date 2022. 8. 22.
 * @author CHO_JANG_HO
 * @version 0.1
 * @history
 * 	 build, no, date, author, description
 * - build, no, 2022. 8. 22., CHO_JANG_HO, 설명
 */
public class ChartServlet extends HttpServlet{

	/**
	 * 
	 * @history
	 * 	 build, no, date, author, description
	 * - build, no, 2022. 8. 22., CHO_JANG_HO, 변경comment
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger = (Logger) LoggerFactory.getLogger(ChartServlet.class);
	
	/**
	 * 1.get 방식으로 화면을 호출
	 * 2.chart를 그리기 위한 초기값 설정(시작일자, 종료일자)
	 * 3.초기값으로 데이터를 가져옴
	 * 4.데이터를 화면으로 넘겨줌
	 * 5.jsp 파일을 불러 화면을 보여줌
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=utf-8");
		
		if(logger.isInfoEnabled()) {
			logger.info("requested client IP => " + request.getRemoteAddr());
			logger.info("requested client Method => " + request.getMethod());
			logger.info("requested client RequestURL => " + request.getRequestURL());
		}
		
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		if(logger.isDebugEnabled()) {
			logger.debug("get -> startDate : " + startDate + ", endDate : " + endDate);
		}
		
		ChartService cs = new ChartService();
		Map<String, String> timeMap = cs.doGetTimeSetting(startDate, endDate);
		String chartData = cs.getChartData(timeMap.get("startTime"), timeMap.get("endTime"));
		request.setAttribute("getStartValue", timeMap.get("startTime"));
		request.setAttribute("getEndValue", timeMap.get("endTime"));
		request.setAttribute("getData", chartData);
		
		 
		request.getRequestDispatcher("WEB-INF/jsp/chart.jsp").forward(request, response);
		if(logger.isInfoEnabled()) {
			logger.info("=======================================================");
			logger.info("=============== Go to HighChart screen ===============");
			logger.info("=======================================================");
		}
		
	}
	
	/**
	 * 1.doPost로 검색 데이터를 가져올 수 있도록 설정
	 * 2.jsp화면에서 데이터를 JSON타입으로 받을 수 있도록 설정
	 * 3.jsp화면에서 받은 시작일자, 종료일자 값을 받음
	 * 4.가져온 날짜 데이터로 data 검색
	 * 5.가져온 데이터를 jsp화면으로 넘겨줌
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		
		if(logger.isInfoEnabled()) {
			logger.info("requested client IP => " + request.getLocalAddr());
			logger.info("requested client Method => " + request.getMethod());
			logger.info("requested client RequestURL => " + request.getRequestURL());
		}
		
		PrintWriter pw = response.getWriter();
		
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		if(logger.isDebugEnabled()) {
			logger.debug("post -> startDate : " + startDate + ", endDate : " + endDate);
		}
		
		ChartService cs = new ChartService();
		String chartData = cs.getChartData(startDate, endDate);
		pw.write(chartData);
	}
}
