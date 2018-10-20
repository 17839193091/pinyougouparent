package entity;

import java.io.Serializable;

/**
 * 描述:
 *  请求返回结果通知实体类
 * @author hudongfei
 * @create 2018-10-20 19:06
 */
public class Result implements Serializable {
    private boolean success;
    private String message;

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
