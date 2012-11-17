package com.dotview.Common;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logs
{
	private static Logs mLog;
	/**日志保存路径*/
	private static final String LOG_SAVE_PATH = "sdcard/FriendsOnMap/";
	/**日志开关*/
	private static final boolean LOG_SWITCH = false;
	
	public static Logs LogInstance(){
		if(mLog == null){
			mLog = new Logs();
		}
		return mLog;
	}
	
	/**插入日志*/
	public void addLog(String logStr){
		if(LOG_SWITCH){
			File file = checkLogFileIsExist();
			if(file == null)
				return;
			FileOutputStream fos = null;
			try
			{
				fos = new FileOutputStream(file, true);
				fos.write((new Date().toLocaleString() + "	" + logStr).getBytes("gbk"));
				fos.write("\r\n".getBytes("gbk"));
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally{
				try
				{
					if(fos != null){
						fos.close();
						fos = null;
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				fos = null;
				file = null;
			}
		}
	}
	
	/**检查日志文件是否存在*/
	private File checkLogFileIsExist(){
		File file = new File(LOG_SAVE_PATH);
		if(!file.exists()){
			file.mkdirs();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(new Date());
		file = new File(LOG_SAVE_PATH + dateStr + ".txt");
		if(!isLogExist(file)){
			try
			{
				file.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		sdf = null;
		return file;
	}
	
	/**
	 * 检查当天日志文件是否存在
	 * @param file
	 * @return
	 */
	private boolean isLogExist(File file){
		File tempFile = new File(LOG_SAVE_PATH);
		File[] files = tempFile.listFiles();
		for(int i = 0; i < files.length; i++){
			if(files[0].getName().trim().equalsIgnoreCase(file.getName())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 打印异常堆栈信息
	 * @param e
	 * @return
	 */
	public static String getExceptionStackTrace(Throwable e){
        if(e != null){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        }
        return "";
    }
}