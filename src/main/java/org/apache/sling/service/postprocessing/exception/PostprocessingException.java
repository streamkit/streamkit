package org.apache.sling.service.postprocessing.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Cosmin Stanciu
 */
public class PostprocessingException extends Exception {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	public static final String CODEC = "1";
	public static final String COMPILING = "2";
    public static final String GENERAL = "3";

	private String code;
	private String logMessage;
	private Object[] params;

    public PostprocessingException(){}

	public PostprocessingException( String code, String logMessage, Exception e, Object[] params) {
		this.code = code;
		this.logMessage = logMessage;
		this.params = params;
		log.error(logMessage, e);
	}

	public PostprocessingException( String code, String logMessage, Exception e) {
		this(code, logMessage, e, null);
	}

	public PostprocessingException( String code, Exception e) {
		this(code, null, e, null);
	}

	public PostprocessingException(String code, String logMessage) {
		this(code, logMessage, null, null);
	}

    public PostprocessingException(String logMessage) {
        this(null, logMessage, null, null);
    }


	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLogMessage() {
		return logMessage;
	}

	public void setLogMessage(String logMessage) {
		this.logMessage = logMessage;
	}

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }
}
