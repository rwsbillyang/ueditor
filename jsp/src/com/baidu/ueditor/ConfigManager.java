package com.baidu.ueditor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ueditor.define.ActionMap;
//import com.config.Config;


/**
 * 配置管理器
 * @author hancong03@baidu.com
 *
 */
public final class ConfigManager {
	private static Logger log = LoggerFactory.getLogger(ConfigManager.class);
	//是否激活云对象存储
	public static boolean ENABLE_COS=true;
	public static CloudStorageInteface  cloudStroage = null;
	private String webId;
    
	private final String rootPath;
	private final String originalPath;
	private final String contextPath;
	private static final String configFileName = "config.json";
	private String parentPath = null;
	private JSONObject jsonConfig = null;
	// 涂鸦上传filename定义
	private final static String SCRAWL_FILE_NAME = "scrawl";
	// 远程图片抓取filename定义
	private final static String REMOTE_FILE_NAME = "remote";
	
	/*
	 * 通过一个给定的路径构建一个配置管理器， 该管理器要求地址路径所在目录下必须存在config.properties文件
	 */
	private ConfigManager (HttpServletRequest request, String rootPath, String contextPath, String uri ) throws FileNotFoundException, IOException {
		HttpSession session = request.getSession();
		Integer webid = (Integer) session.getAttribute("webId");
		log.info(" baidu udeditor ,sessionId="+session.getId()); //session=”false” in jsp
		if(webid==null)
		{
			log.info("webId is null in session in baidu udeditor ConfigMananger");
		}else
			webId = webid.toString();
		
		rootPath = rootPath.replace( "\\", "/" );
		this.rootPath = rootPath;
		this.contextPath = contextPath;
		
		if ( contextPath.length() > 0 ) {
			this.originalPath = this.rootPath + uri.substring( contextPath.length() );
		} else {
			this.originalPath = this.rootPath + uri;
		}
		
		this.initEnv();
		
	}
	
	/**
	 * 配置管理器构造工厂
	 * @param rootPath 服务器根路径
	 * @param contextPath 服务器所在项目路径
	 * @param uri 当前访问的uri
	 * @return 配置管理器实例或者null
	 */
	public static ConfigManager getInstance (HttpServletRequest request, String rootPath, String contextPath, String uri ) {
		try {
			return new ConfigManager(request, rootPath, contextPath, uri);
		} catch ( Exception e ) {
			return null;
		}
		
	}
	
	// 验证配置文件加载是否正确
	public boolean valid () {
		return this.jsonConfig != null;
	}
	
	public JSONObject getAllConfig () {
		
		return this.jsonConfig;
		
	}
	public Integer getInt(String key)
	{
		try {
			return this.jsonConfig.getInt(key);
		} catch (JSONException e) {
			log.warn("got JSONException in ueditor,key="+key);
			//e.printStackTrace();	
		}
		return null;
	}
	public Long getLong(String key)
	{
		try {
			return this.jsonConfig.getLong(key);
		} catch (JSONException e) {
			log.warn("got JSONException in ueditor,key="+key);
			//e.printStackTrace();	
		}
		return null;
	}
	public String getString(String key)
	{
		try {
			return this.jsonConfig.getString(key);
		} catch (JSONException e) {
			log.warn("got JSONException in ueditor,key="+key);
			//e.printStackTrace();	
		}
		return null;
	}
	public Map<String, Object> getConfig ( int type ) {
		
		Map<String, Object> conf = new HashMap<String, Object>();
		String savePath = null;
		
		switch ( type ) {
		
			case ActionMap.UPLOAD_FILE:
				conf.put( "isBase64", "false" );
				conf.put( "maxSize", getString( "fileMaxSize" ) );
				conf.put( "allowFiles", this.getArray( "fileAllowFiles" ) );
				conf.put( "fieldName", getString( "fileFieldName" ) );
				savePath = getString( "filePathFormat" );
				break;
				
			case ActionMap.UPLOAD_IMAGE:
				conf.put( "isBase64", "false" );
				conf.put( "maxSize", getLong( "imageMaxSize" ) );
				conf.put( "allowFiles", this.getArray( "imageAllowFiles" ) );
				conf.put( "fieldName", getString( "imageFieldName" ) );
				savePath = getString( "imagePathFormat" );
				break;
				
			case ActionMap.UPLOAD_VIDEO:
				conf.put( "maxSize", getLong( "videoMaxSize" ) );
				conf.put( "allowFiles", this.getArray( "videoAllowFiles" ) );
				conf.put( "fieldName", getString( "videoFieldName" ) );
				savePath = getString( "videoPathFormat" );
				break;
				
			case ActionMap.UPLOAD_SCRAWL:
				conf.put( "filename", ConfigManager.SCRAWL_FILE_NAME );
				conf.put( "maxSize", getLong( "scrawlMaxSize" ) );
				conf.put( "fieldName", getString( "scrawlFieldName" ) );
				conf.put( "isBase64", "true" );
				savePath = getString( "scrawlPathFormat" );
				break;
				
			case ActionMap.CATCH_IMAGE:
				conf.put( "filename", ConfigManager.REMOTE_FILE_NAME );
				conf.put( "filter", this.getArray( "catcherLocalDomain" ) );
				conf.put( "maxSize", getLong( "catcherMaxSize" ) );
				conf.put( "allowFiles", this.getArray( "catcherAllowFiles" ) );
				conf.put( "fieldName", getString( "catcherFieldName" ) + "[]" );
				savePath = getString( "catcherPathFormat" );
				break;
				
			case ActionMap.LIST_IMAGE:
				conf.put( "allowFiles", this.getArray( "imageManagerAllowFiles" ) );
				conf.put( "dir", getString( "imageManagerListPath" ) );
				conf.put( "count", getInt( "imageManagerListSize" ) );
				break;
				
			case ActionMap.LIST_FILE:
				conf.put( "allowFiles", this.getArray( "fileManagerAllowFiles" ) );
				conf.put( "dir", getString( "fileManagerListPath" ) );
				conf.put( "count", getInt( "fileManagerListSize" ) );
				break;
				
		}
		//若包含webId将其替换为Id号
		if(webId!=null && savePath!=null&&savePath.contains("webId"))
		{
			savePath=savePath.replace("webId",webId);	
		}
		log.info("in baidu udeditor ConfigMananger, rootPath="+rootPath+",savePath="+savePath);
		conf.put( "savePath", savePath );
		conf.put( "rootPath", this.rootPath );
		
		return conf;
		
	}
	
