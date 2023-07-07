/**
 * @history
 * date, author
 * 2022. 8. 24., CHO_JANG_HO
 */
package MavenChart.draw.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import MavenChart.common.filevo.ChartFileVO;
import ch.qos.logback.classic.Logger;

/**
 * 
 * @date 2022. 8. 24.
 * @author CHO_JANG_HO
 * @version 0.1
 * @history
 * 	 build, no, date, author, description
 * - build, no, 2022. 8. 24., CHO_JANG_HO, 설명
 */
public class ChartService {

	private Logger logger = (Logger) LoggerFactory.getLogger(ChartService.class);
	Map<String, String> dbParam = ChartFileVO.getInstance().getDBParam();
	Connection conn = null;
	
	/**
	 * get parameter 초기화
	 * @author CHO_JANG_HO
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public Map<String, String> doGetTimeSetting(String startDate, String endDate) {
		Map<String, String> rtnMap = new HashMap<String, String>();
		
		String startTime = "";
		String endTime = "";
		if(null == startDate && null == endDate) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -7);
			Date setStartDate = new Date(cal.getTimeInMillis());
			Date setEndDate = new Date();
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			startTime = sdf.format(setStartDate);
			endTime = sdf.format(setEndDate);
			
			if(logger.isDebugEnabled()) {
				logger.debug("time is null, time Setting... startTime : " + startTime + ", endTime : " + endTime);
			}
		} else {
			startTime = startDate;
			endTime = endDate;
		}
		rtnMap.put("startTime", startTime);
		rtnMap.put("endTime", endTime);
		
		return rtnMap;
	}
	
	/**
	 * DB 연결
	 * @author CHO_JANG_HO
	 */
	public void DBconnection() {
		try {
			Class.forName(dbParam.get("dbDriverName"));
			conn = DriverManager.getConnection(dbParam.get("dbURL"), dbParam.get("dbUser"), dbParam.get("dbPasswd"));
		} catch (ClassNotFoundException e) {
			logger.error("cannot find DB driver : " + e.getMessage());
		} catch (SQLException e) {
			logger.error("cannot connect to DB : " + e.getMessage());
		} 
	}
	
	/**
	 * 1. db를 연결한다.
	 * 2. 데이터를 가져오는 sql를 만든다.
	 * 3. preparedstatement로 DB에서 실행할 쿼리내용을 담는다.
	 * 4. rs에 ps로 가져온 결과값들을 담는다.
	 * 5. DB에서 가져온 데이터를 JSON으로 바꾼다.
	 * @author CHO_JANG_HO
	 * @return
	 */
	public String getChartData(String startDate, String endDate) {
		DBconnection();
		String rtnData = "";
		// data 가져옴
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "SELECT z_dt" + 
					 "	   , z_cpupct" + 
					 "	   , z_mempct" + 
					 "	   , z_fsavg" + 
					 "	FROM rt_collect_sms" + 
					 " WHERE 1=1" +
					 "   AND z_dt BETWEEN " + "'" + startDate + "'" + " AND " + "'" +  endDate  + "'";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			
			ResultSetMetaData rsmd = rs.getMetaData();
			
			/**
			 * rs.getMetaData().getColumnName(i)는 해당 컬럼의 이름을 가져오고
			 * rs.getObject(i)는 현재 가르키고 있는 row의 data 값들을 가져옴
			 * 			  	rs.getMetaData().getColumnName(1)				rs.getMetaData().getColumnName(2)		rs.getMetaData().getColumnName(3)		rs.getMetaData().getColumnName(4)
			 *            		z_dt         									z_cpupct           						z_mempct         						z_fsavg
			 *            	rs.getObject(1)									rs.getObject(2)							rs.getObject(3)							rs.getObject(4)
			 * ⬇rs.next()   		2022-08-25 15:09:55.000							9.33805									62.835827								20.22526741027832
			 * ⬇rs.next()		2022-08-25 15:04:55.000							9.377128								62.270134								20.22496795654297
			 * ⬇rs.next()		2022-08-25 14:59:55.000							9.4258375								62.844254								20.224632263183594
			 * ⬇rs.next()		2022-08-25 14:54:55.000							9.263577								63.043354								20.224334716796875
			 * ⬇rs.next()		2022-08-25 14:49:55.000							8.85691									63.153515								20.224023818969727
			 * ⬇rs.next()		2022-08-25 14:44:55.000							8.959607								63.335606								20.223712921142578
			 * ⬇rs.next()		2022-08-25 14:39:56.000							9.154152								63.18773								20.223419189453125
			 * ⬇rs.next()		2022-08-25 14:34:56.000							10.672002								63.179398								20.223121643066406
			 * ⬇rs.next()		2022-08-25 14:29:56.000							9.230905								63.16363								20.222824096679688
			 * ⬇rs.next()		2022-08-25 14:24:56.000							8.866515								63.373924								20.222518920898438
			 */
			while(rs.next()) {
				Map<String, Object> rowData = new HashMap<String, Object>();
				for(int i = 1; i <= rsmd.getColumnCount(); i++) {
					rowData.put(rsmd.getColumnName(i), rs.getObject(i));
				}
				data.add(rowData);
			}
			
			if(logger.isDebugEnabled()) {
				int dataSize = data.size();
				logger.debug("data count : " + dataSize);
				if(dataSize > 50) {
					String dataLog = "[";
					for(int i = 0; i <= 50; i++){
						dataLog += data.get(i) + ", ";
					}
					dataLog += "... ]";
					logger.debug("data : " + dataLog);
				} else {
					logger.debug("data : " + data.toString());
				}
			}
		} catch (SQLException e) {
			logger.error("cannot execute select query : " + e.getMessage());
		} finally {
			if(null != ps) {
				try {
					ps.close();
				} catch (SQLException e) {
					logger.error("prepareStatement error : " + e.getMessage());
				}
			}
			
			if(conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("connection error : " + e.getMessage());
				}
			}
		}
		
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
		rtnData = gson.toJson(data);
		
		return rtnData;
	}
}
