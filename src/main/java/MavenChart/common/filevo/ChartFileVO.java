/**
 * @history
 * date, author
 * 2022. 8. 24., CHO_JANG_HO
 */
package MavenChart.common.filevo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

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
public class ChartFileVO {

	private static ChartFileVO chartFileVO = null;
	private Logger logger = (Logger) LoggerFactory.getLogger(ChartFileVO.class);
	private File dbFile = null;
	
	/**
	 * singleton 객체로 생성하게끔 생성자를 막아둠
	 * 파일을 찾고 불러오는 객체는 한 곳에서 사용하기 위해 사용
	 */
	private ChartFileVO() {}
	
	/**
	 * ChartFileVO 객체가 하나만 사용하도록 구현
	 * @author CHO_JANG_HO
	 * @return
	 */
	public static ChartFileVO getInstance() {
		if(null == chartFileVO) {
			chartFileVO = new ChartFileVO();
		}
		
		return chartFileVO;
	}
	
	/**
	 * db.properties 파일을 찾기 위한 메소드
	 * DB서버에 연결할 서버 정보 파일 찾기
	 * @author CHO_JANG_HO
	 */
	public void fileSearch() {
		File projectDir = new File(System.getProperty("webapp.root"));
		if(logger.isDebugEnabled()) {
			logger.debug("webapp.root path set in listener " + System.getProperty("webapp.root"));
		}
		Iterator<File> files = FileUtils.iterateFiles(projectDir, null, true);
		
		boolean dbFileFlag = false;
		while(files.hasNext()) {
			File currentFile = files.next();
			if(currentFile.isFile()) {
				if(currentFile.getAbsoluteFile().getName().endsWith("db.properties")) {
					dbFile = currentFile;
					if(logger.isInfoEnabled()) {
						logger.info("Found the DB config file");
					}
					dbFileFlag = true;
					break;
				}
			}
		}
		
		if(!dbFileFlag) {
			logger.error("There is no db.properties configuration file. DB is not available.");
		}
	}
	
	/**
	 * DB 설정 정보 파일 읽고 파라미터로 저장
	 * @author CHO_JANG_HO
	 * @return
	 */
	public Map<String, String> getDBParam(){
		fileSearch();
		Map<String, String> dbParam = new HashMap<String, String>();
		try {
			FileReader dbFileReader = new FileReader(dbFile);
			Properties dbResource = new Properties();
			dbResource.load(dbFileReader);
			
			String dbDriverName = dbResource.getProperty("dbDriverClass");
			String dbURL = dbResource.getProperty("dbURL");
			String dbUser = dbResource.getProperty("dbUser");
			String dbPasswd = dbResource.getProperty("dbPasswd");
			
			dbParam.put("dbDriverName", dbDriverName);
			dbParam.put("dbURL", dbURL);
			dbParam.put("dbUser", dbUser);
			dbParam.put("dbPasswd", dbPasswd);
		} catch (FileNotFoundException e) {
			logger.error("db.properties file not found : " + e.getMessage());
		} catch (IOException e) {
			logger.error("cannot load db.properties file : " + e.getMessage());
		}
		
		return dbParam;
	}
}