	/**
     * Get rootPath from request,if not,find it from conf map.
     * @param request
     * @param conf
     * @return
     * @author Ternence
     * @create 2015年1月31日
     */
    public static String getRootPath(HttpServletRequest request, Map<String, Object> conf) {
        Object rootPath = request.getAttribute("rootPath");
        if (rootPath != null) {
            return rootPath + "" + File.separatorChar;
        } else {
            return conf.get("rootPath") + "";
        }
    }

    private void initEnv () throws FileNotFoundException, IOException {
		
		File file = new File( this.originalPath );
		
		if ( !file.isAbsolute() ) {
			file = new File( file.getAbsolutePath() );
		}
		
		this.parentPath = file.getParent();
		
		String configContent = this.readFile( this.getConfigPath() );
		
		try{
			JSONObject jsonConfig = new JSONObject( configContent );
			this.jsonConfig = jsonConfig;
		} catch ( Exception e ) {
			this.jsonConfig = null;
			log.warn("got JSONException in ueditor,configContent="+configContent);
			//e.printStackTrace();	
		}
		
	}
	
	private String getConfigPath () {
        String path = this.getClass().getResource("/").getPath() + ConfigManager.configFileName;
        if (new File(path).exists()) {
          return path;
        }else {          
          return this.parentPath + File.separator + ConfigManager.configFileName;
        }
	}

	private String[] getArray ( String key ) {
		try {
			JSONArray jsonArray = this.jsonConfig.getJSONArray( key );
			String[] result = new String[ jsonArray.length() ];
			
			for ( int i = 0, len = jsonArray.length(); i < len; i++ ) {
				result[i] = jsonArray.getString( i );
			}
			return result;
		} catch (JSONException e) {
			log.warn("got JSONException in ueditor,key="+key);
		}
		
		return null;
	}
	
	private String readFile ( String path ) throws IOException {
		
		StringBuilder builder = new StringBuilder();
		
		try {
			
			InputStreamReader reader = new InputStreamReader( new FileInputStream( path ), "UTF-8" );
			BufferedReader bfReader = new BufferedReader( reader );
			
			String tmpContent = null;
			
			while ( ( tmpContent = bfReader.readLine() ) != null ) {
				builder.append( tmpContent );
			}
			
			bfReader.close();
			
		} catch ( UnsupportedEncodingException e ) {
			// 忽略
		}
		
		return this.filter( builder.toString() );
		
	}
	
	// 过滤输入字符串, 剔除多行注释以及替换掉反斜杠
	private String filter ( String input ) {
		return input.replaceAll( "/\\*[\\s\\S]*?\\*/", "" );
	}
	
}
