/**
 * @history
 * date, author
 * 2022. 8. 23., CHO_JANG_HO
 */
package MavenChart.rest.store.service;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import MavenChart.common.filevo.ChartFileVO;
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
public class RestService {

	private Logger logger = (Logger) LoggerFactory.getLogger(RestService.class);
	private Connection conn = null;
	private String insertErrMsg = "";
	/**
	 * restCode 표
	 * 20000 : 정상처리
	 * 41000 : collect data 중 z_dt 컬럼 값 비어있음
	 * 42000 : collect data 중 z_cpupct 컬럼 값 비어있음
	 * 43000 : collect data 중 z_mempct 컬럼 값 비어있음
	 * 44000 : collect data 중 z_fsavg 컬럼 값 비어있음
	 * 51000 : prepareStatement에 데이터 담고 실행하는 과정에서 형변환 실패
	 * 52000 : prepareStatement 파라미터 셋팅 및 쿼리 실행 실패
	 * 53000 : 쿼리 실행 후 DB connection 끊기 실패
	 */
	private String restCode = "";
	
	/**
	 * collector에서 받은 data 확인
	 * 필수값(차트를 그리는 데 필요한 컬럼들) -> z_dt, z_cpupct, z_mempct, z_fsavg 값이 비어있는지 확인
	 * @author CHO_JANG_HO
	 * @param collectData
	 * @return
	 */
	public boolean checkData(List<Map<String, Object>> collectData) {
//		boolean nextExecute = false;
		
		int collectDataSize = collectData.size();
		
//		for(int i = 0; i < collectDataSize; i++) {
//			if("".equals(collectData.get(i).get("z_dt")) || null == collectData.get(i).get("z_dt")) {
////				restCode = "z_dt value is empty";
//				logger.error("z_dt value is empty");
//				restCode = "41000";
//				break;
//			} else if("".equals(collectData.get(i).get("z_cpupct")) || null == collectData.get(i).get("z_cpupct")) {				
//				restCode = "z_cpupct value is empty";
//				logger.error(restCode);
//				restCode = "40100";
//				break;
//			} else if("".equals(collectData.get(i).get("z_mempct")) || null == collectData.get(i).get("z_mempct")) {
//				restCode = "z_mempct value is empty";
//				logger.error(restCode);
//				restCode = "40010";
//				break;
//			} else if("".equals(collectData.get(i).get("z_fsavg")) || null == collectData.get(i).get("z_fsavg")) {
//				restCode = "z_fsavg value is empty";
//				logger.error(restCode);
//				restCode = "40001";
//				break;
//			} else {
//				restCode = "data checking completed";
//				nextExecute = true;
//			}
//		}
		boolean nextExecute = true;
		for(int i = 0; i < collectDataSize; i++) {
			if("".equals(collectData.get(i).get("z_dt")) || null == collectData.get(i).get("z_dt")) {
//				restCode = "z_dt value is empty";
				logger.error("z_dt value is empty");
				restCode = "41000";
				nextExecute = false;
				break;
			} else if("".equals(collectData.get(i).get("z_cpupct")) || null == collectData.get(i).get("z_cpupct")) {				
//				restCode = "z_cpupct value is empty";
				logger.error("z_cpupct value is empty");
				restCode = "42000";
				nextExecute = false;
				break;
			} else if("".equals(collectData.get(i).get("z_mempct")) || null == collectData.get(i).get("z_mempct")) {
//				restCode = "z_mempct value is empty";
				logger.error("z_mempct value is empty");
				restCode = "43000";
				nextExecute = false;
				break;
			} else if("".equals(collectData.get(i).get("z_fsavg")) || null == collectData.get(i).get("z_fsavg")) {
//				restCode = "z_fsavg value is empty";
				logger.error("z_fsavg value is empty");
				restCode = "44000";
				nextExecute = false;
				break;
			} 
		}
		
		if("".equalsIgnoreCase(restCode)) {
			if(logger.isInfoEnabled()) {
				logger.info("data checking completed");
			}
		}
			

		return nextExecute;
	}
	
	/**
	 * Collector에서 받은 데이터 체킹 후 관련 메시지 getter
	 * @author CHO_JANG_HO
	 * @return
	 */
	public String getRestCode() {
		return restCode;
	}
	
	/**
	 * Collector로 받은 JSON 데이터를 원래 데이터 타입으로 변환
	 * @author CHO_JANG_HO
	 * @param data
	 * @return
	 */
	public List<Map<String, Object>> dataRestoring(String data){
		List<Map<String, Object>> rtnData = null;
		Gson gson = new Gson();
		Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
		rtnData = gson.fromJson(data, type);
		return rtnData;
	}

	/**
	 * DB connection
	 * @author CHO_JANG_HO
	 */
	private void DBconnection() {
		Map<String, String> dbParam = ChartFileVO.getInstance().getDBParam();
		try {
			Class.forName(dbParam.get("dbDriverName"));
			conn = DriverManager.getConnection(dbParam.get("dbURL"), dbParam.get("dbUser"), dbParam.get("dbPasswd"));
		} catch (ClassNotFoundException cnfe) {
			logger.error("cannot find DB driver : " + cnfe.getMessage());
		} catch (SQLException sqle) {
			logger.error("cannot connect to DB : " + sqle.getMessage());
		} 
	}
	
	/**
	 * DB에 데이터 insert
	 * preparestatement에서 set하기 전 colname을 따옴 -> 오류가 생긴 column 명을 잡기 위해서
	 * @author CHO_JANG_HO
	 * @param data
	 * @return
	 */
	public int insertData(List<Map<String, Object>> data) {
		DBconnection();
		
		int insertCount = 0;
		PreparedStatement ps = null;
		String detectColNm = "";
		
		try {
			int dataSize = data.size();
			for(int i = 0; i < dataSize; i++) {
				String sql = "INSERT INTO rt_collect_sms(z_dt, z_myip, z_myhost, z_cpupct, z_mempct, z_fsavg) values(?, ?, ?, ?, ?, ?)";
				
				ps = conn.prepareStatement(sql);
//				System.out.println("getFetchDirection : " + ps.getFetchDirection());
//				System.out.println("getFetchSize : " + ps.getFetchSize());
//				System.out.println("getLargeMaxRows : " + ps.getLargeMaxRows());
//				System.out.println("getLargeUpdateCount : " + ps.getLargeUpdateCount());
//				System.out.println("getMaxFieldSize : " + ps.getMaxFieldSize());
//				System.out.println("getMaxRows : " + ps.getMaxRows());
//				System.out.println("getQueryTimeout : " + ps.getQueryTimeout());
//				System.out.println("getResultSetConcurrency : " + ps.getResultSetConcurrency());
//				System.out.println("getResultSetHoldability : " + ps.getResultSetHoldability());
//				System.out.println("getResultSetType : " + ps.getResultSetType());
//				System.out.println("getUpdateCount : " + ps.getUpdateCount());
//				System.out.println("getConnection : " + ps.getConnection());
//				System.out.println("getParameterMetaData : " + ps.getParameterMetaData());
//				System.out.println("getResultSet : " + ps.getResultSet().getStatement());
//				System.out.println("getWarnings : " + ps.getWarnings());
//				ps.setString(1, data.get(i).get("z_dt").toString());
				detectColNm = "z_dt";
				ps.setTimestamp(1, Timestamp.valueOf(data.get(i).get("z_dt").toString()));
//				ps.setTimestamp(1, Timestamp.valueOf("aaaaaaaaaaaaaa"));
				detectColNm = "z_myip";
				ps.setString(2, data.get(i).get("z_myip").toString());
				detectColNm = "z_myhost";
				ps.setString(3, data.get(i).get("z_myhost").toString());
				detectColNm = "z_cpupct";
				ps.setDouble(4, Double.parseDouble(data.get(i).get("z_cpupct").toString()));
//				ps.setDouble(4, Double.parseDouble("a1s2s2s22222"));
				detectColNm = "z_mempct";
				ps.setDouble(5, Double.parseDouble(data.get(i).get("z_mempct").toString()));
//				ps.setDouble(5, Double.parseDouble("a1s2s2s22222"));
				detectColNm = "z_fsavg";
				ps.setDouble(6, Double.parseDouble(data.get(i).get("z_fsavg").toString()));
//				ps.setDouble(6, Double.parseDouble("a1s2s2s22222"));

				// insert parameter 값들을 보기 위해 추출
				if(logger.isDebugEnabled()) {
					String parameters = "";
					for(Map.Entry<String, Object> entry : data.get(i).entrySet()) {
						if("z_fsavg".equalsIgnoreCase(entry.getKey())) {
							parameters += entry.getKey() + "=" + entry.getValue();
						} else {
							parameters += entry.getKey() + "=" + entry.getValue() + ", ";
						}
					}
					logger.debug("insert parameters => " + parameters);
				}
				// insert query logging 발생
				insertCount += ps.executeUpdate();
//				ps.executeBatch();
				if(logger.isInfoEnabled()) {
					logger.info((i + 1) + " 번째 record inserted");
				}
			}
			if(logger.isDebugEnabled()) {
				logger.debug("inserted counts => " + insertCount);
			}
			restCode = "20000";
		} catch(IllegalArgumentException iae) {
			insertErrMsg = "column name : " +  detectColNm + ", detail error : " + iae.getMessage();
			restCode = "51000";
			if(null != ps && null != conn) {
				try {
					ps.close();
					conn.close();
				} catch (SQLException sqle) {
					insertErrMsg = sqle.getMessage();
				}
			}
			
		} catch (SQLException sqle) {
			restCode = "52000";
			insertErrMsg = sqle.getMessage();
		} finally {
			if(null != ps && null != conn) {
				try {
					ps.close();
					conn.close();
				} catch (SQLException sqle) {
					restCode = "53000";
					insertErrMsg = sqle.getMessage();
				}
			}
		}
		return insertCount;
	}
	
	/**
	 * DB Insert 관련 메시지 getter
	 * @author CHO_JANG_HO
	 * @return
	 */
	public String getInsertErrMsg() {
		return insertErrMsg;
	}
}
